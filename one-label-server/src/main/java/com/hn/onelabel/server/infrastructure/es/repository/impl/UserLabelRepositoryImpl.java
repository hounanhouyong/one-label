package com.hn.onelabel.server.infrastructure.es.repository.impl;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.ons.api.Message;
import com.google.common.base.Joiner;
import com.google.common.collect.Lists;
import com.hn.onelabel.api.enums.DatasourceEnum;
import com.hn.onelabel.api.model.request.DeleteLabelDimensionOperationRequest;
import com.hn.onelabel.api.model.request.SyncUserLabelRequest;
import com.hn.onelabel.server.common.component.CacheService;
import com.hn.onelabel.server.common.utils.LocalDateTimeUtils;
import com.hn.onelabel.server.common.utils.RandomUtils;
import com.hn.onelabel.server.common.utils.SequenceIdUtils;
import com.hn.onelabel.server.domain.aggregate.userlabel.UserLabel;
import com.hn.onelabel.server.domain.aggregate.userlabel.repository.UserLabelRepository;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.Label;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.LabelDimension;
import com.hn.onelabel.server.infrastructure.cache.RedisCacheService;
import com.hn.onelabel.server.infrastructure.cache.RedisKey;
import com.hn.onelabel.server.infrastructure.db.LabelDimensionDataDO;
import com.hn.onelabel.server.infrastructure.db.mapper.LabelDimensionDataMapper;
import com.hn.onelabel.server.infrastructure.es.ElasticsearchProperties;
import com.hn.onelabel.server.infrastructure.mq.RocketMqProducer;
import com.hn.onelabel.server.infrastructure.nacos.*;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.join.ScoreMode;
import org.elasticsearch.action.bulk.BulkProcessor;
import org.elasticsearch.action.delete.DeleteRequest;
import org.elasticsearch.action.get.GetRequest;
import org.elasticsearch.action.get.GetResponse;
import org.elasticsearch.action.index.IndexRequest;
import org.elasticsearch.action.search.SearchRequest;
import org.elasticsearch.action.search.SearchResponse;
import org.elasticsearch.action.search.SearchType;
import org.elasticsearch.action.update.UpdateRequest;
import org.elasticsearch.client.RequestOptions;
import org.elasticsearch.client.RestHighLevelClient;
import org.elasticsearch.common.xcontent.XContentType;
import org.elasticsearch.index.query.QueryBuilder;
import org.elasticsearch.index.query.QueryBuilders;
import org.elasticsearch.rest.RestStatus;
import org.elasticsearch.search.SearchHit;
import org.elasticsearch.search.SearchHits;
import org.elasticsearch.search.builder.SearchSourceBuilder;
import org.elasticsearch.search.sort.SortOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class UserLabelRepositoryImpl implements UserLabelRepository {

    @Autowired
    private RestHighLevelClient client;
    @Autowired
    private ElasticsearchProperties elasticsearchProperties;
    @Autowired
    private BulkProcessor bulkProcessor;
    @Autowired
    private RedisCacheService redisCacheService;
    @Autowired
    private LabelDimensionDataMapper labelDimensionDataMapper;

    @Autowired
    private LabelQueryConfigLoader labelQueryConfigLoader;
    @Autowired
    private LabelDimensionConfigLoader labelDimensionConfigLoader;
    @Autowired
    private SwitchConfigLoader switchConfigLoader;
    @Autowired
    private ParallelProcessingSwitchConfigLoader parallelProcessingSwitchConfigLoader;
    @Autowired
    private LogSwitchConfigLoader logSwitchConfigLoader;
    @Autowired
    private UserLruConfigLoader userLruConfigLoader;

    @Autowired
    private MapperFacade mapperFacade;

    @Autowired
    private RocketMqProducer mqProducer;

    private static final Long DIMENSION_KEY_ID_SHARDING_INTERVAL = 1000000000000L;

    private final ExecutorService executorService = Executors.newFixedThreadPool(8);

    @Override
    public boolean findUserHasLabel(Long userId, String labelCode, boolean used) {
        Objects.requireNonNull(userId, "Null userId.");
        Objects.requireNonNull(labelCode, "Null labelCode.");

        if (used) {
            this.lru(userId);
        }

        return this.findUserHasLabelByCacheAndEs(userId, labelCode, labelQueryConfigLoader.getDatasourceList(labelCode).contains(DatasourceEnum.ES.name()));
    }

    @Override
    public List<LabelDimension> findUserLabelDimensions(Long userId, String labelCode, boolean used, boolean syncEsDataToRedis) {
        Objects.requireNonNull(userId, "Null userId.");
        Objects.requireNonNull(labelCode, "Null labelCode.");
        List<LabelDimension> responses = new ArrayList<>();

        UserLabel userLabel = this.findUserLabelByCacheAndEs(userId, labelQueryConfigLoader.getDatasourceList(labelCode).contains(DatasourceEnum.ES.name()), syncEsDataToRedis);
        if (Objects.isNull(userLabel)) {
            return responses;
        }

        List<LabelDimension> labelDimensions = userLabel.getLabelDimensions().stream().filter(labelDimension -> labelDimension.getLabelCode().equals(labelCode)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(labelDimensions)) {
            return responses;
        }

        CompletionService<List<LabelDimensionDataDO>> executorCompletionService = new ExecutorCompletionService<>(executorService);
        List<Future<List<LabelDimensionDataDO>>> futures = new ArrayList<>();

        labelDimensions.removeIf(labelDimension -> labelDimension.getDimensionKey().equals("@sequenceId@"));
        labelDimensions.forEach(labelDimension -> {
            if (labelDimension.getDimensionKey().equals("@labelDimensionData@")) {
                if (parallelProcessingSwitchConfigLoader.parallelFindLabelDimensionsSwitchIsOpen()) {
                    futures.add(executorCompletionService.submit(() -> this.findLabelDimensionDataFromDb(userId, this.getDimensionKeyIdShardingNos(labelDimension.getDimensionKeyId(), labelDimension.getDimensionVal()))));
                } else {
                    this.buildLabelDimensions(responses, this.findLabelDimensionDataFromDb(userId, this.getDimensionKeyIdShardingNos(labelDimension.getDimensionKeyId(), labelDimension.getDimensionVal())));
                }
            } else {
                responses.add(labelDimension);
            }
        });

        if (parallelProcessingSwitchConfigLoader.parallelFindLabelDimensionsSwitchIsOpen()) {
            for (int i=0; i<futures.size(); i++) {
                try {
                    this.buildLabelDimensions(responses, executorCompletionService.take().get());
                } catch (InterruptedException | ExecutionException e) {
                    log.error("[findLabelDimensions] - executorCompletionService exception.", e);
                }
            }
        }

        if (used) {
            this.lru(userId);
        }

        return responses;
    }

    @Override
    public List<LabelDimension> findUserLabelDimensions(Long userId, List<String> labelCodes, boolean used, boolean syncEsDataToRedis) {
        Objects.requireNonNull(userId, "Null userId.");
        List<LabelDimension> responses = new ArrayList<>();
        if (CollectionUtils.isEmpty(labelCodes)) {
            return responses;
        }

        AtomicReference<Boolean> containsEsDatasource = new AtomicReference<>(false);
        labelCodes.forEach(labelCode -> {
            if (containsEsDatasource.get()) {
                return;
            }
            if (labelQueryConfigLoader.getDatasourceList(labelCode).contains(DatasourceEnum.ES.name())) {
                containsEsDatasource.set(true);
            }
        });

        UserLabel userLabel = this.findUserLabelByCacheAndEs(userId, containsEsDatasource.get(), syncEsDataToRedis);
        if (Objects.isNull(userLabel)) {
            return responses;
        }

        List<LabelDimension> labelDimensions = userLabel.getLabelDimensions().stream().filter(labelDimension -> labelCodes.contains(labelDimension.getLabelCode())).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(labelDimensions)) {
            return responses;
        }

        CompletionService<List<LabelDimensionDataDO>> executorCompletionService = new ExecutorCompletionService<>(executorService);
        List<Future<List<LabelDimensionDataDO>>> futures = new ArrayList<>();

        labelDimensions.removeIf(labelDimension -> labelDimension.getDimensionKey().equals("@sequenceId@"));
        labelDimensions.forEach(labelDimension -> {
            if (labelDimension.getDimensionKey().equals("@labelDimensionData@")) {
                if (parallelProcessingSwitchConfigLoader.parallelFindLabelDimensionsSwitchIsOpen()) {
                    futures.add(executorCompletionService.submit(() -> this.findLabelDimensionDataFromDb(userId, this.getDimensionKeyIdShardingNos(labelDimension.getDimensionKeyId(), labelDimension.getDimensionVal()))));
                } else {
                    this.buildLabelDimensions(responses, this.findLabelDimensionDataFromDb(userId, this.getDimensionKeyIdShardingNos(labelDimension.getDimensionKeyId(), labelDimension.getDimensionVal())));
                }
            } else {
                responses.add(labelDimension);
            }
        });

        if (parallelProcessingSwitchConfigLoader.parallelFindLabelDimensionsSwitchIsOpen()) {
            for (int i=0; i<futures.size(); i++) {
                try {
                    this.buildLabelDimensions(responses, executorCompletionService.take().get());
                } catch (InterruptedException | ExecutionException e) {
                    log.error("[findLabelDimensions] - executorCompletionService exception.", e);
                }
            }
        }

        if (used) {
            this.lru(userId);
        }

        return responses;
    }

    private void buildLabelDimensions(List<LabelDimension> responses, List<LabelDimensionDataDO> documents) {
        if (CollectionUtils.isEmpty(documents)) {
            return;
        }
        documents.forEach(document -> {
            List<LabelDimension> labelDimensionList = JSON.parseArray(document.getLabelDimension(), LabelDimension.class);
            if (!CollectionUtils.isEmpty(labelDimensionList)) {
                labelDimensionList.removeIf(ld -> ld.getDimensionKey().equals("@sequenceId@"));
                responses.addAll(labelDimensionList);
            }
        });
    }

    @Override
    public LabelDimension findUserLabelDimension(Long userId, String labelCode, Long labelDimensionKeyId, String labelDimensionKey, boolean used) {
        Objects.requireNonNull(userId, "Null userId.");
        Objects.requireNonNull(labelCode, "Null labelCode.");
        Objects.requireNonNull(labelDimensionKeyId, "Null labelDimensionKeyId.");
        Objects.requireNonNull(labelDimensionKey, "Null labelDimensionKey.");
        if (used) {
            this.lru(userId);
        }
        if (labelDimensionConfigLoader.enableDimensionHotKey(labelCode, labelDimensionKeyId)) {
            String dimensionVal = redisCacheService.get(RedisKey.getUserLabelDimensionHotRedisKey(userId, labelCode, labelDimensionKeyId, labelDimensionKey));
            if (!StringUtils.isEmpty(dimensionVal)) {
                return new LabelDimension(labelCode, labelDimensionKeyId, labelDimensionKey, dimensionVal);
            } else if (labelDimensionConfigLoader.enableFindLabelDimensionsWhenRedisHotKeyMisses(labelCode, labelDimensionKeyId)) {
                LabelDimension labelDimension = this.findUserLabelDimensions(userId, labelCode, used, false).stream().filter(ld -> ld.getDimensionKey().equals(labelDimensionKey)).findFirst().orElse(null);
                if (!Objects.isNull(labelDimension)) {
                    Message message = new Message();
                    message.setTopic("xxx");
                    message.setTag("deleteLabelDimensionOperation");
                    String msgId = mqProducer.syncSend(message, DeleteLabelDimensionOperationRequest.builder()
                            .userId(userId)
                            .labelCode(labelCode)
                            .labelDimensionKeyList(Collections.singletonList(labelDimensionKey))
                            .build());
                    if (logSwitchConfigLoader.userLabelRepositoryPrintLogSwitchIsOpen()) {
                        log.info("[findUserLabelDimension] - [deleteLabelDimensionOperation] - userId: {}, labelCode: {}, labelDimensionKey: {}, msgId: {}", userId, labelCode, labelDimensionKey, msgId);
                    }
                }
                return labelDimension;
            } else {
                return null;
            }
        } else {
            return this.findUserLabelDimensions(userId, labelCode, used, false).stream().filter(ld -> ld.getDimensionKey().equals(labelDimensionKey)).findFirst().orElse(null);
        }
    }

    @Override
    public UserLabel findUserLabel(Long userId, boolean used, boolean syncEsDataToRedis) {
        if (used) {
            this.lru(userId);
        }
        if (parallelProcessingSwitchConfigLoader.parallelBuildUserLabelHistorySwitchIsOpen()) {
            return this.parallelFindUserLabelByMultiDataSource(userId, syncEsDataToRedis);
        } else {
            return this.findUserLabelByMultiDataSource(userId, syncEsDataToRedis);
        }
    }

    private void lru(Long userId) {
        redisCacheService.set(RedisKey.getUserLruRedisKey(userId), LocalDateTimeUtils.getFormatDateString(LocalDateTime.now(), "yyyy-MM-dd HH:mm:ss"), userLruConfigLoader.getHowManyDaysIsUserLruDataRetained(7), TimeUnit.DAYS);
    }

    @Override
    public void saveUserLabelDimensionHotKey(Long userId, String labelCode, Long labelDimensionKeyId, String labelDimensionHotKey, String labelDimensionVal, Long timeoutSeconds) {
        Objects.requireNonNull(userId, "Null userId.");
        Objects.requireNonNull(labelCode, "Null labelCode.");
        Objects.requireNonNull(labelDimensionKeyId, "Null labelDimensionKeyId.");
        Objects.requireNonNull(labelDimensionHotKey, "Null labelDimensionHotKey.");
        Objects.requireNonNull(timeoutSeconds, "Null timeoutSeconds.");
        redisCacheService.set(RedisKey.getUserLabelDimensionHotRedisKey(userId, labelCode, labelDimensionKeyId, labelDimensionHotKey), labelDimensionVal, timeoutSeconds, TimeUnit.SECONDS);
    }

    @Override
    public void saveUserLabel(UserLabel userLabel) {
        Objects.requireNonNull(userLabel, "Null userLabel.");

        // redis
        this.saveUserLabelToRedis(userLabel);

        // es
        if (switchConfigLoader.writeUserLabelToEsSwitchIsOpen()) {
            if (this.isExistsFromEs(userLabel.getUserId())) {
                UpdateRequest request = new UpdateRequest(elasticsearchProperties.getIndex(), "_doc", String.valueOf(userLabel.getUserId()));
                request.doc(JSON.toJSONStringWithDateFormat(userLabel, JSON.DEFFAULT_DATE_FORMAT), XContentType.JSON);
                bulkProcessor.add(request);
            } else {
                IndexRequest request = new IndexRequest(elasticsearchProperties.getIndex(), "_doc", String.valueOf(userLabel.getUserId()));
                request.source(JSON.toJSONStringWithDateFormat(userLabel, JSON.DEFFAULT_DATE_FORMAT), XContentType.JSON);
                bulkProcessor.add(request);
            }
//            bulkProcessor.flush();
        }

//        try {
//            // 30秒后关闭BulkProcessor
//            bulkProcessor.awaitClose(30, TimeUnit.SECONDS);
//        } catch (InterruptedException e) {
//            log.info("[saveUserLabel] - bulkProcessor.awaitClose exception.", e);
//        }
    }

    @Override
    public void syncUserLabel(Long userId) {
        Objects.requireNonNull(userId, "Null userId.");
        UserLabel userLabel = this.findUserLabelByEs(userId);
        if (!Objects.isNull(userLabel)) {
            this.saveUserLabelToRedis(userLabel);
        }
    }

    private void saveUserLabelToRedis(UserLabel userLabel) {
        UserLabel userLabel4redis = mapperFacade.map(userLabel, UserLabel.class);

        // db
        if (parallelProcessingSwitchConfigLoader.parallelWriteLabelDimensionsToDbSwitchIsOpen()) {
            this.parallelWriteLabelDimensionsToDb(userLabel4redis);
        } else {
            this.writeLabelDimensionsToDb(userLabel4redis);
        }

        // redis
        redisCacheService.hSet(RedisKey.getUserLabelsRedisHashKey(userLabel4redis.getUserId()), String.valueOf(userLabel4redis.getUserId()), JSON.toJSONString(userLabel4redis));
    }

    @Override
    public void clearUserLabel(List<Long> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return;
        }
        userIds.forEach(userId -> {
            // redis
            if (logSwitchConfigLoader.userLabelRepositoryPrintLogSwitchIsOpen()) {
                log.info("[clearUserLabel] - [redis] - userId: {}, redisKey: {}, field: {}", userId, RedisKey.getUserLabelsRedisHashKey(userId), userId);
            }
            redisCacheService.hDel(RedisKey.getUserLabelsRedisHashKey(userId), String.valueOf(userId));
            // es
            if (switchConfigLoader.writeUserLabelToEsSwitchIsOpen() && this.isExistsFromEs(userId)) {
                if (logSwitchConfigLoader.userLabelRepositoryPrintLogSwitchIsOpen()) {
                    log.info("[clearUserLabel] - [es] - userId: {}", userId);
                }
                DeleteRequest request = new DeleteRequest(elasticsearchProperties.getIndex(), "_doc", String.valueOf(userId));
                bulkProcessor.add(request);
//                bulkProcessor.flush();
            }
        });
    }

    @Override
    public Long countLabelDimensions(String labelCode, String dimensionKey) {
        Objects.requireNonNull(labelCode, "Null labelCode.");
        Objects.requireNonNull(dimensionKey, "Null dimensionKey.");
        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.nestedQuery("labelDimensions",
                        QueryBuilders.boolQuery().must(QueryBuilders.termQuery("labelDimensions.labelCode", labelCode))
                                .must(QueryBuilders.termQuery("labelDimensions.dimensionKey", dimensionKey)),
                        ScoreMode.None));
        try {
            return this.countFromEs(queryBuilder, null);
        } catch (IOException e) {
            log.error("[countLabelDimensions] - es search exception.", e);
        }

        return 0L;
    }

    @Override
    public Long countLabelDimensions(String labelCode, String dimensionKey, String dimensionVal) {
        Objects.requireNonNull(labelCode, "Null labelCode.");
        Objects.requireNonNull(dimensionKey, "Null dimensionKey.");
        Objects.requireNonNull(dimensionVal, "Null dimensionVal.");
        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.nestedQuery("labelDimensions",
                        QueryBuilders.boolQuery().must(QueryBuilders.termQuery("labelDimensions.labelCode", labelCode))
                                .must(QueryBuilders.termQuery("labelDimensions.dimensionKey", dimensionKey))
                                .must(QueryBuilders.termQuery("labelDimensions.dimensionVal", dimensionVal)),
                        ScoreMode.None));
        try {
            return this.countFromEs(queryBuilder, null);
        } catch (IOException e) {
            log.error("[countLabelDimensions] - es search exception.", e);
        }

        return 0L;
    }

    @Override
    public Pair<List<Long>, Object[]> findUserIdsByConditionsAndPageBySearchAfter(String labelCode, String dimensionKey, String dimensionVal, BooleanClause.Occur occur, Integer pageSize, Object[] sortValues) {

        QueryBuilder labelDimensionQueryBuilder;

        if (BooleanClause.Occur.MUST.name().equals(occur.name())) {
            labelDimensionQueryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("labelDimensions.labelCode", labelCode))
                    .must(QueryBuilders.termQuery("labelDimensions.dimensionKey", dimensionKey))
                    .must(QueryBuilders.termQuery("labelDimensions.dimensionVal", dimensionVal));
        } else if (BooleanClause.Occur.MUST_NOT.name().equals(occur.name())) {
            labelDimensionQueryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("labelDimensions.labelCode", labelCode))
                    .must(QueryBuilders.termQuery("labelDimensions.dimensionKey", dimensionKey))
                    .mustNot(QueryBuilders.termQuery("labelDimensions.dimensionVal", dimensionVal));
        } else if (BooleanClause.Occur.SHOULD.name().equals(occur.name())) {
            labelDimensionQueryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("labelDimensions.labelCode", labelCode))
                    .must(QueryBuilders.termQuery("labelDimensions.dimensionKey", dimensionKey))
                    .should(QueryBuilders.termQuery("labelDimensions.dimensionVal", dimensionVal));
        } else {
            labelDimensionQueryBuilder = QueryBuilders.boolQuery()
                    .must(QueryBuilders.termQuery("labelDimensions.labelCode", labelCode))
                    .must(QueryBuilders.termQuery("labelDimensions.dimensionKey", dimensionKey))
                    .filter(QueryBuilders.termQuery("labelDimensions.dimensionVal", dimensionVal));
        }

        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.nestedQuery("labelDimensions", labelDimensionQueryBuilder, ScoreMode.None));

        String sourceField = "userId";
        List<Long> userIdList = new ArrayList<>();

        try {
            Pair<SearchHits, Object[]> pair = this.searchAfterFromEs(queryBuilder, null, pageSize, sortValues, new String[]{sourceField}, new String[]{});
            if (!Objects.isNull(pair)) {
                if (logSwitchConfigLoader.userLabelRepositoryPrintLogSwitchIsOpen()) {
                    log.info("[userLabel] - hits: {}, sortValues: {}.", JSON.toJSONString(pair.getLeft()), pair.getRight());
                }
                if (pair.getLeft().totalHits > 0) {
                    for (SearchHit searchHit : pair.getLeft().getHits()) {
                        Map<String, Object> map = searchHit.getSourceAsMap();
                        if (map.containsKey(sourceField)) {
                            userIdList.add(Long.parseLong(String.valueOf(map.get(sourceField))));
                        }
                    }
                    return Pair.of(userIdList, pair.getRight());
                }
            }
        } catch (IOException e) {
            log.error("[userLabel] - es search exception.", e);
        }

        return null;
    }

    private void writeLabelDimensionsToDb(UserLabel userLabel) {
        if (!CollectionUtils.isEmpty(userLabel.getLabelDimensions())) {
            userLabel.getLabelDimensions().stream().collect(Collectors.groupingBy(LabelDimension::getDimensionKeyId)).forEach((dimensionKeyId, labelDimensions) -> {
                List<String> labelCodeList = labelDimensions.stream().map(LabelDimension::getLabelCode).distinct().collect(Collectors.toList());
                if (CollectionUtils.isEmpty(labelCodeList)) {
                    return;
                }
                String labelCode = labelCodeList.get(0);
                if (logSwitchConfigLoader.userLabelRepositoryPrintLogSwitchIsOpen()) {
                    log.info("[saveUserLabel] - [writeLabelDimensionsToDb] - userId: {}, labelCode: {}, dimensionKeyId: {}, labelDimensions: {}.", userLabel.getUserId(), labelCode, dimensionKeyId, JSON.toJSONString(labelDimensions));
                }
                if (labelDimensionConfigLoader.enableWriteDataToDb(labelCode, dimensionKeyId)) {
                    userLabel.getLabelDimensions().removeIf(item -> item.getDimensionKeyId().equals(dimensionKeyId));
                    userLabel.getLabelDimensions().add(new LabelDimension(labelCode, dimensionKeyId, "@labelDimensionData@", Joiner.on(",").join(this.saveLabelDimensionShardingData(userLabel.getUserId(), labelCode, dimensionKeyId, labelDimensions))));
                }
            });
        }
    }

    private void parallelWriteLabelDimensionsToDb(UserLabel userLabel) {
        if (!CollectionUtils.isEmpty(userLabel.getLabelDimensions())) {
            CompletionService<Pair<String, Pair<Long, List<Long>>>> executorCompletionService = new ExecutorCompletionService<>(executorService);
            List<Future<Pair<String, Pair<Long, List<Long>>>>> futures = new ArrayList<>();
            userLabel.getLabelDimensions().stream().collect(Collectors.groupingBy(LabelDimension::getDimensionKeyId)).forEach((dimensionKeyId, labelDimensions) -> {
                List<String> labelCodeList = labelDimensions.stream().map(LabelDimension::getLabelCode).distinct().collect(Collectors.toList());
                if (CollectionUtils.isEmpty(labelCodeList)) {
                    return;
                }
                String labelCode = labelCodeList.get(0);
                if (logSwitchConfigLoader.userLabelRepositoryPrintLogSwitchIsOpen()) {
                    log.info("[saveUserLabel] - [parallelWriteLabelDimensionsToDb] - userId: {}, labelCode: {}, dimensionKeyId: {}, labelDimensions: {}.", userLabel.getUserId(), labelCode, dimensionKeyId, JSON.toJSONString(labelDimensions));
                }
                if (!labelDimensionConfigLoader.enableWriteDataToDb(labelCode, dimensionKeyId)) {
                    return;
                }
                futures.add(executorCompletionService.submit(() -> Pair.of(labelCode, Pair.of(dimensionKeyId, this.saveLabelDimensionShardingData(userLabel.getUserId(), labelCode, dimensionKeyId, labelDimensions)))));
            });
            for (int i=0; i<futures.size(); i++) {
                Pair<String, Pair<Long, List<Long>>> pair = null;
                try {
                    pair = executorCompletionService.take().get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("[saveUserLabel] - [parallelWriteLabelDimensionsToDb] - executorCompletionService exception.", e);
                }
                if (!Objects.isNull(pair) && !Objects.isNull(pair.getRight())) {
                    if (logSwitchConfigLoader.userLabelRepositoryPrintLogSwitchIsOpen()) {
                        log.info("[saveUserLabel] - [parallelWriteLabelDimensionsToDb] - write labelDimension to db, userId: {}, labelCode: {}, dimensionKeyId: {}, dimensionKeyIdShardingNo: {}.", userLabel.getUserId(), pair.getLeft(), pair.getRight().getLeft(), JSON.toJSONString(pair.getRight().getRight()));
                    }
                    Iterator<LabelDimension> it = userLabel.getLabelDimensions().iterator();
                    while (it.hasNext()) {
                        LabelDimension item = it.next();
                        if (item.getDimensionKeyId().equals(pair.getRight().getLeft())) {
                            it.remove();
                        }
                    }
                    userLabel.getLabelDimensions().add(new LabelDimension(pair.getLeft(), pair.getRight().getLeft(), "@labelDimensionData@", Joiner.on(",").join(pair.getRight().getRight())));
                }
            }
        }
    }

    private List<Long> saveLabelDimensionShardingData(Long userId, String labelCode, Long dimensionKeyId, List<LabelDimension> labelDimensions) {
        AtomicReference<Integer> shardingNo = new AtomicReference<>(0);
        List<List<LabelDimension>> partitions = Lists.partition(labelDimensions, labelDimensionConfigLoader.getShardingDimensionValPartitionSize(labelCode, dimensionKeyId, 100));
        return partitions.stream().map(partition -> {
            Long dimensionShardingKey = this.buildDimensionKeyIdShardingNo(dimensionKeyId, shardingNo.getAndSet(shardingNo.get() + 1));
            List<LabelDimension> finalLabelDimensions = mapperFacade.mapAsList(partition, LabelDimension.class);
            finalLabelDimensions.removeIf(ld -> ld.getDimensionKey().equals("@sequenceId@"));
            finalLabelDimensions.add(new LabelDimension(labelCode, dimensionKeyId, "@sequenceId@", SequenceIdUtils.generateSequenceId()));
            LabelDimensionDataDO document = new LabelDimensionDataDO();
            document.setUserId(userId);
            document.setLabelDimensionKeyId(dimensionShardingKey);
            document.setLabelDimension(JSON.toJSONString(finalLabelDimensions));
            labelDimensionDataMapper.insertEntityOnDuplicateKeyUpdate(document);
            return dimensionShardingKey;
        }).collect(Collectors.toList());
    }

    private Long buildDimensionKeyIdShardingNo(Long dimensionKeyId, Integer shardingNo) {
        Objects.requireNonNull(dimensionKeyId, "Null dimensionKeyId.");
        Objects.requireNonNull(shardingNo, "Null shardingNo.");
        if (shardingNo == 0) {
            return dimensionKeyId;
        } else {
            return dimensionKeyId * DIMENSION_KEY_ID_SHARDING_INTERVAL + shardingNo;
        }
    }

    private List<Long> getDimensionKeyIdShardingNos(Long dimensionKeyId, String dimensionVal) {
        Objects.requireNonNull(dimensionKeyId, "Null dimensionKeyId.");
        Objects.requireNonNull(dimensionVal, "Null dimensionVal.");
        List<Long> dimensionKeyIdShardingNos = new ArrayList<>();
        List<Long> shardingNos = Arrays.stream(dimensionVal.split(",")).map(Long::parseLong).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(shardingNos)) {
            return dimensionKeyIdShardingNos;
        }
        shardingNos.forEach(shardingNo -> {
            if (shardingNo < DIMENSION_KEY_ID_SHARDING_INTERVAL || shardingNo % DIMENSION_KEY_ID_SHARDING_INTERVAL == 0) {
                dimensionKeyIdShardingNos.add(dimensionKeyId);
            } else {
                dimensionKeyIdShardingNos.add(shardingNo);
            }
        });
        return dimensionKeyIdShardingNos;
    }

    private List<LabelDimensionDataDO> findLabelDimensionDataFromDb(Long userId, List<Long> dimensionKeyIdShardingNoList) {
        Objects.requireNonNull(userId, "Null userId.");
        List<LabelDimensionDataDO> response = new ArrayList<>();
        if (CollectionUtils.isEmpty(dimensionKeyIdShardingNoList)) {
            return response;
        }
        if (dimensionKeyIdShardingNoList.size() == 1) {
            LabelDimensionDataDO document = labelDimensionDataMapper.selectByUserIdAndDimensionKeyId(userId, dimensionKeyIdShardingNoList.get(0));
            if (!Objects.isNull(document)) {
                response.add(document);
            }
        } else {
            List<LabelDimensionDataDO> documents = labelDimensionDataMapper.selectByUserIdAndDimensionKeyIdList(userId, dimensionKeyIdShardingNoList);
            if (!CollectionUtils.isEmpty(documents)) {
                response.addAll(documents);
            }
        }
        return response;
    }

    private UserLabel findUserLabelByMultiDataSource(Long userId, boolean syncEsDataToRedis) {
        UserLabel userLabelHistory = this.findUserLabelByCacheAndEs(userId, true, syncEsDataToRedis);
        if (logSwitchConfigLoader.userLabelRepositoryPrintLogSwitchIsOpen()) {
            log.info("[findUserLabelByMultiDataSource] - load pre, userId: {}, user history label: {}.", userId, JSON.toJSONString(userLabelHistory));
        }

        this.clearNullLabelFromUserLabelHistory(userLabelHistory);

        if (Objects.isNull(userLabelHistory)) {
            userLabelHistory = new UserLabel(userId);
        }

        this.distinctLabelFromUserLabelHistory(userLabelHistory);

        if (!CollectionUtils.isEmpty(userLabelHistory.getLabelDimensions())) {
            userLabelHistory.getLabelDimensions().removeIf(labelDimension -> labelDimension.getDimensionKey().equals("@sequenceId@"));
            List<Pair<Long, List<LabelDimension>>> replaceList = new ArrayList<>();
            userLabelHistory.getLabelDimensions().stream().collect(Collectors.groupingBy(LabelDimension::getDimensionKeyId)).forEach((dimensionKeyId, labelDimensions) -> labelDimensions.forEach(labelDimension -> {
                if (labelDimension.getDimensionKey().equals("@labelDimensionData@")) {
                    List<LabelDimension> replaceLabelDimensions = new ArrayList<>();
                    this.findLabelDimensionDataFromDb(userId, this.getDimensionKeyIdShardingNos(dimensionKeyId, labelDimension.getDimensionVal())).forEach(document -> {
                        if (!Objects.isNull(document)) {
                            if (logSwitchConfigLoader.userLabelRepositoryPrintLogSwitchIsOpen()) {
                                log.info("[findUserLabelByMultiDataSource] - load, userId: {}, dimensionKeyId: {}, shardingNo: {}, document: {}.", userId, dimensionKeyId, document.getLabelDimensionKeyId(), JSON.toJSONString(document));
                            }
                            List<LabelDimension> labelDimensionList = JSON.parseArray(document.getLabelDimension(), LabelDimension.class);
                            if (!CollectionUtils.isEmpty(labelDimensionList)) {
                                labelDimensionList.removeIf(ld -> ld.getDimensionKey().equals("@sequenceId@"));
                                labelDimensionList.removeIf(ld -> ld.getDimensionKey().equals("@labelDimensionData@"));
                                replaceLabelDimensions.addAll(labelDimensionList);
                            }
                        }
                    });
                    replaceList.add(Pair.of(dimensionKeyId, replaceLabelDimensions));
                }
            }));
            userLabelHistory.getLabelDimensions().removeIf(ld -> ld.getDimensionKey().equals("@labelDimensionData@"));
            if (!CollectionUtils.isEmpty(replaceList)) {
                UserLabel finalUserLabelHistory = userLabelHistory;
                replaceList.forEach(pair -> {
                    finalUserLabelHistory.getLabelDimensions().removeIf(item -> item.getDimensionKeyId().equals(pair.getLeft()));
                    finalUserLabelHistory.getLabelDimensions().addAll(pair.getRight());
                });
                userLabelHistory = finalUserLabelHistory;
            }
        }

        if (logSwitchConfigLoader.userLabelRepositoryPrintLogSwitchIsOpen()) {
            log.info("[findUserLabelByMultiDataSource] - load after, userId: {}, user history label: {}.", userId, JSON.toJSONString(userLabelHistory));
        }
        return userLabelHistory;
    }

    private boolean findUserHasLabelByCacheAndEs(Long userId, String labelCode, boolean containsEsDatasource) {
        boolean redisHasData = true;
        String userLabelsVal = redisCacheService.hGet(RedisKey.getUserLabelsRedisHashKey(userId), String.valueOf(userId));
        if (!StringUtils.isEmpty(userLabelsVal)) {
            UserLabel userLabel = JSON.parseObject(userLabelsVal, UserLabel.class);
            if (!Objects.isNull(userLabel)) {
                return userLabel.getUserLabels().stream().anyMatch(label -> label.getLabelCode().equals(labelCode));
            }
        } else {
            redisHasData = false;
        }
        if (!redisHasData) {
            if (switchConfigLoader.searchEsWhenRedisMissesSwitchIsOpen() && containsEsDatasource) {
                boolean esHasData = this.findUserHasLabelByEs(userId, labelCode);
                if (esHasData && switchConfigLoader.syncDataToRedisWhereSearchFromEsSwitchIsOpen()) {
                    Message message = new Message();
                    message.setTopic("xxx");
                    message.setTag("syncUserLabel");
                    String msgId = mqProducer.delaySend(message, SyncUserLabelRequest.builder().userId(userId).build(), RandomUtils.generateRandom(1000, 1000));
                    log.info("[syncData] - sync user label to redis when search from es, userId: {}, msgId: {}", userId, msgId);
                }
                return esHasData;
            }
        }

        return false;
    }

    private UserLabel findUserLabelByCacheAndEs(Long userId, boolean containsEsDatasource, boolean syncEsDataToRedis) {
        return CacheService.selectCacheByTemplate(() -> {
            String userLabelsVal = redisCacheService.hGet(RedisKey.getUserLabelsRedisHashKey(userId), String.valueOf(userId));
            return !StringUtils.isEmpty(userLabelsVal) ? JSON.parseObject(userLabelsVal, UserLabel.class) : null;
        }, () -> {
            if (switchConfigLoader.searchEsWhenRedisMissesSwitchIsOpen() && containsEsDatasource) {
                UserLabel userLabel = this.findUserLabelByEs(userId);
                if (!Objects.isNull(userLabel)) {
                    if (switchConfigLoader.syncDataToRedisWhereSearchFromEsSwitchIsOpen() && syncEsDataToRedis) {
                        Message message = new Message();
                        message.setTopic("xxx");
                        message.setTag("syncUserLabel");
                        String msgId = mqProducer.delaySend(message, SyncUserLabelRequest.builder().userId(userId).build(), RandomUtils.generateRandom(1000, 1000));
                        log.info("[syncData] - sync user label to redis when search from es, userId: {}, msgId: {}", userId, msgId);
                    }
                }
                return userLabel;
            }
            return null;
        });
    }

    private void clearNullLabelFromUserLabelHistory(UserLabel userLabelHistory) {
        if (!Objects.isNull(userLabelHistory) && !CollectionUtils.isEmpty(userLabelHistory.getUserLabels())) {
            userLabelHistory.getUserLabels().removeIf(Objects::isNull);
        }
    }

    private void distinctLabelFromUserLabelHistory(UserLabel userLabelHistory) {
        List<Label> labels = userLabelHistory.getUserLabels();
        labels = labels.stream().collect(Collectors.collectingAndThen(Collectors.toCollection(() -> new TreeSet<>(Comparator.comparing(Label::getLabelCode))), ArrayList::new));
        userLabelHistory.setUserLabels(labels);
    }

    private UserLabel parallelFindUserLabelByMultiDataSource(Long userId, boolean syncEsDataToRedis) {
        UserLabel userLabelHistory = this.findUserLabelByCacheAndEs(userId, true, syncEsDataToRedis);
        if (logSwitchConfigLoader.userLabelRepositoryPrintLogSwitchIsOpen()) {
            log.info("[parallelFindUserLabelByMultiDataSource] - load pre, userId: {}, user history label: {}.", userId, JSON.toJSONString(userLabelHistory));
        }

        this.clearNullLabelFromUserLabelHistory(userLabelHistory);

        if (Objects.isNull(userLabelHistory)) {
            userLabelHistory = new UserLabel(userId);
        }

        this.distinctLabelFromUserLabelHistory(userLabelHistory);

        if (!CollectionUtils.isEmpty(userLabelHistory.getLabelDimensions())) {
            userLabelHistory.getLabelDimensions().removeIf(labelDimension -> labelDimension.getDimensionKey().equals("@sequenceId@"));
            CompletionService<Pair<Long, List<LabelDimensionDataDO>>> executorCompletionService = new ExecutorCompletionService<>(executorService);
            List<Future<Pair<Long, List<LabelDimensionDataDO>>>> futures = new ArrayList<>();
            userLabelHistory.getLabelDimensions().stream().collect(Collectors.groupingBy(LabelDimension::getDimensionKeyId)).forEach((dimensionKeyId, labelDimensions) -> {
                if (CollectionUtils.isEmpty(labelDimensions)) {
                    return;
                }
                labelDimensions.forEach(labelDimension -> {
                    if (labelDimension.getDimensionKey().equals("@labelDimensionData@")) {
                        futures.add(executorCompletionService.submit(() -> Pair.of(labelDimension.getDimensionKeyId(), this.findLabelDimensionDataFromDb(userId, this.getDimensionKeyIdShardingNos(labelDimension.getDimensionKeyId(), labelDimension.getDimensionVal())))));
                    }
                });
            });
            userLabelHistory.getLabelDimensions().removeIf(ld -> ld.getDimensionKey().equals("@labelDimensionData@"));
            for (int i=0; i<futures.size(); i++) {
                Pair<Long, List<LabelDimensionDataDO>> pair = null;
                try {
                    pair = executorCompletionService.take().get();
                } catch (InterruptedException | ExecutionException e) {
                    log.error("[parallelFindUserLabelByMultiDataSource] - executorCompletionService exception.", e);
                }
                if (!Objects.isNull(pair) && !CollectionUtils.isEmpty(pair.getRight())) {
                    if (logSwitchConfigLoader.userLabelRepositoryPrintLogSwitchIsOpen()) {
                        log.info("[parallelFindUserLabelByMultiDataSource] - load, userId: {}, dimensionKeyId: {}, documents: {}.", userId, pair.getLeft(), JSON.toJSONString(pair.getRight()));
                    }
                    List<LabelDimension> labelDimensionList = new ArrayList<>();
                    pair.getRight().forEach(document -> labelDimensionList.addAll(JSON.parseArray(document.getLabelDimension(), LabelDimension.class)));
                    if (!CollectionUtils.isEmpty(labelDimensionList)) {
                        labelDimensionList.removeIf(ld -> ld.getDimensionKey().equals("@sequenceId@"));
                        labelDimensionList.removeIf(ld -> ld.getDimensionKey().equals("@labelDimensionData@"));
                        Iterator<LabelDimension> it = userLabelHistory.getLabelDimensions().iterator();
                        while (it.hasNext()) {
                            LabelDimension item = it.next();
                            if (item.getDimensionKeyId().equals(pair.getLeft())) {
                                it.remove();
                            }
                        }
                        userLabelHistory.getLabelDimensions().addAll(labelDimensionList);
                    }
                }
            }
        }

        if (logSwitchConfigLoader.userLabelRepositoryPrintLogSwitchIsOpen()) {
            log.info("[parallelFindUserLabelByMultiDataSource] - load after, userId: {}, user history label: {}.", userId, JSON.toJSONString(userLabelHistory));
        }
        return userLabelHistory;
    }

    private boolean findUserHasLabelByEs(Long userId, String labelCode) {
        Objects.requireNonNull(userId, "Null userId.");
        Objects.requireNonNull(labelCode, "Null labelCode.");
        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("userId", userId))
                .must(QueryBuilders.nestedQuery("userLabels",
                        QueryBuilders.boolQuery().must(QueryBuilders.termQuery("userLabels.labelCode", labelCode)),
                        ScoreMode.None));
        try {
            return this.countFromEs(queryBuilder, null) > 0;
        } catch (IOException e) {
            log.error("[userHasLabel] - es search exception.", e);
        }
        return false;
    }

    private UserLabel findUserLabelByEs(Long userId) {
        Objects.requireNonNull(userId, "Null userId.");

        QueryBuilder queryBuilder = QueryBuilders.boolQuery()
                .must(QueryBuilders.termQuery("userId", userId));

        try {
            SearchHits hits = this.searchFromEs(queryBuilder, null, 0, 1);
            if (logSwitchConfigLoader.userLabelRepositoryPrintLogSwitchIsOpen()) {
                log.info("[userLabel] - hits: {}", JSON.toJSONString(hits));
            }
            if (!Objects.isNull(hits) && hits.totalHits > 0) {
                return JSON.parseObject(JSON.toJSONString(hits.getHits()[0].getSourceAsMap()), UserLabel.class);
            }
        } catch (IOException e) {
            log.error("[userLabel] - es search exception.", e);
        }
        return null;
    }

    private Pair<SearchHits, Object[]> searchAfterFromEs(QueryBuilder query, QueryBuilder filter, Integer size, Object[] sortValues, String[] sourceIncludes, String[] sourceExcludes) throws IOException {
        log.debug("searchAfter, index={}, query:{}, filter:{}, size:{}", elasticsearchProperties.getIndex(), JSON.toJSONString(query), JSON.toJSONString(filter), size);
        SearchRequest request = new SearchRequest(elasticsearchProperties.getIndex());
        request.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(query)
                .postFilter(filter)
                .size(size)
                .sort("_id", SortOrder.ASC)
                .explain(true);
        builder.fetchSource(sourceIncludes, sourceExcludes);
        request.source(builder);
        if (!Objects.isNull(sortValues)) {
            builder.searchAfter(sortValues);
        }
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        if (logSwitchConfigLoader.userLabelRepositoryPrintLogSwitchIsOpen()) {
            log.info("[userLabels] - es search, request: {}, response: {}", JSON.toJSONString(request), JSON.toJSONString(response));
        }
        RestStatus status = response.status();
        if (status.equals(RestStatus.OK)) {
            SearchHits hits = response.getHits();
            if (Objects.isNull(hits) || Objects.isNull(hits.getHits()) || hits.getHits().length == 0) {
                return null;
            }
            return Pair.of(hits, hits.getHits()[hits.getHits().length - 1].getSortValues());
        } else {
            return null;
        }
    }

    private SearchHits searchFromEs(QueryBuilder query, QueryBuilder filter, Integer offset, Integer limit) throws IOException {
        log.debug("search, index={}, query:{}, filter:{}, offset:{}, limit:{}", elasticsearchProperties.getIndex(), JSON.toJSONString(query), JSON.toJSONString(filter), offset, limit);
        SearchRequest request = new SearchRequest(elasticsearchProperties.getIndex());
        request.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(query)
                .postFilter(filter)
                .from(offset)
                .size(limit)
                .explain(true);
        request.source(builder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        if (logSwitchConfigLoader.userLabelRepositoryPrintLogSwitchIsOpen()) {
            log.info("[userLabels] - es search, request: {}, response: {}", JSON.toJSONString(request), JSON.toJSONString(response));
        }
        RestStatus status = response.status();
        if (status.equals(RestStatus.OK)) {
            return response.getHits();
        } else {
            log.error("[userLabels] - es search failure, status: {}, request: {}", JSON.toJSONString(status), JSON.toJSONString(request));
            return null;
        }
    }

    private Long countFromEs(QueryBuilder query, QueryBuilder filter) throws IOException {
        SearchRequest request = new SearchRequest(elasticsearchProperties.getIndex());
        request.searchType(SearchType.DFS_QUERY_THEN_FETCH);
        SearchSourceBuilder builder = new SearchSourceBuilder();
        builder.query(query)
                .postFilter(filter)
                .size(0)
                .explain(true);
        request.source(builder);
        SearchResponse response = client.search(request, RequestOptions.DEFAULT);
        if (logSwitchConfigLoader.userLabelRepositoryPrintLogSwitchIsOpen()) {
            log.info("[userLabels] - es search, request: {}, response: {}", JSON.toJSONString(request), JSON.toJSONString(response));
        }
        RestStatus status = response.status();
        if (status.equals(RestStatus.OK)) {
            SearchHits hits = response.getHits();
            return hits.getTotalHits();
        } else {
            log.error("[userLabels] - es search failure, status: {}, request: {}", JSON.toJSONString(status), JSON.toJSONString(request));
            return 0L;
        }
    }

    private boolean isExistsFromEs(Long userId) {
        GetRequest getRequest=new GetRequest();
        getRequest.index(elasticsearchProperties.getIndex()).id(String.valueOf(userId));
        try {
            GetResponse response = client.get(getRequest, RequestOptions.DEFAULT);
            return response.isExists();
        } catch (IOException e) {
            log.error("[userLabels] - es get exception.", e);
        }
        return false;
    }

}
