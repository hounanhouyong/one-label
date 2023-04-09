package com.hn.onelabel.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.aliyun.openservices.ons.api.Message;
import com.hn.onelabel.api.enums.LabelDimensionKeyDefineTypeEnum;
import com.hn.onelabel.api.enums.LabelDimensionOperationTypeEnum;
import com.hn.onelabel.api.model.request.*;
import com.hn.onelabel.server.common.exception.LockOneLabelException;
import com.hn.onelabel.server.common.utils.LocalDateTimeUtils;
import com.hn.onelabel.server.common.utils.RandomUtils;
import com.hn.onelabel.server.common.utils.SequenceIdUtils;
import com.hn.onelabel.server.domain.aggregate.userlabel.UserLabel;
import com.hn.onelabel.server.domain.aggregate.userlabel.repository.LabelRepository;
import com.hn.onelabel.server.domain.aggregate.userlabel.repository.UserLabelRepository;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.Label;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.LabelDimension;
import com.hn.onelabel.server.domain.service.UserLabelDomainService;
import com.hn.onelabel.server.infrastructure.cache.RedisCacheService;
import com.hn.onelabel.server.infrastructure.cache.RedisKey;
import com.hn.onelabel.server.infrastructure.cache.RedisLockService;
import com.hn.onelabel.server.infrastructure.cache.RedisVal;
import com.hn.onelabel.server.infrastructure.db.repository.impl.LabelOperationRecordsRepository;
import com.hn.onelabel.server.infrastructure.mq.RocketMqProducer;
import com.hn.onelabel.server.infrastructure.nacos.LabelDimensionConfigLoader;
import com.hn.onelabel.server.infrastructure.nacos.LogSwitchConfigLoader;
import com.hn.onelabel.server.infrastructure.nacos.SwitchConfigLoader;
import com.hn.onelabel.server.service.LabelDimensionCommandService;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LabelDimensionCommandServiceImpl implements LabelDimensionCommandService {

    @Autowired
    private UserLabelRepository userLabelRepository;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private LabelOperationRecordsRepository labelOperationRecordsRepository;
    @Autowired
    private UserLabelDomainService userLabelDomainService;

    @Autowired
    private RedisCacheService redisCacheService;
    @Autowired
    private RedisLockService redisLockService;

    @Autowired
    private LabelDimensionConfigLoader labelDimensionConfigLoader;
    @Autowired
    private SwitchConfigLoader switchConfigLoader;
    @Autowired
    private LogSwitchConfigLoader logSwitchConfigLoader;

    @Autowired
    private MapperFacade mapperFacade;

    @Autowired
    private RocketMqProducer mqProducer;

    @Retryable(value = {LockOneLabelException.class}, maxAttempts = 5, backoff = @Backoff(delay = 300, multiplier = 1.5, maxDelay = 600))
    @Override
    public void labelDimensionOperation(String sequenceId, Long userId, String labelCode, List<LabelDimensionRequest> labelDimensionRequestList) throws LockOneLabelException {
        Objects.requireNonNull(userId, "Null userId.");
        Objects.requireNonNull(labelCode, "Null labelCode.");

        Assert.isTrue(!CollectionUtils.isEmpty(labelDimensionRequestList), "Null labelDimensionRequestList.");

        if (logSwitchConfigLoader.labelDimensionCommandServicePrintLogSwitchIsOpen()) {
            log.info("[labelDimensionOperation] - sequenceId: {}, userId: {}, labelCode: {}, request: {}.", sequenceId, userId, labelCode, JSON.toJSONString(labelDimensionRequestList));
        }

        // 1 check
        this.checkLabelAndDimensionKeyIsValid(labelCode, labelDimensionRequestList.stream().map(LabelDimensionRequest::getLabelDimensionKeyRequest).collect(Collectors.toList()));

        String currentThreadId = String.valueOf(Thread.currentThread().getId());

        if (switchConfigLoader.lockLabelDimensionOperationSwitchIsOpen()) {
            if (!redisLockService.tryLock(RedisKey.getLockUserRedisKey(userId), currentThreadId, 1L)) {
                if (switchConfigLoader.retryableLabelDimensionOperationSwitchIsOpen()) {
                    throw new LockOneLabelException("sequenceId=" + sequenceId + ", userId=" + userId + ", user label is locked by another, please retry.");
                } else {
                    Message message = new Message();
                    message.setTopic("xxx");
                    message.setTag("labelDimensionOperation");
                    String msgId = mqProducer.delaySend(message, LabelDimensionOperationRequest.builder()
                            .userId(userId)
                            .labelCode(labelCode)
                            .labelDimensionRequestList(labelDimensionRequestList)
                            .build(), RandomUtils.generateRandom(500, 1000));
                    log.info("[labelDimensionOperation] - lock failure, sequenceId: {}, userId: {}, labelCode: {}, msgId: {}", sequenceId, userId, labelCode, msgId);
                    return;
                }
            }
        }

        // 2 user label history
        UserLabel userLabelHistory = this.buildUserLabelHistory(userId, labelCode, true, true);

        // 3 add label dimensions
        List<LabelDimension> addLabelDimensions = labelDimensionRequestList.stream().map(request -> {
            Pair<Long, String> pair = labelDimensionConfigLoader.buildDimensionKeyIdAndDimensionKey(labelCode, request.getLabelDimensionKeyRequest());
            return new LabelDimension(labelCode, pair.getLeft(), pair.getRight(), request.getDimensionVal());
        }).collect(Collectors.toList());
        if (logSwitchConfigLoader.labelDimensionCommandServicePrintLogSwitchIsOpen()) {
            log.info("[labelDimensionOperation] - sequenceId: {}, userId: {}, labelCode: {}, add label dimensions: {}.", sequenceId, userId, labelCode, JSON.toJSONString(addLabelDimensions));
        }

        // 4 user label
        this.saveUserLabel(userId, labelCode, userLabelHistory, addLabelDimensions, null);

        if (switchConfigLoader.lockLabelDimensionOperationSwitchIsOpen()) {
            redisLockService.releaseLock(RedisKey.getLockUserRedisKey(userId), currentThreadId);
        }
    }

    @Recover
    public void recover(LockOneLabelException e) {
        log.info("this is recover, {}.", e.getMessage());
    }

    @Retryable(value = {LockOneLabelException.class}, maxAttempts = 5, backoff = @Backoff(delay = 300, multiplier = 1.5, maxDelay = 600))
    @Override
    public void labelDimensionIncr(String sequenceId, Long userId, String labelCode, LabelDimensionKeyRequest labelDimensionKeyRequest, Integer increaseVal) {
        Objects.requireNonNull(userId, "Null userId.");
        Objects.requireNonNull(labelCode, "Null labelCode.");
        Objects.requireNonNull(labelDimensionKeyRequest, "Null labelDimensionKeyRequest.");

        if (Objects.isNull(increaseVal)) {
            increaseVal = 0;
        }

        // 1 check
        this.checkLabelAndDimensionKeyIsValid(labelCode, Collections.singletonList(labelDimensionKeyRequest));

        Pair<Long, String> pair = labelDimensionConfigLoader.buildDimensionKeyIdAndDimensionKey(labelCode, labelDimensionKeyRequest);

        if (labelDimensionConfigLoader.enableDimensionHotKey(labelCode, labelDimensionKeyRequest.getDimensionKeyId())) {
            LabelDimension labelDimension = userLabelRepository.findUserLabelDimension(userId, labelCode, pair.getLeft(), pair.getRight(), true);
            userLabelRepository.saveUserLabelDimensionHotKey(
                    userId,
                    labelCode,
                    pair.getLeft(),
                    pair.getRight(),
                    !Objects.isNull(labelDimension) && !Objects.isNull(labelDimension.getDimensionVal()) ? String.valueOf(Integer.parseInt(labelDimension.getDimensionVal()) + increaseVal) : String.valueOf(increaseVal),
                    LocalDateTimeUtils.localDateTime2Long2Second(labelDimensionConfigLoader.getInvalidTime(labelCode, pair.getLeft())));
            return;
        }

        String currentThreadId = String.valueOf(Thread.currentThread().getId());

        if (switchConfigLoader.lockLabelDimensionOperationSwitchIsOpen()) {
            if (!redisLockService.tryLock(RedisKey.getLockUserRedisKey(userId), currentThreadId, 1L)) {
                if (switchConfigLoader.retryableLabelDimensionOperationSwitchIsOpen()) {
                    throw new RuntimeException("sequenceId=" + sequenceId + ", userId=" + userId + ", user label is locked, please retry.");
                } else {
                    Message message = new Message();
                    message.setTopic("xxx");
                    message.setTag("labelDimensionIncr");
                    String msgId = mqProducer.delaySend(message, LabelDimensionIncrRequest.builder()
                            .userId(userId)
                            .labelCode(labelCode)
                            .labelDimensionKeyRequest(labelDimensionKeyRequest)
                            .increaseVal(increaseVal)
                            .build(), RandomUtils.generateRandom(500, 1000));
                    log.info("[labelDimensionIncr] - lock failure, sequenceId: {}, userId: {}, labelCode: {}, msgId: {}", sequenceId, userId, labelCode, msgId);
                    return;
                }
            }
        }

        // 2 user label history
        UserLabel userLabelHistory = this.buildUserLabelHistory(userId, labelCode, true, true);

        // 3 add label dimensions
        List<LabelDimension> addLabelDimensions = new ArrayList<>();
        AtomicReference<Integer> dimensionVal = new AtomicReference<>(increaseVal);
        Integer finalIncreaseVal = increaseVal;
        userLabelHistory.getLabelDimensions().forEach(labelDimension -> {
            if (labelDimension.getLabelCode().equals(labelCode) && labelDimension.getDimensionKey().equals(pair.getRight())) {
                dimensionVal.set(Integer.parseInt(labelDimension.getDimensionVal()) + finalIncreaseVal);
            }
        });
        addLabelDimensions.add(new LabelDimension(labelCode, pair.getLeft(), pair.getRight(), String.valueOf(dimensionVal.get())));
        if (logSwitchConfigLoader.labelDimensionCommandServicePrintLogSwitchIsOpen()) {
            log.info("[labelDimensionIncr] - sequenceId: {}, userId: {}, labelCode: {} add label dimensions: {}.", sequenceId, userId, labelCode, JSON.toJSONString(addLabelDimensions));
        }

        // 4 user label
        this.saveUserLabel(userId, labelCode, userLabelHistory, addLabelDimensions, null);

        if (switchConfigLoader.lockLabelDimensionOperationSwitchIsOpen()) {
            redisLockService.releaseLock(RedisKey.getLockUserRedisKey(userId), currentThreadId);
        }
    }

    private void checkLabelAndDimensionKeyIsValid(String labelCode, List<LabelDimensionKeyRequest> requestList) {
        Assert.isTrue(labelDimensionConfigLoader.checkLabelCodeIsValid(labelCode), String.format("No define labelCode, labelCode: %s", labelCode));
        requestList.forEach(request -> {
            switch (Objects.requireNonNull(LabelDimensionKeyDefineTypeEnum.getByName(request.getDimensionKeyDefineType()))) {
                case FIXED_KEY:
                    Assert.isTrue(labelDimensionConfigLoader.checkDimensionFixedKeyIsValid(labelCode, request.getDimensionFixedKey()), String.format("Invalid dimensionFixedKey: %s, dimensionKeyId: %s", request.getDimensionFixedKey(), request.getDimensionKeyId()));
                    break;
                case DYNAMIC_KEY:
                    Assert.isTrue(labelDimensionConfigLoader.checkDimensionDynamicKeyIsValid(labelCode, request.getDimensionKeyId(), request.getDimensionDynamicKeyParams()), String.format("Invalid dimensionDynamicKey, dimensionKeyId: %s, dataMap: %s", request.getDimensionKeyId(), JSON.toJSONString(request.getDimensionDynamicKeyParams())));
                    break;
            }
        });
    }

    private UserLabel buildUserLabelHistory(Long userId, String labelCode, boolean addIfNoExist, boolean used) {
        UserLabel userLabelHistory = userLabelRepository.findUserLabel(userId, used, false);
        if (addIfNoExist && (Objects.isNull(userLabelHistory) || userLabelHistory.getUserLabels().stream().noneMatch(label -> label.getLabelCode().equals(labelCode)))) {
            log.info("[labelDimensionOperation] - [buildUserLabelHistory] - No user label, add label, userId: {}, labelCode: {}", userId, labelCode);
            if (Objects.isNull(userLabelHistory)) {
                userLabelHistory = new UserLabel(userId);
            }
            Label label = Objects.requireNonNull(labelRepository.findByCode(labelCode), "No define labelCode.");
            label.setCreateTime(new Date());
            userLabelHistory.getUserLabels().add(label);
        }
        return userLabelHistory;
    }

    private void saveUserLabel(Long userId, String labelCode, UserLabel userLabelHistory, List<LabelDimension> addLabelDimensions, List<LabelDimension> delLabelDimensions) {
        List<LabelDimension> finalAddLabelDimensions = new ArrayList<>();
        if (!CollectionUtils.isEmpty(addLabelDimensions)) {
            finalAddLabelDimensions = mapperFacade.mapAsList(addLabelDimensions, LabelDimension.class);
        }
        List<LabelDimension> finalDelLabelDimensions = new ArrayList<>();
        if (!CollectionUtils.isEmpty(delLabelDimensions)) {
            finalDelLabelDimensions = mapperFacade.mapAsList(delLabelDimensions, LabelDimension.class);
        }
        List<LabelDimension> finalHistoryLabelDimensions = new ArrayList<>();
        if (!Objects.isNull(userLabelHistory) && !CollectionUtils.isEmpty(userLabelHistory.getLabelDimensions())) {
            finalHistoryLabelDimensions = mapperFacade.mapAsList(userLabelHistory.getLabelDimensions(), LabelDimension.class);
        }
        // 1 user label
        Pair<String, List<LabelDimension>> addLabelDimensionPair = !CollectionUtils.isEmpty(addLabelDimensions) ? Pair.of(labelCode, addLabelDimensions) : null;
        Pair<String, List<LabelDimension>> delLabelDimensionPair = !CollectionUtils.isEmpty(delLabelDimensions) ? Pair.of(labelCode, delLabelDimensions) : null;
        Pair<UserLabel, List<LabelDimension>> pair = userLabelDomainService.labelDimensionOperation(userId, userLabelHistory.getUserLabels(), userLabelHistory.getLabelDimensions(), addLabelDimensionPair, delLabelDimensionPair);
        // 2 user label repository
        userLabelRepository.saveUserLabel(pair.getLeft());
        // 3 user label dimensions invalid
        this.saveLabelDimensionInvalid(userId, labelCode, pair.getRight());
        // 4 user label dimension operation records
        labelOperationRecordsRepository.saveLabelDimensionOperationRecords(SequenceIdUtils.generateSequenceId(), userId, labelCode, LabelDimensionOperationTypeEnum.ADD_LABEL_DIMENSION, finalAddLabelDimensions, finalHistoryLabelDimensions, pair.getLeft().getLabelDimensions());
        labelOperationRecordsRepository.saveLabelDimensionOperationRecords(SequenceIdUtils.generateSequenceId(), userId, labelCode, LabelDimensionOperationTypeEnum.DELETE_LABEL_DIMENSION, finalDelLabelDimensions, finalHistoryLabelDimensions, pair.getLeft().getLabelDimensions());
    }

    @Override
    public void clearLabelDimensionInvalidData(LocalDateTime invalidDate, long stepSize) {
        List<String> labelCodeList = labelDimensionConfigLoader.getLabelCodeList();
        if (CollectionUtils.isEmpty(labelCodeList)) {
            return;
        }
        log.info("[clearLabelDimensionInvalidData] - labelCode: {}, invalidDate: {}, stepSize: {}", JSON.toJSONString(labelCodeList), LocalDateTimeUtils.getFormatDateString(invalidDate, "yyyyMMdd"), stepSize);
        int total = this.labelDimensionInvalidHandler(labelCodeList, invalidDate, stepSize);
        log.info("[clearLabelDimensionInvalidData] - end, total: {}.", total);
    }

    @Override
    public void deleteLabelDimensionOperation(String sequenceId, String labelCode, List<Triple<Long, Long, String>> userDimensionKeyList) {
        if (CollectionUtils.isEmpty(userDimensionKeyList)) {
            return;
        }
        Map<Long, List<Triple<Long, Long, String>>> map = userDimensionKeyList.stream().collect(Collectors.groupingBy(Triple::getLeft));
        map.forEach((userId, list) -> {
            if (!CollectionUtils.isEmpty(list)) {
                this.deleteLabelDimensionOperation(sequenceId, userId, labelCode, list.stream().map(Triple::getRight).collect(Collectors.toList()));
            }
        });
    }

    @Override
    public void deleteLabelDimensionOperation(String sequenceId, Long userId, String labelCode, List<String> dimensionKeyList) {
        if (CollectionUtils.isEmpty(dimensionKeyList)) {
            return;
        }

        String currentThreadId = String.valueOf(Thread.currentThread().getId());

        if (switchConfigLoader.lockLabelDimensionOperationSwitchIsOpen()) {
            if (!redisLockService.tryLock(RedisKey.getLockUserRedisKey(userId), currentThreadId, 1L)) {
                Message message = new Message();
                message.setTopic("xxx");
                message.setTag("deleteLabelDimensionOperation");
                String msgId = mqProducer.delaySend(message, DeleteLabelDimensionOperationRequest.builder()
                        .userId(userId)
                        .labelCode(labelCode)
                        .labelDimensionKeyList(dimensionKeyList)
                        .build(), RandomUtils.generateRandom(500, 1000));
                log.info("[deleteLabelDimensionOperation] - lock failure, sequenceId: {}, userId: {}, labelCode: {}, msgId: {}.", sequenceId, userId, labelCode, msgId);
                return;
            }
        }

        // 1 user label history
        UserLabel userLabelHistory = this.buildUserLabelHistory(userId, labelCode, false, false);
        if (Objects.isNull(userLabelHistory) || userLabelHistory.getUserLabels().stream().noneMatch(label -> label.getLabelCode().equals(labelCode))) {
            log.info("[deleteLabelDimensionOperation] - sequenceId: {}, No user label, userId: {}, labelCode: {}", sequenceId, userId, labelCode);
            return;
        }
        if (logSwitchConfigLoader.labelDimensionCommandServicePrintLogSwitchIsOpen()) {
            log.info("[deleteLabelDimensionOperation] - sequenceId: {}, userId: {}, labelCode: {}, user history label: {}", sequenceId, userId, labelCode, JSON.toJSONString(userLabelHistory));
        }
        // 2 delete label dimensions
        List<LabelDimension> deleteLabelDimensions = dimensionKeyList.stream().map(dimensionKey -> new LabelDimension(labelCode, null, dimensionKey, null)).collect(Collectors.toList());
        if (logSwitchConfigLoader.labelDimensionCommandServicePrintLogSwitchIsOpen()) {
            log.info("[deleteLabelDimensionOperation] - sequenceId: {}, userId: {}, labelCode: {}, delete label dimensions: {}", sequenceId, userId, labelCode, JSON.toJSONString(deleteLabelDimensions));
        }
        // 3 user label
        this.saveUserLabel(userId, labelCode, userLabelHistory, null, deleteLabelDimensions);

        if (switchConfigLoader.lockLabelDimensionOperationSwitchIsOpen()) {
            redisLockService.releaseLock(RedisKey.getLockUserRedisKey(userId), currentThreadId);
        }
    }

    private int labelDimensionInvalidHandler(List<String> labelCodeList, LocalDateTime invalidDate, long stepSize) {
        if (CollectionUtils.isEmpty(labelCodeList)) {
            return 0;
        }
        AtomicInteger total = new AtomicInteger();
        labelCodeList.forEach(labelCode -> RedisKey.getAllUserLabelDimensionInvalidRedisKey(labelCode).forEach(redisKey -> {
            if (redisCacheService.exists(redisKey)) {
                Set<ZSetOperations.TypedTuple<String>> invalidRedisValSet = redisCacheService.zRevRangeByScoreWithScores(
                        redisKey,
                        LocalDateTimeUtils.localDateTime2Long2Second(LocalDateTime.of(invalidDate.toLocalDate(), LocalTime.MIN)),
                        LocalDateTimeUtils.localDateTime2Long2Second(LocalDateTime.of(invalidDate.plusDays(1).toLocalDate(), LocalTime.MIN)),
                        0L,
                        stepSize
                );
                if (!CollectionUtils.isEmpty(invalidRedisValSet)) {
                    List<Triple<Long, Long, String>> userDimensionKeyList = invalidRedisValSet.stream().map(tuple -> {
                        String val = tuple.getValue();
                        return Triple.of(
                                RedisVal.getUserIdFromUldInvalidRedisVal(val),
                                RedisVal.getDimensionKeyIdFromUldInvalidRedisVal(val),
                                RedisVal.getDimensionKeyFromUldInvalidRedisVal(val)
                        );
                    }).collect(Collectors.toList());
                    this.deleteLabelDimensionOperation(SequenceIdUtils.generateSequenceId(), labelCode, userDimensionKeyList);
                    redisCacheService.zRem(redisKey, invalidRedisValSet.stream().map(ZSetOperations.TypedTuple::getValue).toArray(String[]::new));
                    total.addAndGet(userDimensionKeyList.size());
                }
            }
        }));
        return total.get();
    }

    private void saveLabelDimensionInvalid(Long userId, String labelCode, List<LabelDimension> newLabelDimensions) {
        if (CollectionUtils.isEmpty(newLabelDimensions)) {
            return;
        }
        newLabelDimensions.forEach(ld -> {
            if (labelDimensionConfigLoader.invalidDimensionKeyIsEnabled(labelCode, ld.getDimensionKeyId())) {
                if (logSwitchConfigLoader.labelDimensionCommandServicePrintLogSwitchIsOpen()) {
                    log.info("[saveLabelDimensionInvalid] - userId: {}, labelCode: {}, redisKey: {}, redisVal: {}.", userId, labelCode, RedisKey.getUserLabelDimensionsInvalidRedisKey(userId, labelCode), RedisVal.getUserLabelDimensionsInvalidRedisVal(userId, ld.getDimensionKeyId(), ld.getDimensionKey()));
                }
                redisCacheService.zAdd(
                        RedisKey.getUserLabelDimensionsInvalidRedisKey(userId, labelCode),
                        RedisVal.getUserLabelDimensionsInvalidRedisVal(userId, ld.getDimensionKeyId(), ld.getDimensionKey()),
                        LocalDateTimeUtils.localDateTime2Long2Second(labelDimensionConfigLoader.getInvalidTime(labelCode, ld.getDimensionKeyId()))
                );
            }
        });
    }

}
