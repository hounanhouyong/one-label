package com.hn.onelabel.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.alibaba.fastjson.TypeReference;
import com.aliyun.openservices.ons.api.Message;
import com.hn.onelabel.adapter.api.model.request.RefreshUserLabelRequest;
import com.hn.onelabel.api.enums.*;
import com.hn.onelabel.api.model.request.*;
import com.hn.onelabel.server.common.utils.RandomUtils;
import com.hn.onelabel.server.common.utils.SequenceIdUtils;
import com.hn.onelabel.server.domain.aggregate.labelrule.LabelRule;
import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleDefineLabel;
import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleEffectiveTime;
import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleScript;
import com.hn.onelabel.server.domain.aggregate.userlabel.UserLabel;
import com.hn.onelabel.server.domain.aggregate.userlabel.repository.LabelRepository;
import com.hn.onelabel.server.domain.aggregate.userlabel.repository.UserLabelRepository;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.Label;
import com.hn.onelabel.server.domain.service.UserLabelDomainService;
import com.hn.onelabel.server.infrastructure.cache.RedisCacheService;
import com.hn.onelabel.server.infrastructure.cache.RedisKey;
import com.hn.onelabel.server.infrastructure.cache.RedisLockService;
import com.hn.onelabel.server.infrastructure.db.repository.impl.LabelOperationRecordsRepository;
import com.hn.onelabel.server.infrastructure.db.repository.impl.LabelRuleRepository;
import com.hn.onelabel.server.infrastructure.mq.RocketMqProducer;
import com.hn.onelabel.server.infrastructure.nacos.*;
import com.hn.onelabel.server.infrastructure.rpc.UserDmpRpcService;
import com.hn.onelabel.server.service.UserLabelCommandService;
import lombok.extern.slf4j.Slf4j;
import ma.glasnost.orika.MapperFacade;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserLabelCommandServiceImpl implements UserLabelCommandService {

    @Autowired
    private LabelContextEntityMappingConfigLoader labelContextEntityMappingConfigLoader;
    @Autowired
    private LabelRuleContextConfigLoader labelRuleContextConfigLoader;
    @Autowired
    private LabelContextRuleMappingConfigLoader labelContextRuleMappingConfigLoader;

    @Autowired
    private UserLabelRepository userLabelRepository;
    @Autowired
    private LabelRepository labelRepository;
    @Autowired
    private LabelRuleRepository labelRuleRepository;
    @Autowired
    private LabelOperationRecordsRepository labelOperationRecordsRepository;

    @Autowired
    private UserLabelDomainService userLabelDomainService;

    @Autowired
    private UserDmpRpcService userDmpRpcService;

    @Autowired
    private RedisCacheService redisCacheService;
    @Autowired
    private RedisLockService redisLockService;

    @Autowired
    private MapperFacade mapperFacade;

    @Autowired
    private RocketMqProducer mqProducer;

    @Autowired
    private LabelDimensionConfigLoader labelDimensionConfigLoader;
    @Autowired
    private SwitchConfigLoader switchConfigLoader;
    @Autowired
    private MqProducerSwitchConfigLoader mqProducerSwitchConfigLoader;
    @Autowired
    private UserLruConfigLoader userLruConfigLoader;

    @Override
    public boolean labelOperation(String sequenceId, JSONObject context, Long ruleContextId) {
        Objects.requireNonNull(context, "Null ruleContext.");
        Objects.requireNonNull(ruleContextId, "Null ruleContextId.");

        // 1 entity
        Long userId = labelContextEntityMappingConfigLoader.getEntityId(ruleContextId, context);
        if (Objects.isNull(userId)) {
            log.info("[labelOperation] - sequenceId: {}, no entityId, context: {}, contextId: {}.", sequenceId, JSON.toJSONString(context), ruleContextId);
            return false;
        }

        // 2 preload context
        this.preloadContext(userId, context, labelRuleContextConfigLoader.getContextAttributes(ruleContextId, RuleContextAttributeLoadTypeEnum.PRE_LOAD.name()));

        // 3 rule hits
        List<LabelRule> hitsLabelRules = userLabelDomainService.hits(context, this.findLabelRules(ruleContextId));

        // 4 label operation
        return this.labelOperationHandler(sequenceId, userId, ruleContextId, context, hitsLabelRules);
    }

    private void preloadContext(Long userId, JSONObject context, List<Triple<String, String, String>> preLoadAttributeList) {
        if (CollectionUtils.isEmpty(preLoadAttributeList)) {
            return;
        }
        preLoadAttributeList.forEach(triple -> {
            if (Objects.requireNonNull(RuleContextAttributeLoadFromEnum.getByName(triple.getLeft())) == RuleContextAttributeLoadFromEnum.LABEL) {
                context.put(triple.getMiddle(), userLabelRepository.findUserHasLabel(userId, triple.getMiddle(), false));
            }
            if (Objects.requireNonNull(RuleContextAttributeLoadFromEnum.getByName(triple.getLeft())) == RuleContextAttributeLoadFromEnum.DMP) {
                switch (Objects.requireNonNull(RuleContextAttributeTypeEnum.getByCode(triple.getRight()))) {
                    case BOOLEAN:
                        context.put(triple.getMiddle(), userDmpRpcService.findAttributeValIsTrue(userId, triple.getMiddle()));
                        break;
                    case DATE:
                        context.put(triple.getMiddle(), userDmpRpcService.findAttributeVal4Date(userId, triple.getMiddle()));
                        break;
                    case INTEGER:
                        context.put(triple.getMiddle(), userDmpRpcService.findAttributeVal4Integer(userId, triple.getMiddle()));
                        break;
                    case LONG:
                        context.put(triple.getMiddle(), userDmpRpcService.findAttributeVal4Long(userId, triple.getMiddle()));
                        break;
                    case STRING:
                        context.put(triple.getMiddle(), userDmpRpcService.findAttributeVal4String(userId, triple.getMiddle()));
                        break;
                }
            }
        });
    }

    private List<LabelRule> findLabelRules(Long ruleContextId) {
        List<LabelRule> labelRules = new ArrayList<>();
        List<LabelRule> loadLabelRulesFromConfig = labelContextRuleMappingConfigLoader.getLabelRuleList(ruleContextId);
        if (!CollectionUtils.isEmpty(loadLabelRulesFromConfig)) {
            labelRules.addAll(loadLabelRulesFromConfig);
        }
        List<LabelRule> loadLabelRulesFromDb = labelRuleRepository.findLabelRules(ruleContextId, true);
        if (!CollectionUtils.isEmpty(loadLabelRulesFromDb)) {
            labelRules.addAll(loadLabelRulesFromDb);
        }
        return labelRules;
    }

    private boolean labelOperationHandler(String sequenceId, Long userId, Long ruleContextId, JSONObject context, List<LabelRule> hitsLabelRules) {
        if (CollectionUtils.isEmpty(hitsLabelRules)) {
            return true;
        }

        log.info("[labelOperation] - sequenceId: {}, userId: {}, hitsLabelRules: {}, contextId: {}, context: {}", sequenceId, userId, JSON.toJSONString(hitsLabelRules), ruleContextId, JSON.toJSONString(context));

        // hit label rules operation
        List<LabelRule> hitLabelRules = hitsLabelRules.stream().filter(labelRule -> !RuleTypeEnum.isLabelDimensionRuleType(labelRule.getRuleType())).collect(Collectors.toList());
        if (this.hitLabelRulesOperation(sequenceId, HitLabelRulesOperationRequest.builder()
                .userId(userId)
                .hitsLabelRules(hitLabelRules.stream()
                        .map(labelRule -> SaveLabelRuleInfoRequest.builder()
                                .ruleName(labelRule.getRuleName())
                                .ruleDesc(labelRule.getRuleDesc())
                                .ruleType(labelRule.getRuleType())
                                .ruleScriptType(labelRule.getRuleScript().getRuleScriptType())
                                .ruleScriptContent(labelRule.getRuleScript().getRuleScriptContent())
                                .ruleEffectiveStartTime(labelRule.getRuleEffectiveTime().getRuleEffectiveStartTime())
                                .ruleEffectiveEndTime(labelRule.getRuleEffectiveTime().getRuleEffectiveEndTime())
                                .ruleContextId(labelRule.getRuleContextId())
                                .ruleGroupId(labelRule.getRuleGroupId())
                                .labelCode(labelRule.getRuleDefineLabel().getLabelCode())
                                .labelDimensionKeyId(labelRule.getRuleDefineLabel().getLabelDimensionKeyId())
                                .labelDimensionKeyDefineType(labelRule.getRuleDefineLabel().getLabelDimensionKeyDefineType())
                                .labelDimensionFixedKey(labelRule.getRuleDefineLabel().getLabelDimensionFixedKey())
                                .externalTags(labelRule.getExternalTag())
                                .build()).collect(Collectors.toList()))
                .build())) {
            // save hit records
            labelRuleRepository.saveLabelHitRecords(sequenceId, userId, hitLabelRules, ruleContextId, JSON.toJSONString(context));
        }
        if (!CollectionUtils.isEmpty(hitLabelRules)) {
            // notification
            hitLabelRules.stream().map(labelRule -> LabelOperationNotificationRequest.builder()
                    .labelOperationType(Objects.requireNonNull(LabelOperationTypeEnum.getByName(labelRule.getRuleType())).name())
                    .labelGroupCode(Objects.requireNonNull(labelRepository.findByCode(labelRule.getRuleDefineLabel().getLabelCode())).getLabelGroupCode())
                    .labelCode(labelRule.getRuleDefineLabel().getLabelCode())
                    .userId(userId)
                    .extInfo(JSON.toJSONString(context))
                    .build()).collect(Collectors.toList()).stream().collect(Collectors.groupingBy(LabelOperationNotificationRequest::getLabelGroupCode)).forEach((tag, notificationRequestList) -> {
                if (mqProducerSwitchConfigLoader.tagProducerSwitchIsOpen(tag)) {
                    Message message = new Message();
                    message.setTopic("xxx");
                    message.setTag(tag);
                    String msgId = mqProducer.syncSend(message, notificationRequestList);
                    log.info("[labelOperation] - sequenceId: {}, userId: {}, send labelOperationNotification message, msgId: {}.", sequenceId, userId, msgId);
                }
            });
        }

        List<LabelRule> hitLabelDimensionRules = hitsLabelRules.stream().filter(labelRule -> RuleTypeEnum.isLabelDimensionRuleType(labelRule.getRuleType())).collect(Collectors.toList());
        if (!CollectionUtils.isEmpty(hitLabelDimensionRules)) {
            // labelDimensionOperation
            hitLabelDimensionRules.stream().map(labelRule -> Pair.of(labelRule.getRuleDefineLabel().getLabelCode(), this.buildLabelDimensionRequest(context, labelRule))).collect(Collectors.toList())
                    .stream().collect(Collectors.groupingBy(Pair::getLeft)).forEach((labelCode, pairs) -> {
                        Message message = new Message();
                        message.setTopic("xxx");
                        message.setTag("labelDimensionOperation");
                String msgId = mqProducer.syncSend(message, LabelDimensionOperationRequest.builder()
                        .userId(userId)
                        .labelCode(labelCode)
                        .labelDimensionRequestList(pairs.stream().map(Pair::getRight).collect(Collectors.toList()))
                        .build());
                log.info("[labelOperation] - sequenceId: {}, userId: {}, send labelDimensionOperation message, msgId: {}.", sequenceId, userId, msgId);
            });
            // notification
            hitLabelDimensionRules.stream().map(labelRule -> LabelDimensionOperationNotificationRequest.builder()
                    .labelDimensionOperationType(Objects.requireNonNull(LabelDimensionOperationTypeEnum.getByName(labelRule.getRuleType())).name())
                    .labelGroupCode(Objects.requireNonNull(labelRepository.findByCode(labelRule.getRuleDefineLabel().getLabelCode())).getLabelGroupCode())
                    .labelCode(labelRule.getRuleDefineLabel().getLabelCode())
                    .labelDimensionKeyId(labelRule.getRuleDefineLabel().getLabelDimensionKeyId())
                    .labelDimensionKey(labelDimensionConfigLoader.buildDimensionKeyIdAndDimensionKey(labelRule.getRuleDefineLabel().getLabelCode(), this.buildLabelDimensionRequest(context, labelRule).getLabelDimensionKeyRequest()).getRight())
                    .tags(!StringUtils.isEmpty(labelRule.getExternalTag()) ? Arrays.asList(labelRule.getExternalTag().split(",")) : null)
                    .userId(userId)
                    .extInfo(JSON.toJSONString(context))
                    .build()).collect(Collectors.toList()).stream().collect(Collectors.groupingBy(LabelDimensionOperationNotificationRequest::getLabelGroupCode)).forEach((tag, notificationRequestList) -> {
                if (mqProducerSwitchConfigLoader.tagProducerSwitchIsOpen(tag)) {
                    Message message = new Message();
                    message.setTopic("xxx");
                    message.setTag(tag);
                    String msgId = mqProducer.syncSend(message, notificationRequestList);
                    log.info("[labelOperation] - sequenceId: {}, userId: {}, send labelDimensionOperationNotification message, msgId: {}.", sequenceId, userId, msgId);
                }
            });
        }

        return true;
    }

    private LabelDimensionRequest buildLabelDimensionRequest(JSONObject context, LabelRule labelRule) {
        if (LabelDimensionKeyDefineTypeEnum.DYNAMIC_KEY.name().equals(labelRule.getRuleDefineLabel().getLabelDimensionKeyDefineType())) {
            return LabelDimensionRequest.builder()
                    .labelDimensionKeyRequest(LabelDimensionKeyRequest.builder()
                            .dimensionKeyDefineType(LabelDimensionKeyDefineTypeEnum.DYNAMIC_KEY.name())
                            .dimensionKeyId(labelRule.getRuleDefineLabel().getLabelDimensionKeyId())
                            .dimensionDynamicKeyParams(JSONObject.parseObject(context.toJSONString(), new TypeReference<Map<String, String>>(){}))
                            .build())
                    .dimensionVal(JSON.toJSONString(context))
                    .build();
        } else {
            return LabelDimensionRequest.builder()
                    .labelDimensionKeyRequest(LabelDimensionKeyRequest.builder()
                            .dimensionKeyDefineType(LabelDimensionKeyDefineTypeEnum.DYNAMIC_KEY.name())
                            .dimensionKeyId(labelRule.getRuleDefineLabel().getLabelDimensionKeyId())
                            .dimensionDynamicKeyParams(Collections.singletonMap("labelDimensionFixedKey", labelRule.getRuleDefineLabel().getLabelDimensionFixedKey()))
                            .build())
                    .dimensionVal(String.valueOf(labelRule.getRuleId()))
                    .build();
        }
    }

    @Override
    public boolean labelOperation(String sequenceId, Long userId, LabelOperationTypeEnum labelOperationTypeEnum, String labelCode, boolean used) {
        Objects.requireNonNull(labelCode, "Null labelCode.");

        String currentThreadId = String.valueOf(Thread.currentThread().getId());

        if (switchConfigLoader.lockLabelDimensionOperationSwitchIsOpen()) {
            if (!redisLockService.tryLock(RedisKey.getLockUserRedisKey(userId), currentThreadId, 1L)) {
                Message message = new Message();
                message.setTopic("xxx");
                message.setTag("labelOperation");
                String msgId = mqProducer.delaySend(message, UserLabelOperationRequest.builder()
                        .userId(userId)
                        .labelCode(labelCode)
                        .operationType(labelOperationTypeEnum.name())
                        .build(), RandomUtils.generateRandom(500, 1000));
                log.info("[labelOperation] - lock failure, sequenceId: {}, userId: {}, labelCode: {}, msgId: {}", sequenceId, userId, labelCode, msgId);
                return true;
            }
        }

        // 1 user label
        UserLabel userLabelHistory = userLabelRepository.findUserLabel(userId, used, false);

        List<Label> finalHistoryLabels = new ArrayList<>();
        if (!CollectionUtils.isEmpty(userLabelHistory.getUserLabels())) {
            finalHistoryLabels = mapperFacade.mapAsList(userLabelHistory.getUserLabels(), Label.class);
        }

        Label addLabel = Objects.requireNonNull(labelRepository.findByCode(labelCode), "No define labelCode.");
        UserLabel userLabel = userLabelDomainService.labelOperation(userId, userLabelHistory.getUserLabels(), userLabelHistory.getLabelDimensions(), labelOperationTypeEnum, labelCode, addLabel);

        // 2 user label repository
        userLabelRepository.saveUserLabel(userLabel);

        // 3 save operation records
        labelOperationRecordsRepository.saveLabelOperationRecords(SequenceIdUtils.generateSequenceId(), userId, labelCode, labelOperationTypeEnum, addLabel, finalHistoryLabels, userLabel.getUserLabels());

        if (switchConfigLoader.lockLabelDimensionOperationSwitchIsOpen()) {
            redisLockService.releaseLock(RedisKey.getLockUserRedisKey(userId), currentThreadId);
        }

        return true;
    }

    @Override
    public boolean hitLabelRulesOperation(String sequenceId, HitLabelRulesOperationRequest request) {
        if (CollectionUtils.isEmpty(request.getHitsLabelRules())) {
            return true;
        }

        List<LabelRule> hitsLabelRules = request.getHitsLabelRules().stream().map(rule -> new LabelRule(rule.getRuleContextId(), rule.getRuleGroupId(), rule.getRuleId(), rule.getRuleName(), rule.getRuleDesc(), rule.getRuleType(),
                new RuleScript(rule.getRuleScriptType(), rule.getRuleScriptContent()),
                new RuleEffectiveTime(rule.getRuleEffectiveStartTime(), rule.getRuleEffectiveEndTime()),
                new RuleDefineLabel(rule.getLabelCode(), rule.getLabelDimensionKeyId(), rule.getLabelDimensionKeyDefineType(), rule.getLabelDimensionFixedKey()),
                rule.getExternalTags())).collect(Collectors.toList());

        String currentThreadId = String.valueOf(Thread.currentThread().getId());
        if (switchConfigLoader.lockLabelDimensionOperationSwitchIsOpen()) {
            if (!redisLockService.tryLock(RedisKey.getLockUserRedisKey(request.getUserId()), currentThreadId, 1L)) {
                Message message = new Message();
                message.setTopic("xxx");
                message.setTag("hitLabelRulesOperation");
                String msgId = mqProducer.delaySend(message, request, RandomUtils.generateRandom(500, 1000));
                log.info("[labelOperation] - [hitLabelRulesOperation] - lock failure, sequenceId: {}, userId: {}, msgId: {}", sequenceId, request.getUserId(), msgId);
                return false;
            }
        }

        log.info("[labelOperation] - [hitLabelRulesOperation] - sequenceId: {}, userId: {}, hits labelCode: {}", sequenceId, request.getUserId(), JSON.toJSONString(request.getHitsLabelRules().stream().map(SaveLabelRuleInfoRequest::getLabelCode).distinct().collect(Collectors.toList())));

        // 5 user label
        UserLabel userLabelHistory = userLabelRepository.findUserLabel(request.getUserId(), true, false);

        // 6 user label repository
        userLabelRepository.saveUserLabel(userLabelDomainService.labelOperation(request.getUserId(), userLabelHistory.getUserLabels(), userLabelHistory.getLabelDimensions(), hitsLabelRules,
                hitsLabelRules.stream().filter(labelRule -> labelRule.getRuleType().equals(RuleTypeEnum.ADD_LABEL.name()))
                        .collect(Collectors.toMap(
                                labelRule -> labelRule.getRuleDefineLabel().getLabelCode(),
                                labelRule -> Objects.requireNonNull(labelRepository.findByCode(labelRule.getRuleDefineLabel().getLabelCode()), "No define labelCode.")
                        ))));

        if (switchConfigLoader.lockLabelDimensionOperationSwitchIsOpen()) {
            redisLockService.releaseLock(RedisKey.getLockUserRedisKey(request.getUserId()), currentThreadId);
        }

        return true;
    }

    @Override
    public void clear(List<Long> userIds) {
        userLabelRepository.clearUserLabel(userIds);
    }

    @Override
    public void clear(String labelCode, int start, int end) {
        log.info("[clear] - begin, labelCode: {}, [{}, {}]", labelCode, start, end);
        int total = this.clearLabelHandler(labelCode, start, end);
        log.info("[clear] - end, labelCode: {}, [{}, {}], total: {}.", labelCode, start, end, total);
    }

    @Override
    public void clearLru(int start, int end) {
        log.info("[clearLru] - begin, [{}, {}]", start, end);
        AtomicInteger total = new AtomicInteger();
        AtomicInteger totalRetainedUsers = new AtomicInteger();
        RedisKey.getAllUserLabelsRedisHashKey(start, end).forEach(redisKey -> {
            if (redisCacheService.exists(redisKey)) {
                List<Object> list = redisCacheService.hVals(redisKey);
                if (CollectionUtils.isEmpty(list)) {
                    return;
                }
                List<Long> userIdList = new ArrayList<>();
                list.forEach(val -> {
                    UserLabel userLabel = JSON.parseObject((String) val, UserLabel.class);
                    if (Objects.isNull(userLabel)) {
                        return;
                    }
                    if (redisCacheService.exists(RedisKey.getUserLruRedisKey(userLabel.getUserId()))) {
                        totalRetainedUsers.addAndGet(1);
                        return;
                    }
                    if (userLabel.getUserLabels().stream().noneMatch(label -> userLruConfigLoader.noClearUserIfTheLabelsExists().contains(label.getLabelCode()))) {
                        userIdList.add(userLabel.getUserId());
                    }
                });
                if (!CollectionUtils.isEmpty(userIdList)) {
                    log.info("[clearLru] - clear, userIdList: {}", JSON.toJSONString(userIdList));
                    userIdList.forEach(userId -> redisCacheService.hDel(RedisKey.getUserLabelsRedisHashKey(userId), String.valueOf(userId)));
                    total.addAndGet(userIdList.size());
                }
            }
        });
        log.info("[clearLru] - number of retained users is {}", totalRetainedUsers.get());
        log.info("[clearLru] - end, [{}, {}], total: {}.", start, end, total.get());
    }

    private int clearLabelHandler(String labelCode, int start, int end) {
        AtomicInteger total = new AtomicInteger();
        RedisKey.getAllUserLabelsRedisHashKey(start, end).forEach(redisKey -> {
            if (!redisCacheService.exists(redisKey)) {
                return;
            }
            List<Object> list = redisCacheService.hVals(redisKey);
            if (CollectionUtils.isEmpty(list)) {
                return;
            }
            list.forEach(val -> {
                UserLabel userLabel = JSON.parseObject((String) val, UserLabel.class);
                if (Objects.isNull(userLabel)) {
                    return;
                }
                if (userLabel.getUserLabels().stream().noneMatch(label -> label.getLabelCode().equals(labelCode))) {
                    return;
                }
                if (!switchConfigLoader.clearLabelSwitchIsOpen()) {
                    return;
                }
                log.info("[clearLabelHandler] - clear, userId: {}, labelCode: {}", userLabel.getUserId(), labelCode);
                this.labelOperation(SequenceIdUtils.generateSequenceId(), userLabel.getUserId(), LabelOperationTypeEnum.DELETE_LABEL, labelCode, false);
                total.addAndGet(1);
            });
        });
        return total.get();
    }

    @Override
    public void refreshUserLabelsData(int start, int end) {
        log.info("[refreshUserLabelsData] - begin, [{}, {}]", start, end);
        int total = this.refreshUserLabelsHandler(start, end);
        log.info("[refreshUserLabelsData] - end, [{}, {}], total: {}.", start, end, total);
    }

    private int refreshUserLabelsHandler(int start, int end) {
        AtomicInteger total = new AtomicInteger();
        RedisKey.getAllUserLabelsRedisHashKey(start, end).forEach(redisKey -> {
            if (redisCacheService.exists(redisKey)) {
                List<Object> list = redisCacheService.hVals(redisKey);
                if (!CollectionUtils.isEmpty(list)) {
                    list.forEach(val -> {
                        UserLabel userLabel = JSON.parseObject((String) val, UserLabel.class);
                        if (!Objects.isNull(userLabel)) {
                            this.refreshUserLabel(SequenceIdUtils.generateSequenceId(), userLabel.getUserId());
                        }
                    });
                    total.addAndGet(list.size());
                }
            }
        });
        return total.get();
    }

    @Override
    public void refreshUserLabel(String sequenceId, Long userId) {
        Objects.requireNonNull(userId, "Null userId.");

        if (!switchConfigLoader.refreshUserLabelSwitchIsOpen()) {
            return;
        }

        log.info("[refreshUserLabel] - sequenceId: {}, userId: {}", sequenceId, userId);

        String currentThreadId = String.valueOf(Thread.currentThread().getId());

        if (switchConfigLoader.lockLabelDimensionOperationSwitchIsOpen()) {
            if (!redisLockService.tryLock(RedisKey.getLockUserRedisKey(userId), currentThreadId, 1L)) {
                Message message = new Message();
                message.setTopic("xxx");
                message.setTag("refreshUserLabel");
                String msgId = mqProducer.delaySend(message, RefreshUserLabelRequest.builder()
                        .userId(userId)
                        .build(), RandomUtils.generateRandom(500, 1000));
                log.info("[refreshUserLabel] - lock failure, sequenceId: {}, userId: {}, msgId: {}", sequenceId, userId, msgId);
                return;
            }
        }

        // 1 user label
        UserLabel userLabelHistory = userLabelRepository.findUserLabel(userId, false, false);

        // 2 user label repository
        userLabelRepository.saveUserLabel(userLabelHistory);

        if (switchConfigLoader.lockLabelDimensionOperationSwitchIsOpen()) {
            redisLockService.releaseLock(RedisKey.getLockUserRedisKey(userId), currentThreadId);
        }
    }

    @Override
    public void syncUserLabel(String sequenceId, Long userId) {
        Objects.requireNonNull(userId, "Null userId.");
        log.info("[syncUserLabel] - sequenceId: {}, userId: {}", sequenceId, userId);
        if (redisCacheService.exists(RedisKey.getLockUserRedisKey(userId))) {
            log.info("[syncUserLabel] - sequenceId: {}, userId: {}, exists lock user key.", sequenceId, userId);
            return;
        }
        String currentThreadId = String.valueOf(Thread.currentThread().getId());
        if (!redisLockService.tryLock(RedisKey.getLockUserSyncRedisKey(userId), currentThreadId, 1L)) {
            log.info("[syncUserLabel]- lock failure, sequenceId: {}, userId: {}", sequenceId, userId);
            return;
        }
        // sync user label
        userLabelRepository.syncUserLabel(userId);
        redisLockService.releaseLock(RedisKey.getLockUserSyncRedisKey(userId), currentThreadId);
    }

}
