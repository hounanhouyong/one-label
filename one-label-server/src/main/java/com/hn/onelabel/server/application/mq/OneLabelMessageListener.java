package com.hn.onelabel.server.application.mq;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.shade.com.alibaba.rocketmq.common.utils.MessageUtils;
import com.hn.onelabel.adapter.api.enums.CouponChangeEventTypeEnum;
import com.hn.onelabel.adapter.api.enums.CouponUsageStatusEnum;
import com.hn.onelabel.adapter.api.enums.SegmentSceneEnum;
import com.hn.onelabel.adapter.api.enums.TouchTypeEnum;
import com.hn.onelabel.adapter.api.model.request.*;
import com.hn.onelabel.api.enums.LabelDimensionKeyDefineTypeEnum;
import com.hn.onelabel.api.enums.LabelOperationTypeEnum;
import com.hn.onelabel.api.model.request.*;
import com.hn.onelabel.server.common.utils.SequenceIdUtils;
import com.hn.onelabel.server.infrastructure.mq.RocketMqProducer;
import com.hn.onelabel.server.infrastructure.nacos.*;
import com.hn.onelabel.server.service.LabelDimensionCommandService;
import com.hn.onelabel.server.service.LabelDimensionQueryService;
import com.hn.onelabel.server.service.UserLabelCommandService;
import com.hn.onelabel.server.service.UserLabelQueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.aliyun.openservices.ons.api.Action;
import com.aliyun.openservices.ons.api.ConsumeContext;
import com.aliyun.openservices.ons.api.Message;
import com.aliyun.openservices.ons.api.MessageListener;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

/**
 * group:
 * topic:
 * tag:
 *                 "hitLabelRulesOperation",
 *                 "labelOperation",
 *                 "labelDimensionOperation",
 *                 "deleteLabelDimensionOperation",
 *                 "labelDimensionIncr",
 *                 "refreshUserLabel",
 *                 "syncUserLabel",
 *                 "reportPromotionPlanTouchResult",
 *                 "saveUserSegment",
 *                 "syncUserCoupons",
 *                 "syncUserCouponChangeEvent"
 */

@Slf4j
public class OneLabelMessageListener implements MessageListener {

    @Autowired
    private UserLabelQueryService userLabelQueryService;
    @Autowired
    private UserLabelCommandService userLabelCommandService;
    @Autowired
    private LabelDimensionQueryService labelDimensionQueryService;
    @Autowired
    private LabelDimensionCommandService labelDimensionCommandService;

    @Autowired
    private SwitchConfigLoader switchConfigLoader;
    @Autowired
    private MqConsumerSwitchConfigLoader mqConsumerSwitchConfigLoader;
    @Autowired
    private AdaptationPromotionPlanTouchCommonConfigLoader adaptationPromotionPlanTouchCommonConfigLoader;
    @Autowired
    private AdaptationWelfareCenterCommonConfigLoader adaptationWelfareCenterCommonConfigLoader;
    @Autowired
    private AdaptationCouponCommonConfigLoader adaptationCouponCommonConfigLoader;

    @Autowired
    private RocketMqProducer mqProducer;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Override
    public Action consume(Message message, ConsumeContext context) {
        log.info("[OneLabelMessageListener] - msgId: {}, topic: {}, tag: {}", message.getMsgID(), message.getTopic(), message.getTag());
        if (!switchConfigLoader.userLabelMessageListenerSwitchIsOpen()) {
            log.info("[OneLabelMessageListener] - switch is close. switch: enableUserLabelMessageListener.");
            return Action.CommitMessage;
        }

        switch (message.getTag()) {
            case "hitLabelRulesOperation":
                HitLabelRulesOperationRequest hitLabelRulesOperationRequest = this.buildHitLabelRulesOperationRequest(message);
                if (!Objects.isNull(hitLabelRulesOperationRequest)) {
                    this.hitLabelRulesOperationHandler(message.getMsgID(), hitLabelRulesOperationRequest);
                }
                break;
            case "labelOperation":
                UserLabelOperationRequest userLabelOperationRequest = this.buildUserLabelOperationRequest(message);
                if (!Objects.isNull(userLabelOperationRequest)) {
                    this.userLabelOperationHandler(message.getMsgID(), userLabelOperationRequest);
                }
                break;
            case "labelDimensionOperation":
                LabelDimensionOperationRequest labelDimensionOperationRequest = this.buildLabelDimensionOperationRequest(message);
                if (!Objects.isNull(labelDimensionOperationRequest)) {
                    this.labelDimensionOperationHandler(message.getMsgID(), labelDimensionOperationRequest);
                }
                break;
            case "deleteLabelDimensionOperation":
                DeleteLabelDimensionOperationRequest deleteLabelDimensionOperationRequest = this.buildDeleteLabelDimensionOperationRequest(message);
                if (!Objects.isNull(deleteLabelDimensionOperationRequest)) {
                    this.deleteLabelDimensionOperationHandler(message.getMsgID(), deleteLabelDimensionOperationRequest);
                }
                break;
            case "labelDimensionIncr":
                LabelDimensionIncrRequest labelDimensionIncrRequest = this.buildLabelDimensionIncrRequest(message);
                if (!Objects.isNull(labelDimensionIncrRequest)) {
                    this.labelDimensionIncrHandler(message.getMsgID(), labelDimensionIncrRequest);
                }
                break;
            case "refreshUserLabel":
                RefreshUserLabelRequest refreshUserLabelRequest = this.buildRefreshUserLabelRequest(message);
                if (!Objects.isNull(refreshUserLabelRequest)) {
                    this.refreshUserLabelHandler(message.getMsgID(), refreshUserLabelRequest);
                }
                break;
            case "syncUserLabel":
                SyncUserLabelRequest syncUserLabelRequest = this.buildSyncUserLabelRequest(message);
                if (!Objects.isNull(syncUserLabelRequest)) {
                    this.syncUserLabelHandler(message.getMsgID(), syncUserLabelRequest);
                }
                break;
            case "reportPromotionPlanTouchResult":
                if (!mqConsumerSwitchConfigLoader.tagConsumerSwitchIsOpen("reportPromotionPlanTouchResult")) {
                    break;
                }
                ReportPromotionPlanTouchResultRequest reportPromotionPlanTouchResultRequest = this.buildReportPromotionPlanTouchResultRequest(message);
                if (!Objects.isNull(reportPromotionPlanTouchResultRequest)) {
                    this.reportPromotionPlanTouchResultHandler(message.getMsgID(), reportPromotionPlanTouchResultRequest);
                }
                break;
            case "saveUserSegment":
                if (!mqConsumerSwitchConfigLoader.tagConsumerSwitchIsOpen("saveUserSegment")) {
                    break;
                }
                SaveUserSegmentRequest saveUserSegmentRequest = this.buildSaveUserSegmentRequest(message);
                if (!Objects.isNull(saveUserSegmentRequest)) {
                    this.saveUserSegmentHandler(message.getMsgID(), saveUserSegmentRequest);
                }
                break;
            case "syncUserCoupons":
                if (!mqConsumerSwitchConfigLoader.tagConsumerSwitchIsOpen("syncUserCoupons")) {
                    break;
                }
                SyncUserCouponsRequest syncUserCouponsRequest = this.buildSyncUserCouponsRequest(message);
                if (!Objects.isNull(syncUserCouponsRequest)) {
                    this.syncUserCouponsHandler(message.getMsgID(), syncUserCouponsRequest);
                }
                break;
            case "syncUserCouponChangeEvent":
                if (!mqConsumerSwitchConfigLoader.tagConsumerSwitchIsOpen("syncUserCouponChangeEvent")) {
                    break;
                }
                SyncUserCouponChangeEventRequest syncUserCouponChangeEventRequest = this.buildSyncUserCouponChangeEventRequest(message);
                if (!Objects.isNull(syncUserCouponChangeEventRequest)) {
                    this.syncUserCouponChangeEventHandler(message.getMsgID(), syncUserCouponChangeEventRequest);
                }
                break;
            default:
                log.info("[OneLabelMessageListener] - No handler, msgId: {}, tag: {}.", message.getMsgID(), message.getTag());
        }

        return Action.CommitMessage;
    }

    private HitLabelRulesOperationRequest buildHitLabelRulesOperationRequest(Message message) {
        HitLabelRulesOperationRequest request = null;
        try {
            request = JSON.parseObject(message.getBody(), HitLabelRulesOperationRequest.class);
        } catch (Exception e) {
            log.error("[OneLabelMessageListener] - parse message body exception, msgId: {}", message.getMsgID(), e);
        }
        return request;
    }

    private void hitLabelRulesOperationHandler(String msgId, HitLabelRulesOperationRequest request) {
        log.info("[hitLabelRulesOperationHandler] - msgId: {}, userId: {}, request: {}", msgId, request.getUserId(), JSON.toJSONString(request));
        userLabelCommandService.hitLabelRulesOperation(msgId, request);
    }

    private UserLabelOperationRequest buildUserLabelOperationRequest(Message message) {
        UserLabelOperationRequest request = null;
        try {
            request = JSON.parseObject(message.getBody(), UserLabelOperationRequest.class);
        } catch (Exception e) {
            log.error("[OneLabelMessageListener] - parse message body exception, msgId: {}", message.getMsgID(), e);
        }
        return request;
    }

    private void userLabelOperationHandler(String msgId, UserLabelOperationRequest request) {
        log.info("[OneLabelMessageListener] - msgId: {}, userId: {}, labelCode: {}, request: {}", msgId, request.getUserId(), request.getLabelCode(), JSON.toJSONString(request));
        userLabelCommandService.labelOperation(msgId, request.getUserId(), Objects.requireNonNull(LabelOperationTypeEnum.getByName(request.getOperationType())), request.getLabelCode(), true);
    }

    private LabelDimensionOperationRequest buildLabelDimensionOperationRequest(Message message) {
        LabelDimensionOperationRequest request = null;
        try {
            request = JSON.parseObject(message.getBody(), LabelDimensionOperationRequest.class);
        } catch (Exception e) {
            log.error("[OneLabelMessageListener] - parse message body exception, msgId: {}", message.getMsgID(), e);
        }
        return request;
    }

    private void labelDimensionOperationHandler(String msgId, LabelDimensionOperationRequest request) {
        log.info("[labelDimensionOperationHandler] - msgId: {}, userId: {}, labelCode: {}, request: {}", msgId, request.getUserId(), request.getLabelCode(), JSON.toJSONString(request));
        labelDimensionCommandService.labelDimensionOperation(msgId, request.getUserId(), request.getLabelCode(), request.getLabelDimensionRequestList());
    }

    private DeleteLabelDimensionOperationRequest buildDeleteLabelDimensionOperationRequest(Message message) {
        DeleteLabelDimensionOperationRequest request = null;
        try {
            request = JSON.parseObject(message.getBody(), DeleteLabelDimensionOperationRequest.class);
        } catch (Exception e) {
            log.error("[OneLabelMessageListener] - parse message body exception, msgId: {}", message.getMsgID(), e);
        }
        return request;
    }

    private void deleteLabelDimensionOperationHandler(String msgId, DeleteLabelDimensionOperationRequest request) {
        log.info("[deleteLabelDimensionOperationHandler] - msgId: {}, userId: {}, labelCode: {}, request: {}", msgId, request.getUserId(), request.getLabelCode(), JSON.toJSONString(request));
        labelDimensionCommandService.deleteLabelDimensionOperation(msgId, request.getUserId(), request.getLabelCode(), request.getLabelDimensionKeyList());
    }

    private LabelDimensionIncrRequest buildLabelDimensionIncrRequest(Message message) {
        LabelDimensionIncrRequest request = null;
        try {
            request = JSON.parseObject(message.getBody(), LabelDimensionIncrRequest.class);
        } catch (Exception e) {
            log.error("[OneLabelMessageListener] - parse message body exception, msgId: {}", message.getMsgID(), e);
        }
        return request;
    }

    private void labelDimensionIncrHandler(String msgId, LabelDimensionIncrRequest request) {
        log.info("[labelDimensionIncrHandler] - msgId: {}, userId: {}, labelCode: {}, request: {}", msgId, request.getUserId(), request.getLabelCode(), JSON.toJSONString(request));
        labelDimensionCommandService.labelDimensionIncr(msgId, request.getUserId(), request.getLabelCode(), request.getLabelDimensionKeyRequest(), request.getIncreaseVal());
    }

    private RefreshUserLabelRequest buildRefreshUserLabelRequest(Message message) {
        RefreshUserLabelRequest request = null;
        try {
            request = JSON.parseObject(message.getBody(), RefreshUserLabelRequest.class);
        } catch (Exception e) {
            log.error("[OneLabelMessageListener] - parse message body exception, msgId: {}", message.getMsgID(), e);
        }
        return request;
    }

    private void refreshUserLabelHandler(String msgId, RefreshUserLabelRequest request) {
        log.info("[refreshUserLabelHandler] - msgId: {}, userId: {}, request: {}", msgId, request.getUserId(), JSON.toJSONString(request));
        userLabelCommandService.refreshUserLabel(msgId, request.getUserId());
    }

    private SyncUserLabelRequest buildSyncUserLabelRequest(Message message) {
        SyncUserLabelRequest request = null;
        try {
            request = JSON.parseObject(message.getBody(), SyncUserLabelRequest.class);
        } catch (Exception e) {
            log.error("[OneLabelMessageListener] - parse message body exception, msgId: {}", message.getMsgID(), e);
        }
        return request;
    }

    private void syncUserLabelHandler(String msgId, SyncUserLabelRequest request) {
        log.info("[syncUserLabelHandler] - msgId: {}, userId: {}, request: {}", msgId, request.getUserId(), JSON.toJSONString(request));
        userLabelCommandService.syncUserLabel(msgId, request.getUserId());
    }

    private ReportPromotionPlanTouchResultRequest buildReportPromotionPlanTouchResultRequest(Message message) {
        ReportPromotionPlanTouchResultRequest request = null;
        try {
            request = JSON.parseObject(message.getBody(), ReportPromotionPlanTouchResultRequest.class);
        } catch (Exception e) {
            log.error("[OneLabelMessageListener] - parse message body exception, msgId: {}", message.getMsgID(), e);
        }
        return request;
    }

    private void reportPromotionPlanTouchResultHandler(String msgId, ReportPromotionPlanTouchResultRequest request) {
        if (StringUtils.isEmpty(request.getBizJson())) {
            return;
        }
        JSONObject bizJsonObject;
        try {
            bizJsonObject = JSON.parseObject(request.getBizJson());
        } catch (Exception e) {
            log.info("[reportPromotionPlanTouchResultHandler] - JSON.parseObject failure, request: {}.", JSON.toJSONString(request));
            return;
        }

        log.info("[reportPromotionPlanTouchResultHandler] - msgId: {}, request: {}.", msgId, JSON.toJSONString(request));

        Assert.isTrue(bizJsonObject.containsKey("planId") && bizJsonObject.containsKey("experimentId"), "Null planId or experimentId.");
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("planId", bizJsonObject.getString("planId"));
        dataMap.put("experimentId", bizJsonObject.getString("experimentId"));

        String labelCode = Objects.requireNonNull(TouchTypeEnum.getByName(request.getTouchType())).getLabelCode();

        CompletionService<Pair<Long, String>> executorCompletionService = new ExecutorCompletionService<>(executorService);
        List<Future<Pair<Long, String>>> futures = new ArrayList<>();

        Objects.requireNonNull(request.getUserTouchStatusList()).forEach(userTouchStatus -> futures.add(executorCompletionService.submit(() -> {
            String touchMsgId = null;
            LabelDimensionRequest labelDimensionRequest = LabelDimensionRequest.builder()
                    .labelDimensionKeyRequest(LabelDimensionKeyRequest.builder()
                            .dimensionKeyDefineType(LabelDimensionKeyDefineTypeEnum.DYNAMIC_KEY.name())
                            .dimensionKeyId(adaptationPromotionPlanTouchCommonConfigLoader.getLabelDimensionKeyId(labelCode))
                            .dimensionDynamicKeyParams(dataMap)
                            .build())
                    .dimensionVal(userTouchStatus.getStatusCode() >= 0 ? String.valueOf(userTouchStatus.getStatusCode()) : "_" + (-1) * userTouchStatus.getStatusCode())
                    .build();
            if (adaptationPromotionPlanTouchCommonConfigLoader.reportPromotionPlanTouchResultSyncSwitchIsOpen()) {
                labelDimensionCommandService.labelDimensionOperation(SequenceIdUtils.generateSequenceId(), userTouchStatus.getUserId(), labelCode, Collections.singletonList(labelDimensionRequest));
            } else {
                Message message = new Message();
                message.setTopic("xxx");
                message.setTag("labelDimensionOperation");
                touchMsgId = mqProducer.syncSend(message, LabelDimensionOperationRequest.builder()
                        .userId(userTouchStatus.getUserId())
                        .labelCode(labelCode)
                        .labelDimensionRequestList(Collections.singletonList(labelDimensionRequest))
                        .build());
            }
            return Pair.of(userTouchStatus.getUserId(), touchMsgId);
        })));

        for (int i=0; i<futures.size(); i++) {
            try {
                Pair<Long, String> pair = executorCompletionService.take().get();
                log.info("[reportPromotionPlanTouchResultHandler] parent msgId: {}, distribution, userId: {}, messageId: {}.", msgId, pair.getLeft(), pair.getRight());
            } catch (InterruptedException | ExecutionException e) {
                log.error("[reportPromotionPlanTouchResultHandler] - executorCompletionService exception.", e);
            }
        }
    }

    private SaveUserSegmentRequest buildSaveUserSegmentRequest(Message message) {
        SaveUserSegmentRequest request = null;
        try {
            request = JSON.parseObject(message.getBody(), SaveUserSegmentRequest.class);
        } catch (Exception e) {
            log.error("[OneLabelMessageListener] - parse message body exception, msgId: {}", message.getMsgID(), e);
        }
        return request;
    }

    private void saveUserSegmentHandler(String msgId, SaveUserSegmentRequest request) {
        Assert.isTrue(!CollectionUtils.isEmpty(request.getSaveExperimentInfoRequestList()), "Null saveExperimentInfoRequestList.");
        String labelCode = Objects.requireNonNull(SegmentSceneEnum.getByName(request.getSegmentScene())).getLabelCode();

        log.info("[saveUserSegmentHandler] - msgId: {}, request: {}.", msgId, JSON.toJSONString(request));

        CompletionService<Pair<Long, String>> executorCompletionService = new ExecutorCompletionService<>(executorService);
        List<Future<Pair<Long, String>>> futures = new ArrayList<>();

        Objects.requireNonNull(request.getUserIdList()).forEach(userIdStr -> futures.add(executorCompletionService.submit(() -> {
            Long userId = Long.parseLong(userIdStr);
            String welfareCenterMsgId = null;
            if (CollectionUtils.isEmpty(labelDimensionQueryService.findLabelDimensions(FindUserLabelDimensionsRequest.builder()
                    .userId(userId)
                    .labelCode(labelCode)
                    .build(), true, false).stream().filter(ld -> ld.getDimensionKey().split("#")[0].equals(request.getExperimentGroupId())).collect(Collectors.toList()))) {

                Map<String, String> dataMap = new HashMap<>();
                dataMap.put("experimentGroupId", request.getExperimentGroupId());
                dataMap.put("experimentId", request.getSaveExperimentInfoRequestList().get(0).getExperimentCode());

                Message message = new Message();
                message.setTopic("xxx");
                message.setTag("labelDimensionOperation");
                welfareCenterMsgId = mqProducer.syncSend(message, LabelDimensionOperationRequest.builder()
                        .userId(userId)
                        .labelCode(labelCode)
                        .labelDimensionRequestList(Collections.singletonList(LabelDimensionRequest.builder()
                                .labelDimensionKeyRequest(LabelDimensionKeyRequest.builder()
                                        .dimensionKeyDefineType(LabelDimensionKeyDefineTypeEnum.DYNAMIC_KEY.name())
                                        .dimensionKeyId(adaptationWelfareCenterCommonConfigLoader.getLabelDimensionKeyId(labelCode))
                                        .dimensionDynamicKeyParams(dataMap)
                                        .build())
                                .dimensionVal(CollectionUtils.isEmpty(request.getSaveExperimentInfoRequestList()) ? null : JSON.toJSONString(request.getSaveExperimentInfoRequestList().get(0).getSaveExperimentExtInfoRequestList()))
                                .build()))
                        .build());
            }
            return Pair.of(userId, welfareCenterMsgId);
        })));

        for (int i=0; i<futures.size(); i++) {
            try {
                Pair<Long, String> pair = executorCompletionService.take().get();
                log.info("[saveUserSegmentHandler] - parent msgId: {}, async, userId: {}, labelCode: {}, msgId: {}", msgId, pair.getLeft(), labelCode, pair.getRight());
            } catch (InterruptedException | ExecutionException e) {
                log.error("[saveUserSegmentHandler] - executorCompletionService exception.", e);
            }
        }
    }

    private SyncUserCouponsRequest buildSyncUserCouponsRequest(Message message) {
        SyncUserCouponsRequest request = null;
        try {
            request = JSON.parseObject(message.getBody(), SyncUserCouponsRequest.class);
        } catch (Exception e) {
            log.error("[OneLabelMessageListener] - parse message body exception, msgId: {}", message.getMsgID(), e);
        }
        return request;
    }

    private void syncUserCouponsHandler(String msgId, SyncUserCouponsRequest request) {
        Long userId = Objects.requireNonNull(request.getUserId());
        String labelCode = Objects.requireNonNull(CouponUsageStatusEnum.getByName(request.getCouponUsageStatus())).getLabelCode();

        log.info("[syncUserCouponsHandler] - msgId: {}, userId: {}, request: {}", msgId, userId, JSON.toJSONString(request));

        Long dimensionKeyId = adaptationCouponCommonConfigLoader.getLabelDimensionKeyId(labelCode);

        Message message = new Message();
        message.setTopic("xxx");
        message.setTag("labelDimensionOperation");
        String usableMsgId = mqProducer.syncSend(message, LabelDimensionOperationRequest.builder()
                .userId(userId)
                .labelCode(labelCode)
                .labelDimensionRequestList(Objects.requireNonNull(request.getSyncUserCouponRequestList()).stream().map(couponRequest -> LabelDimensionRequest.builder()
                        .labelDimensionKeyRequest(LabelDimensionKeyRequest.builder()
                                .dimensionKeyDefineType(LabelDimensionKeyDefineTypeEnum.DYNAMIC_KEY.name())
                                .dimensionKeyId(dimensionKeyId)
                                .dimensionDynamicKeyParams(Collections.singletonMap("couponUserId", String.valueOf(couponRequest.getCouponAssetId())))
                                .build())
                        .dimensionVal(couponRequest.getCouponAssetInfo())
                        .build()).collect(Collectors.toList()))
                .build());
        log.info("[syncUserCouponsHandler] - parent msgId: {}, async, userId: {}, labelCode: {}, msgId: {}", msgId, userId, labelCode, usableMsgId);
    }

    private SyncUserCouponChangeEventRequest buildSyncUserCouponChangeEventRequest(Message message) {
        SyncUserCouponChangeEventRequest request = null;
        try {
            request = JSON.parseObject(message.getBody(), SyncUserCouponChangeEventRequest.class);
        } catch (Exception e) {
            log.error("[OneLabelMessageListener] - parse message body exception, msgId: {}", message.getMsgID(), e);
        }
        return request;
    }

    private void syncUserCouponChangeEventHandler(String msgId, SyncUserCouponChangeEventRequest request) {
        Long userId = Objects.requireNonNull(request.getUserId());
        Long couponAssetId = Objects.requireNonNull(request.getCouponAssetId());

        log.info("[syncUserCouponChangeEventHandler] - msgId: {}, userId: {}, couponAssetId: {}, request: {}", msgId, userId, couponAssetId, JSON.toJSONString(request));

        switch (Objects.requireNonNull(CouponChangeEventTypeEnum.getByName(request.getCouponChangeEventType()))) {
            case LOCKED:
                // add
                Message message = new Message();
                message.setTopic("xxx");
                message.setTag("labelDimensionOperation");
                String lockedMsgId = mqProducer.syncSend(message, LabelDimensionOperationRequest.builder()
                        .userId(userId)
                        .labelCode("locked_coupons")
                        .labelDimensionRequestList(Collections.singletonList(LabelDimensionRequest.builder()
                                .labelDimensionKeyRequest(LabelDimensionKeyRequest.builder()
                                        .dimensionKeyDefineType(LabelDimensionKeyDefineTypeEnum.DYNAMIC_KEY.name())
                                        .dimensionKeyId(adaptationCouponCommonConfigLoader.getLabelDimensionKeyId("locked_coupons"))
                                        .dimensionDynamicKeyParams(Collections.singletonMap("couponAssetId", String.valueOf(couponAssetId)))
                                        .build())
                                .dimensionVal(String.valueOf(couponAssetId))
                                .build()))
                        .build());
                log.info("[syncUserCouponChangeEventHandler] - parent msgId: {}, async locked, userId: {}, couponAssetId: {}, msgId: {}", msgId, userId, couponAssetId, lockedMsgId);
                break;
            case UNLOCKED:
                // delete
                Message unLockedMsg = new Message();
                unLockedMsg.setTopic("xxx");
                unLockedMsg.setTag("deleteLabelDimensionOperation");
                String unLockedMsgId = mqProducer.syncSend(unLockedMsg, DeleteLabelDimensionOperationRequest.builder()
                        .userId(userId)
                        .labelCode("locked_coupons")
                        .labelDimensionKeyList(Collections.singletonList(String.valueOf(request.getCouponAssetId())))
                        .build());
                log.info("[syncUserCouponChangeEventHandler] - parent msgId: {}, async unLocked, userId: {}, couponAssetId: {}, msgId: {}", msgId, userId, couponAssetId, unLockedMsgId);
                break;
            case CONFIRMED:
                // add
                Message confirmedMsg = new Message();
                confirmedMsg.setTopic("xxx");
                confirmedMsg.setTag("labelDimensionOperation");
                String confirmedMsgId = mqProducer.syncSend(confirmedMsg, LabelDimensionOperationRequest.builder()
                        .userId(userId)
                        .labelCode("confirmed_coupons")
                        .labelDimensionRequestList(Collections.singletonList(LabelDimensionRequest.builder()
                                .labelDimensionKeyRequest(LabelDimensionKeyRequest.builder()
                                        .dimensionKeyDefineType(LabelDimensionKeyDefineTypeEnum.DYNAMIC_KEY.name())
                                        .dimensionKeyId(adaptationCouponCommonConfigLoader.getLabelDimensionKeyId("confirmed_coupons"))
                                        .dimensionDynamicKeyParams(Collections.singletonMap("couponAssetId", String.valueOf(couponAssetId)))
                                        .build())
                                .dimensionVal(String.valueOf(couponAssetId))
                                .build()))
                        .build());
                log.info("[syncUserCouponChangeEventHandler] - parent msgId: {}, async confirmed, userId: {}, couponAssetId: {}, msgId: {}", msgId, userId, couponAssetId, confirmedMsgId);
                // delete
                Message deleteLockedMsg = new Message();
                deleteLockedMsg.setTopic("xxx");
                deleteLockedMsg.setTag("deleteLabelDimensionOperation");
                String deleteLockedMsgId = mqProducer.syncSend(deleteLockedMsg, DeleteLabelDimensionOperationRequest.builder()
                        .userId(userId)
                        .labelCode("locked_coupons")
                        .labelDimensionKeyList(Collections.singletonList(String.valueOf(request.getCouponAssetId())))
                        .build());
                log.info("[syncUserCouponChangeEventHandler] - parent msgId: {}, async delete locked, userId: {}, couponAssetId: {}, msgId: {}", msgId, userId, couponAssetId, deleteLockedMsgId);
                // delete
                this.deleteUserUsableCoupons(msgId, userId, couponAssetId);
                break;
            case REFUNDED:
                // clear
                Message deleteUsableLabelCodeMsg = new Message();
                deleteUsableLabelCodeMsg.setTopic("xxx");
                deleteUsableLabelCodeMsg.setTag("labelOperation");
                String deleteUsableLabelCodeMsgId = mqProducer.syncSend(deleteUsableLabelCodeMsg, UserLabelOperationRequest.builder()
                        .userId(userId)
                        .labelCode("usable_coupons")
                        .operationType(LabelOperationTypeEnum.DELETE_LABEL.name())
                        .build());
                log.info("[syncUserCouponChangeEventHandler] - parent msgId: {}, async delete usable labelCode, userId: {}, couponAssetId: {}, msgId: {}", msgId, userId, couponAssetId, deleteUsableLabelCodeMsgId);
                // delete
                Message deleteConfirmedMsg = new Message();
                deleteConfirmedMsg.setTopic("xxx");
                deleteConfirmedMsg.setTag("deleteLabelDimensionOperation");
                String deleteConfirmedMsgId = mqProducer.syncSend(deleteConfirmedMsg, DeleteLabelDimensionOperationRequest.builder()
                        .userId(userId)
                        .labelCode("confirmed_coupons")
                        .labelDimensionKeyList(Collections.singletonList(String.valueOf(request.getCouponAssetId())))
                        .build());
                log.info("[syncUserCouponChangeEventHandler] - parent msgId: {}, async delete confirmed, userId: {}, couponAssetId: {}, msgId: {}", msgId, userId, couponAssetId, deleteConfirmedMsgId);
                break;
            case INVALIDED:
                // add
                Message invalidedMsg = new Message();
                invalidedMsg.setTopic("xxx");
                invalidedMsg.setTag("labelDimensionOperation");
                String invalidedMsgId = mqProducer.syncSend(invalidedMsg, LabelDimensionOperationRequest.builder()
                        .userId(userId)
                        .labelCode("invalided_coupons")
                        .labelDimensionRequestList(Collections.singletonList(LabelDimensionRequest.builder()
                                .labelDimensionKeyRequest(LabelDimensionKeyRequest.builder()
                                        .dimensionKeyDefineType(LabelDimensionKeyDefineTypeEnum.DYNAMIC_KEY.name())
                                        .dimensionKeyId(adaptationCouponCommonConfigLoader.getLabelDimensionKeyId("invalided_coupons"))
                                        .dimensionDynamicKeyParams(Collections.singletonMap("couponAssetId", String.valueOf(couponAssetId)))
                                        .build())
                                .dimensionVal(String.valueOf(couponAssetId))
                                .build()))
                        .build());
                log.info("[syncUserCouponChangeEventHandler] - parent msgId: {}, async invalided, userId: {}, couponAssetId: {}, msgId: {}", msgId, userId, couponAssetId, invalidedMsgId);
                // delete
                this.deleteUserUsableCoupons(msgId, userId, couponAssetId);
                break;
            case RETURNED:
                // delete
                this.deleteUserUsableCoupons(msgId, userId, couponAssetId);
                break;
        }
    }

    private void deleteUserUsableCoupons(String msgId, Long userId, Long couponAssetId) {
        if (userLabelQueryService.userHasLabel(FindUserHasLabelRequest.builder()
                .userId(userId)
                .labelCode("usable_coupons")
                .build())) {
            Message deleteUsableMsg = new Message();
            deleteUsableMsg.setTopic("xxx");
            deleteUsableMsg.setTag("deleteLabelDimensionOperation");
            String deleteUsableMsgId = mqProducer.syncSend(deleteUsableMsg, DeleteLabelDimensionOperationRequest.builder()
                    .userId(userId)
                    .labelCode("usable_coupons")
                    .labelDimensionKeyList(Collections.singletonList(String.valueOf(couponAssetId)))
                    .build());
            log.info("[syncUserCouponChangeEventHandler] - parent msgId: {}, async delete usable, userId: {}, couponAssetId: {}, msgId: {}", msgId, userId, couponAssetId, deleteUsableMsgId);
        }
    }

}
