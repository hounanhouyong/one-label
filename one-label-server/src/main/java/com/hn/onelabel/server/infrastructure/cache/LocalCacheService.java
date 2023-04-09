package com.hn.onelabel.server.infrastructure.cache;

import com.alibaba.fastjson.JSON;
import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.LoadingCache;
import com.hn.onelabel.adapter.api.enums.SegmentTypeEnum;
import com.hn.onelabel.api.enums.RuleStatusEnum;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.Label;
import com.hn.onelabel.server.infrastructure.db.*;
import com.hn.onelabel.server.infrastructure.db.mapper.*;
import com.hn.onelabel.server.infrastructure.db.query.LabelGroupQuery;
import com.hn.onelabel.server.infrastructure.db.query.LabelInfoQuery;
import com.hn.onelabel.server.infrastructure.db.query.UserAbExperimentInfoQuery;
import com.hn.onelabel.server.infrastructure.db.query.UserSegmentRuleInfoQuery;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

@Slf4j
@Component
public class LocalCacheService {

    @Autowired
    private LabelInfoMapper labelInfoMapper;
    @Autowired
    private LabelGroupMapper labelGroupMapper;
    @Autowired
    private LabelClassificationMapper labelClassificationMapper;
    @Autowired
    private UserSegmentRuleInfoMapper userSegmentRuleInfoMapper;
    @Autowired
    private UserAbExperimentInfoMapper userAbExperimentInfoMapper;

    private final ExecutorService executorService = Executors.newFixedThreadPool(3);

    private final LoadingCache<String, String> cache_label = Caffeine.newBuilder()
            .initialCapacity(100)
            .expireAfterAccess(300, TimeUnit.SECONDS)
//            .expireAfterWrite(60, TimeUnit.SECONDS)
            .maximumSize(1024)
            .refreshAfterWrite(60, TimeUnit.SECONDS)
            .removalListener(((key, value, cause) -> log.info("[LocalCacheService] - [cache_label] - removalListener, k: {}, v: {}, cause: {}", key, value, cause))).build(key -> {
                log.info("[LocalCacheService] - [cache_label] - cacheLoader, k: {}", key);
                LabelInfoQuery labelDynamicQuery = new LabelInfoQuery();
                labelDynamicQuery.setLabelCode(key);
                List<LabelInfoDO> labelInfos = labelInfoMapper.selectByCondition(labelDynamicQuery);
                if (!CollectionUtils.isEmpty(labelInfos)) {
                    LabelInfoDO labelInfo = labelInfos.get(0);
                    String primaryClassificationCode = null;
                    String secondaryClassificationCode = null;
                    LabelGroupQuery labelGroupDynamicQuery = new LabelGroupQuery();
                    labelGroupDynamicQuery.setLabelGroupCode(labelInfo.getLabelGroupCode());
                    List<LabelGroupDO> labelGroups = labelGroupMapper.selectByCondition(labelGroupDynamicQuery);
                    if (!CollectionUtils.isEmpty(labelGroups)) {
                        LabelGroupDO labelGroup = labelGroups.get(0);
                        LabelClassificationDO labelClassification = labelClassificationMapper.selectByPrimaryKey(labelGroup.getLabelClassificationId());
                        if (!Objects.isNull(labelClassification)) {
                            if (labelClassification.getClassificationLevel() == 1) {
                                primaryClassificationCode = labelClassification.getClassificationCode();
                            } else if (labelClassification.getClassificationLevel() == 2) {
                                secondaryClassificationCode = labelClassification.getClassificationCode();
                                LabelClassificationDO primaryClassification = labelClassificationMapper.selectByPrimaryKey(labelClassification.getClassificationParentId());
                                if (!Objects.isNull(primaryClassification)) {
                                    primaryClassificationCode = primaryClassification.getClassificationCode();
                                }
                            }
                        }
                    }
                    return JSON.toJSONString(Label.builder()
                            .primaryClassification(primaryClassificationCode)
                            .secondaryClassification(secondaryClassificationCode)
                            .labelGroupCode(labelInfo.getLabelGroupCode())
                            .labelCode(labelInfo.getLabelCode())
                            .createTime(labelInfo.getCreateTime())
                            .invalidTime(labelInfo.getLabelInvalidTime())
                            .build());
                }
                return null;
            });


    private final LoadingCache<String, String> cache_user_segment_rule_4_promotion_plan = Caffeine.newBuilder()
            .initialCapacity(100)
            .expireAfterAccess(600, TimeUnit.SECONDS)
//            .expireAfterWrite(60, TimeUnit.SECONDS)
            .maximumSize(1024)
            .refreshAfterWrite(450, TimeUnit.SECONDS)
            .removalListener(((key, value, cause) -> log.info("[LocalCacheService] - [cache_user_segment_rule_4_promotion_plan] - removalListener, k: {}, v: {}, cause: {}", key, value, cause))).build(key -> {
                log.info("[LocalCacheService] - [cache_user_segment_rule_4_promotion_plan] - cacheLoader, k: {}", key);
                UserSegmentRuleInfoQuery dynamicQuery = new UserSegmentRuleInfoQuery();
                dynamicQuery.setSegmentType(SegmentTypeEnum.PROMOTION_PLAN.name());
                dynamicQuery.setExternalExperimentGroupId(key);
                dynamicQuery.setStatus(RuleStatusEnum.ENABLED.name());
                List<UserSegmentRuleInfoDO> segmentRuleInfoList = userSegmentRuleInfoMapper.selectByCondition(dynamicQuery);
                if (!CollectionUtils.isEmpty(segmentRuleInfoList)) {
                    return JSON.toJSONString(segmentRuleInfoList);
                }
                return JSON.toJSONString(new ArrayList<>());
            });

    private final LoadingCache<Long, String> cache_user_ab_experiment = Caffeine.newBuilder()
            .initialCapacity(100)
            .expireAfterAccess(600, TimeUnit.SECONDS)
//            .expireAfterWrite(60, TimeUnit.SECONDS)
            .maximumSize(1024)
            .refreshAfterWrite(450, TimeUnit.SECONDS)
            .removalListener(((key, value, cause) -> log.info("[LocalCacheService] - [cache_user_ab_experiment] - removalListener, k: {}, v: {}, cause: {}", key, value, cause))).build(key -> {
                log.info("[LocalCacheService] - [cache_user_ab_experiment] - cacheLoader, k: {}", key);
                UserAbExperimentInfoQuery abDynamicQuery = new UserAbExperimentInfoQuery();
                abDynamicQuery.setSegmentRuleId(key);
                List<UserAbExperimentInfoDO> userAbExperimentInfoList = userAbExperimentInfoMapper.selectByCondition(abDynamicQuery);
                if (!CollectionUtils.isEmpty(userAbExperimentInfoList)) {
                    return JSON.toJSONString(userAbExperimentInfoList);
                }
                return JSON.toJSONString(new ArrayList<>());
            });


    public Label findLabelByLabelCode(String labelCode) {
        Objects.requireNonNull(labelCode, "Null labelCode.");
        String jsonString = cache_label.get(labelCode);
        if (StringUtils.isEmpty(jsonString)) {
            return null;
        }
        return JSON.parseObject(jsonString, Label.class);
    }

    public List<UserSegmentRuleInfoDO> findUserSegmentRules4PromotionPlan(List<String> externalExperimentGroupIdList) {
        Objects.requireNonNull(externalExperimentGroupIdList, "Null externalExperimentGroupIdList.");
        log.info("[findUserSegmentRules4PromotionPlan] - [cache_user_segment_rule_4_promotion_plan] - get, externalExperimentIdList: {}", JSON.toJSONString(externalExperimentGroupIdList));
        CompletionService<List<UserSegmentRuleInfoDO>> executorCompletionService = new ExecutorCompletionService<>(executorService);
        List<Future<List<UserSegmentRuleInfoDO>>> futures = new ArrayList<>();
        externalExperimentGroupIdList.forEach(externalExperimentGroupId -> futures.add(executorCompletionService.submit(() -> {
            String jsonString = cache_user_segment_rule_4_promotion_plan.get(externalExperimentGroupId);
            return !StringUtils.isEmpty(jsonString) ? JSON.parseArray(jsonString, UserSegmentRuleInfoDO.class) : new ArrayList<>();
        })));
        List<UserSegmentRuleInfoDO> response = new ArrayList<>();
        for (int i=0; i<futures.size(); i++) {
            List<UserSegmentRuleInfoDO> userSegmentRuleInfos;
            try {
                userSegmentRuleInfos = executorCompletionService.take().get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("[findUserSegmentRules4PromotionPlan] - executorCompletionService exception.", e);
                continue;
            }
            response.addAll(userSegmentRuleInfos);
        }
        return response;
    }

    public void invalidUserSegmentRuleCache4PromotionPlan(String externalExperimentGroupId) {
        Objects.requireNonNull(externalExperimentGroupId, "Null externalExperimentGroupId.");
        log.info("[invalidUserSegmentRuleCache4PromotionPlan] - [cache_user_segment_rule_4_promotion_plan] - invalid, externalExperimentGroupId: {}", externalExperimentGroupId);
        cache_user_segment_rule_4_promotion_plan.invalidate(externalExperimentGroupId);
    }

    public List<UserAbExperimentInfoDO> findUserAbExperimentsBySegmentRuleId(Long segmentRuleId) {
        Objects.requireNonNull(segmentRuleId, "Null segmentRuleId.");
        log.info("[findUserAbExperimentsBySegmentRuleId] - [cache_user_ab_experiment] - get, segmentRuleId: {}", segmentRuleId);
        String jsonString = cache_user_ab_experiment.get(segmentRuleId);
        if (StringUtils.isEmpty(jsonString)) {
            return new ArrayList<>();
        }
        return JSON.parseArray(jsonString, UserAbExperimentInfoDO.class);
    }

    public void invalidUserAbExperimentCache(Long segmentRuleId) {
        Objects.requireNonNull(segmentRuleId, "Null segmentRuleId.");
        log.info("[invalidUserAbExperimentCache] - [cache_user_ab_experiment] - invalid, segmentRuleId: {}", segmentRuleId);
        cache_user_ab_experiment.invalidate(segmentRuleId);
    }

}
