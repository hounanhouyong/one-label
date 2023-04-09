package com.hn.onelabel.server.application.rpc;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.aliyun.openservices.ons.api.Message;
import com.google.common.base.Joiner;
import com.hn.onelabel.adapter.api.common.Result;
import com.hn.onelabel.adapter.api.enums.*;
import com.hn.onelabel.adapter.api.feign.LabelAdaptationService;
import com.hn.onelabel.adapter.api.model.request.*;
import com.hn.onelabel.adapter.api.model.response.*;
import com.hn.onelabel.api.enums.*;
import com.hn.onelabel.api.model.request.*;
import com.hn.onelabel.api.model.response.LabelDimensionResponse;
import com.hn.onelabel.server.common.model.ExperimentNode;
import com.hn.onelabel.server.common.utils.GroovyScriptUtil;
import com.hn.onelabel.server.common.utils.RuleScriptUtils;
import com.hn.onelabel.server.common.utils.SequenceIdUtils;
import com.hn.onelabel.server.common.utils.WeightedRoundRobinUtils;
import com.hn.onelabel.server.domain.aggregate.labelrule.LabelRule;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.LabelDimension;
import com.hn.onelabel.server.domain.aggregate.usersegment.UserSegment;
import com.hn.onelabel.server.domain.aggregate.usersegment.valueobject.AbExperiment;
import com.hn.onelabel.server.infrastructure.cache.RedisCacheService;
import com.hn.onelabel.server.infrastructure.cache.RedisKey;
import com.hn.onelabel.server.infrastructure.mq.RocketMqProducer;
import com.hn.onelabel.server.infrastructure.nacos.*;
import com.hn.onelabel.server.infrastructure.rpc.UserDmpRpcService;
import com.hn.onelabel.server.service.*;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Slf4j
@RestController
@Service
public class LabelAdaptationServiceImpl implements LabelAdaptationService {

    @Autowired
    private LabelAdaptationQueryService labelAdaptationQueryService;
    @Autowired
    private LabelDimensionQueryService labelDimensionQueryService;
    @Autowired
    private LabelDimensionCommandService labelDimensionCommandService;
    @Autowired
    private UserSegmentCommandService userSegmentCommandService;
    @Autowired
    private UserSegmentQueryService userSegmentQueryService;
    @Autowired
    private LabelRuleQueryService labelRuleQueryService;
    @Autowired
    private LabelRuleCommandService labelRuleCommandService;
    @Autowired
    private UserLabelQueryService userLabelQueryService;

    @Autowired
    private UserDmpRpcService userDmpRpcService;

    @Autowired
    private LabelDimensionConfigLoader labelDimensionConfigLoader;
    @Autowired
    private AdaptationTouchFatigueCommonConfigLoader adaptationTouchFatigueCommonConfigLoader;
    @Autowired
    private AdaptationProdRecommendCommonConfigLoader adaptationProdRecommendCommonConfigLoader;
    @Autowired
    private AdaptationAbTestCommonConfigLoader adaptationAbTestCommonConfigLoader;
    @Autowired
    private UserDmpQueryConfigLoader userDmpQueryConfigLoader;
    @Autowired
    private AdaptationProdRecommendListCustomizeConfigLoader adaptationProdRecommendListCustomizeConfigLoader;
    @Autowired
    private AdaptationCouponCommonConfigLoader adaptationCouponCommonConfigLoader;
    @Autowired
    private LabelRuleFieldMappingConfigLoader labelRuleFieldMappingConfigLoader;
    @Autowired
    private AdaptationWelfareCenterCommonConfigLoader adaptationWelfareCenterCommonConfigLoader;

    @Autowired
    private RedisCacheService redisCacheService;

    @Autowired
    private RocketMqProducer mqProducer;

    private final ExecutorService executorService = Executors.newFixedThreadPool(4);

    @Override
    public Result<List<RecommendProductResponse>> findRecommendProduct(FindRecommendProductRequest request) {
        Result<List<RecommendProductResponse>> result = new Result<>();

        String labelCode = Objects.requireNonNull(RecommendTypeEnum.getByName(request.getRecommendType())).getLabelCode();

        Triple<List<RecommendProductResponse>, List<LabelDimension>, List<Triple<String, String, Integer>>> recommendProductTriple = labelAdaptationQueryService.findRecommendProduct(request.getUserId(), labelCode);

        // update score
        List<LabelDimensionRequest> labelDimensionRequestList = new ArrayList<>();
        AtomicInteger rankingNo = new AtomicInteger(1);

        Long dimensionKeyId = labelDimensionConfigLoader.buildDimensionKeyId(labelCode, adaptationProdRecommendCommonConfigLoader.getLabelDimensionKeyId(labelCode), null);

        List<String> topProductIdList = adaptationProdRecommendListCustomizeConfigLoader.getTopProductIdList();
        List<String> removedProductIdList = adaptationProdRecommendListCustomizeConfigLoader.getRemovedProductIdList();

        if (!CollectionUtils.isEmpty(recommendProductTriple.getLeft()) && !CollectionUtils.isEmpty(removedProductIdList)) {
            recommendProductTriple.getLeft().removeIf(item -> removedProductIdList.contains(item.getProductId()));
        }
        if (!CollectionUtils.isEmpty(recommendProductTriple.getMiddle()) && !CollectionUtils.isEmpty(removedProductIdList)) {
            recommendProductTriple.getMiddle().removeIf(item -> removedProductIdList.contains(item.getDimensionKey()));
        }
        if (!CollectionUtils.isEmpty(recommendProductTriple.getRight()) && !CollectionUtils.isEmpty(removedProductIdList)) {
            recommendProductTriple.getRight().removeIf(item -> removedProductIdList.contains(item.getLeft()));
        }

        List<String> topAndSortedProductIdList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(recommendProductTriple.getLeft())) {
            if (!CollectionUtils.isEmpty(topProductIdList)) {
                topAndSortedProductIdList = recommendProductTriple.getLeft().stream().filter(response -> topProductIdList.contains(response.getProductId())).map(RecommendProductResponse::getProductId).distinct().collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(topAndSortedProductIdList)) {
                    labelDimensionRequestList.addAll(this.buildLabelDimensionRequestList(topAndSortedProductIdList, dimensionKeyId, rankingNo));
                }
            }
            List<String> topAndSortedAnotherProductIdList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(topProductIdList)) {
                topAndSortedAnotherProductIdList = recommendProductTriple.getRight().stream().filter(triple -> topProductIdList.contains(triple.getLeft())).map(Triple::getLeft).distinct().collect(Collectors.toList());
                if (!CollectionUtils.isEmpty(topAndSortedAnotherProductIdList)) {
                    labelDimensionRequestList.addAll(this.buildLabelDimensionRequestList(topAndSortedAnotherProductIdList, dimensionKeyId, rankingNo));
                }
            }

            List<String> finalTopAndSortedProductIdList = topAndSortedProductIdList;
            List<String> sortedProductIdList = recommendProductTriple.getLeft().stream().filter(response -> !finalTopAndSortedProductIdList.contains(response.getProductId())).sorted(Comparator.comparing(RecommendProductResponse::getRankingNo)).map(RecommendProductResponse::getProductId).collect(Collectors.toList());
            labelDimensionRequestList.addAll(this.buildLabelDimensionRequestList(sortedProductIdList, dimensionKeyId, rankingNo));

            List<String> anotherProductIdList = new ArrayList<>();
            if (!CollectionUtils.isEmpty(recommendProductTriple.getMiddle())) {
                List<String> finalTopAndSortedProductIds = topAndSortedProductIdList;
                anotherProductIdList = recommendProductTriple.getMiddle().stream().filter(ld -> !finalTopAndSortedProductIds.contains(ld.getDimensionKey()) && !sortedProductIdList.contains(ld.getDimensionKey())).map(LabelDimension::getDimensionKey).collect(Collectors.toList());
            }
            if (!CollectionUtils.isEmpty(recommendProductTriple.getRight())) {
                List<String> finalTopAndSortedAnotherProductIdList = topAndSortedAnotherProductIdList;
                anotherProductIdList.addAll(recommendProductTriple.getRight().stream().filter(triple -> !finalTopAndSortedAnotherProductIdList.contains(triple.getLeft()) && !sortedProductIdList.contains(triple.getLeft())).map(Triple::getLeft).collect(Collectors.toList()));
            }

            labelDimensionRequestList.addAll(this.buildLabelDimensionRequestList(anotherProductIdList, dimensionKeyId, rankingNo));
        }

        if (!CollectionUtils.isEmpty(labelDimensionRequestList)) {
            if (adaptationProdRecommendCommonConfigLoader.productRecommendReOrderSyncSwitchIsOpen()) {
                labelDimensionCommandService.labelDimensionOperation(SequenceIdUtils.generateSequenceId(), request.getUserId(), labelCode, labelDimensionRequestList);
            } else {
                Message message = new Message();
                message.setTopic("xxx");
                message.setTag("labelDimensionOperation");
                String msgId = mqProducer.syncSend(message, LabelDimensionOperationRequest.builder()
                        .userId(request.getUserId())
                        .labelCode(labelCode)
                        .labelDimensionRequestList(labelDimensionRequestList)
                        .build());
                log.info("[findRecommendProduct] - send message, msgId: {}.", msgId);
            }
        } else {
            if (!CollectionUtils.isEmpty(recommendProductTriple.getMiddle())) {
                Message message = new Message();
                message.setTopic("xxx");
                message.setTag("labelOperation");
                String msgId = mqProducer.syncSend(message, UserLabelOperationRequest.builder()
                        .userId(request.getUserId())
                        .labelCode(labelCode)
                        .operationType(LabelOperationTypeEnum.DELETE_LABEL.name())
                        .build());
                log.info("[findRecommendProduct] - send message, msgId: {}.", msgId);
            }
        }

        if (CollectionUtils.isEmpty(topAndSortedProductIdList)) {
            return result.success(recommendProductTriple.getLeft());
        } else {
            List<String> finalTopProductIdList = topAndSortedProductIdList;
            AtomicInteger reRankingNo = new AtomicInteger(1);
            // top
            List<RecommendProductResponse> responses = recommendProductTriple.getLeft().stream().filter(response -> finalTopProductIdList.contains(response.getProductId())).map(response -> RecommendProductResponse.builder()
                    .productCategory(response.getProductCategory())
                    .productId(response.getProductId())
                    .rankingNo(reRankingNo.getAndIncrement())
                    .build()).collect(Collectors.toList());
            // other
            responses.addAll(recommendProductTriple.getLeft().stream().filter(response -> !finalTopProductIdList.contains(response.getProductId())).sorted(Comparator.comparing(RecommendProductResponse::getRankingNo)).map(response -> RecommendProductResponse.builder()
                    .productCategory(response.getProductCategory())
                    .productId(response.getProductId())
                    .rankingNo(reRankingNo.getAndIncrement())
                    .build()).collect(Collectors.toList()));
            return result.success(responses);
        }
    }

    private List<LabelDimensionRequest> buildLabelDimensionRequestList(List<String> productIdList, Long dimensionKeyId, AtomicInteger rankingNo) {
        List<LabelDimensionRequest> labelDimensionRequestList = new ArrayList<>();
        if (!CollectionUtils.isEmpty(productIdList)) {
            productIdList.forEach(productId -> {
                Map<String, String> dataMap = new HashMap<>();
                dataMap.put("productId", productId);
                labelDimensionRequestList.add(LabelDimensionRequest.builder()
                        .labelDimensionKeyRequest(LabelDimensionKeyRequest.builder()
                                .dimensionKeyDefineType(LabelDimensionKeyDefineTypeEnum.DYNAMIC_KEY.name())
                                .dimensionKeyId(dimensionKeyId)
                                .dimensionDynamicKeyParams(dataMap)
                                .build())
                        .dimensionVal(String.valueOf(rankingNo.getAndIncrement()))
                        .build());
            });
        }
        return labelDimensionRequestList;
    }

    @Override
    public Result<Integer> findUserTouchFatigue(FindUserTouchFatigueRequest request) {
        Result<Integer> result = new Result<>();
        return result.success(this.findTouchFatigue(request.getUserId(), TouchFatigueTypeEnum.getByName(request.getTouchFatigueType()), request.getResourcesKeyword()));
    }

    @Override
    public Result<Integer> findUserTouchFatigueAndIncr(FindUserTouchFatigueRequest request) {
        Result<Integer> result = new Result<>();
        String labelCode = Objects.requireNonNull(TouchFatigueTypeEnum.getByName(request.getTouchFatigueType())).getLabelCode();

        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("resource", request.getResourcesKeyword());

        LabelDimensionKeyRequest labelDimensionKeyRequest = LabelDimensionKeyRequest.builder()
                .dimensionKeyDefineType(LabelDimensionKeyDefineTypeEnum.DYNAMIC_KEY.name())
                .dimensionKeyId(adaptationTouchFatigueCommonConfigLoader.getLabelDimensionKeyId(labelCode))
                .dimensionFixedKey(null)
                .dimensionDynamicKeyParams(dataMap)
                .build();

        int fatigue = this.findTouchFatigue(request.getUserId(), TouchFatigueTypeEnum.getByName(request.getTouchFatigueType()), request.getResourcesKeyword());

        // incr
        if (adaptationTouchFatigueCommonConfigLoader.touchFatigueIncrSyncSwitchIsOpen()) {
            labelDimensionCommandService.labelDimensionIncr(SequenceIdUtils.generateSequenceId(), request.getUserId(), labelCode, labelDimensionKeyRequest, request.getIncreaseVal());
        } else {
            Message message = new Message();
            message.setTopic("xxx");
            message.setTag("labelDimensionIncr");
            String msgId = mqProducer.syncSend(message, LabelDimensionIncrRequest.builder()
                    .userId(request.getUserId())
                    .labelCode(labelCode)
                    .labelDimensionKeyRequest(labelDimensionKeyRequest)
                    .increaseVal(request.getIncreaseVal())
                    .build());
            log.info("[findUserTouchFatigueAndIncr] - send message, msgId: {}.", msgId);
        }

        return result.success(fatigue);
    }

    @Override
    public Result<List<PromotionPlanExperimentTouchStatisticsResponse>> findPromotionPlanTouchStatistics(FindPromotionPlanTouchStatisticsRequest request) {
        Result<List<PromotionPlanExperimentTouchStatisticsResponse>> result = new Result<>();
        Objects.requireNonNull(request.getPromotionPlanId(), "Null promotionPlanId.");
        Assert.isTrue(!CollectionUtils.isEmpty(request.getExperimentInfoList()), "Null experimentInfoList.");
        request.getExperimentInfoList().forEach(experimentInfo -> Assert.isTrue(!CollectionUtils.isEmpty(experimentInfo.getChannelCodeList()), "Null channelCodeList."));
        return result.success(labelDimensionQueryService.findPromotionPlanTouchStatistics(request));
    }

    @Override
    public Result<List<PromotionPlanExperimentTouchFailureResultResponse>> findPromotionPlanTouchFailureResult(FindPromotionPlanTouchResultDetailsRequest request) {
        Result<List<PromotionPlanExperimentTouchFailureResultResponse>> result = new Result<>();
        Objects.requireNonNull(request.getPromotionPlanId(), "Null promotionPlanId.");
        Assert.isTrue(!CollectionUtils.isEmpty(request.getExperimentQueryInfoList()), "Null experimentQueryInfoList.");
        request.getExperimentQueryInfoList().forEach(experimentQueryInfo -> Assert.isTrue(!CollectionUtils.isEmpty(experimentQueryInfo.getChannelQueryInfoList()), "Null channelQueryInfoList."));
        return result.success(labelDimensionQueryService.findPromotionPlanTouchFailureResult(request));
    }

    @Override
    public Result<Boolean> saveUserSegmentRuleInfo(SaveUserSegmentRuleInfoRequest request) {
        Result<Boolean> result = new Result<>();
        Objects.requireNonNull(request.getRuleInfo(), "Null ruleInfo.");
        Objects.requireNonNull(request.getSaveExperimentInfoRequestList(), "Null saveExperimentInfoRequestList.");

        userSegmentCommandService.saveUserSegmentInfo(SaveUserSegmentInfoRequest.builder()
                .segmentType(Objects.requireNonNull(SegmentTypeEnum.getByName(request.getSegmentType())).name())
                .segmentName(StringUtils.isEmpty(request.getSegmentName()) ? "no name" : request.getSegmentName())
                .ruleScriptType(RuleScriptTypeEnum.GROOVY.name())
                .ruleScriptContent(RuleScriptUtils.buildRuleScript4equation(request.getRuleInfo(), userDmpQueryConfigLoader.getFieldMapping()))
                .externalExperimentGroupId(request.getExperimentGroupId())
                .creator("system")
                .saveUserAbExperimentInfoRequestList(request.getSaveExperimentInfoRequestList().stream().map(experiment -> SaveUserAbExperimentInfoRequest.builder()
                        .externalExperimentCode(Objects.requireNonNull(experiment.getExperimentCode()))
                        .weight(Objects.requireNonNull(experiment.getWeight()))
                        .externalExperimentExtInfo(CollectionUtils.isEmpty(experiment.getSaveExperimentExtInfoRequestList()) ? null : JSON.toJSONString(experiment.getSaveExperimentExtInfoRequestList()))
                        .externalExperimentTag(CollectionUtils.isEmpty(experiment.getSaveExperimentExtInfoRequestList()) ? null : Joiner.on(",").join(experiment.getSaveExperimentExtInfoRequestList().stream().map(SaveExperimentExtInfoRequest::getExperimentTag).distinct().collect(Collectors.toList())))
                        .build()).collect(Collectors.toList()))
                .build());

        return result.success(true);
    }

    @Override
    public Result<List<UserSegmentResponse>> findUserSegmentInfo(FindUserSegmentInfoRequest request) {
        Result<List<UserSegmentResponse>> result = new Result<>();
        Objects.requireNonNull(request.getUserId(), "Null userId.");
        Objects.requireNonNull(request.getSegmentType(), "Null segmentType.");
        Objects.requireNonNull(SegmentTypeEnum.getByName(request.getSegmentType()), "Error segmentType.");

        List<UserSegmentResponse> responses = new ArrayList<>();

        if (CollectionUtils.isEmpty(request.getExperimentGroupIdList())) {
            log.info("[findUserSegmentInfo] - Null experimentGroupIdList, userId: {}", request.getUserId());
            return result.success(responses);
        }

        String labelCode = Objects.requireNonNull(SegmentSceneEnum.getByName(request.getSegmentScene())).getLabelCode();

        List<String> selectedExperimentGroupIdList = new ArrayList<>();
        labelDimensionQueryService.findLabelDimensions(FindUserLabelDimensionsRequest.builder()
                .userId(request.getUserId())
                .labelCode(labelCode)
                .build(), true, true).forEach(ldResponse -> {
            String[] array = ldResponse.getDimensionKey().split("#");
            selectedExperimentGroupIdList.add(array[0]);
            if (this.atLeastOneExperimentTagSatisfied(JSONObject.parseArray(ldResponse.getDimensionVal()), request.getExperimentTags())) {
                responses.add(UserSegmentResponse.builder()
                        .experimentGroupId(array[0])
                        .experimentCode(array[1])
                        .experimentExtInfo(ldResponse.getDimensionVal())
                        .build());
            }
        });

        String abTestLabelCode = "ab_test_tag";
        List<AbExperiment> selectedAbExperimentList = new ArrayList<>();
        List<LabelDimensionRequest> selectedAbTestLabelDimensionRequestList = new ArrayList<>();

        List<LabelDimensionResponse> labelDimensionResponses = labelDimensionQueryService.findLabelDimensions(FindUserLabelDimensionsRequest.builder()
                        .userId(request.getUserId())
                        .labelCode(abTestLabelCode)
                        .build(), true, false);

        Object[] args = { userDmpRpcService.findUserDmp(request.getUserId()) };
        request.getExperimentGroupIdList().removeIf(selectedExperimentGroupIdList::contains);
        userSegmentQueryService.findUserSegments(UserSegmentTypeEnum.PROMOTION_PLAN, RuleStatusEnum.ENABLED, request.getExperimentGroupIdList()).forEach(userSegment -> {
            if (!this.hitRule(userSegment.getRuleScript().getRuleScriptContent(), args)) {
                return;
            }
            Pair<AbExperiment, LabelDimensionRequest> selectedAbExperimentPair = this.selectAbExperimentAndBuildLabelDimensionRequest(request.getUserId(), abTestLabelCode, labelDimensionResponses, userSegment);
            if (Objects.isNull(selectedAbExperimentPair.getLeft())) {
                return;
            }
            if (this.atLeastOneExperimentTagSatisfied(JSONObject.parseArray(selectedAbExperimentPair.getLeft().getExternalExperimentExtInfo()), request.getExperimentTags())) {
                responses.add(UserSegmentResponse.builder()
                        .experimentId(selectedAbExperimentPair.getLeft().getExperimentId())
                        .experimentCode(selectedAbExperimentPair.getLeft().getExternalExperimentCode())
                        .experimentGroupId(selectedAbExperimentPair.getLeft().getExternalExperimentGroupId())
                        .experimentExtInfo(selectedAbExperimentPair.getLeft().getExternalExperimentExtInfo())
                        .build());
                selectedAbExperimentList.add(selectedAbExperimentPair.getLeft());
            }
            if (!Objects.isNull(selectedAbExperimentPair.getRight())) {
                selectedAbTestLabelDimensionRequestList.add(selectedAbExperimentPair.getRight());
            }
        });

        if (!CollectionUtils.isEmpty(selectedAbTestLabelDimensionRequestList)) {
            if (adaptationAbTestCommonConfigLoader.addAbTestLabelSwitchIsOpen()) {
                if (adaptationAbTestCommonConfigLoader.addAbTestSyncSwitchIsOpen()) {
                    labelDimensionCommandService.labelDimensionOperation(SequenceIdUtils.generateSequenceId(), request.getUserId(), abTestLabelCode, selectedAbTestLabelDimensionRequestList);
                } else {
                    Message message = new Message();
                    message.setTopic("xxx");
                    message.setTag("labelDimensionOperation");
                    String msgId = mqProducer.syncSend(message, LabelDimensionOperationRequest.builder()
                            .userId(request.getUserId())
                            .labelCode(abTestLabelCode)
                            .labelDimensionRequestList(selectedAbTestLabelDimensionRequestList)
                            .build());
                    log.info("[findUserSegmentInfo] - send abTestLabelDimensionOperation message, userId: {}, msgId: {}.", request.getUserId(), msgId);
                }
            }
        }

        if (!CollectionUtils.isEmpty(selectedAbExperimentList)) {
            if (!CollectionUtils.isEmpty(request.getExperimentTags()) && request.getExperimentTags().contains(ExperimentTagEnum.WELFARE_CENTER.name())) {
                if (adaptationWelfareCenterCommonConfigLoader.addWelfareCenterLabelSwitchIsOpen()) {
                    List<LabelDimensionRequest> selectedWelfareCenterLabelDimensionRequestList = selectedAbExperimentList.stream().map(abExperiment -> {
                        Map<String, String> dataMap = new HashMap<>();
                        dataMap.put("experimentGroupId", abExperiment.getExternalExperimentGroupId());
                        dataMap.put("experimentId", abExperiment.getExternalExperimentCode());
                        return LabelDimensionRequest.builder()
                                .labelDimensionKeyRequest(LabelDimensionKeyRequest.builder()
                                        .dimensionKeyDefineType(LabelDimensionKeyDefineTypeEnum.DYNAMIC_KEY.name())
                                        .dimensionKeyId(adaptationWelfareCenterCommonConfigLoader.getLabelDimensionKeyId(labelCode))
                                        .dimensionDynamicKeyParams(dataMap)
                                        .build())
                                .dimensionVal(abExperiment.getExternalExperimentExtInfo())
                                .build();
                    }).collect(Collectors.toList());
                    if (adaptationWelfareCenterCommonConfigLoader.addWelfareCenterLabelSyncSwitchIsOpen()) {
                        labelDimensionCommandService.labelDimensionOperation(SequenceIdUtils.generateSequenceId(), request.getUserId(), labelCode, selectedWelfareCenterLabelDimensionRequestList);
                    } else {
                        Message message = new Message();
                        message.setTopic("xxx");
                        message.setTag("labelDimensionOperation");
                        String msgId = mqProducer.syncSend(message, LabelDimensionOperationRequest.builder()
                                .userId(request.getUserId())
                                .labelCode(labelCode)
                                .labelDimensionRequestList(selectedWelfareCenterLabelDimensionRequestList)
                                .build());
                        log.info("[findUserSegmentInfo] - send welfareCenterLabelDimensionOperation message, userId: {}, msgId: {}.", request.getUserId(), msgId);
                    }
                }
            }
        }

        return result.success(responses);
    }

    private boolean hitRule(String ruleScript, Object[] args) {
        try {
            return (Boolean) Objects.requireNonNull(GroovyScriptUtil.invokeMethod(ruleScript, "compute", args));
        } catch (Exception e) {
            log.error("[findUserSegmentInfo] - execute groovy script exception, ruleScript: {}", ruleScript, e);
        }
        return false;
    }

    private Pair<AbExperiment, LabelDimensionRequest> selectAbExperimentAndBuildLabelDimensionRequest(Long userId, String labelCode, List<LabelDimensionResponse> labelDimensionResponses, UserSegment userSegment) {
        Pair<String, Boolean> selectedExperimentIdPair = labelDimensionResponses.stream().filter(ld -> ld.getDimensionKey().split("#")[0].equals(userSegment.getExternalExperimentGroupId())).map(ld -> Pair.of(ld.getDimensionVal(), false)).findFirst()
                .orElse(Pair.of(this.selectOneExperiment(userSegment).getExperimentId(), true));
        AbExperiment selectedAbExperiment = userSegment.getAbExperimentList().stream().filter(abExperiment -> abExperiment.getExperimentId().equals(selectedExperimentIdPair.getLeft())).findFirst().orElse(null);
        if (Objects.isNull(selectedAbExperiment)) {
            log.error("[findUserSegmentInfo] - no ab experiment, userId: {}, labelCode: {}, addLabel: {}, selectedExperimentId: {}, userSegment: {}", userId, labelCode, selectedExperimentIdPair.getRight(), selectedExperimentIdPair.getLeft(), JSON.toJSONString(userSegment));
            return Pair.of(null, null);
        } else if (selectedExperimentIdPair.getRight()) {
            Map<String, String> dataMap = new HashMap<>();
            dataMap.put("experimentGroupId", selectedAbExperiment.getExternalExperimentGroupId());
            dataMap.put("experimentId", selectedAbExperiment.getExternalExperimentCode());
            return Pair.of(selectedAbExperiment, LabelDimensionRequest.builder()
                    .labelDimensionKeyRequest(LabelDimensionKeyRequest.builder()
                            .dimensionKeyDefineType(LabelDimensionKeyDefineTypeEnum.DYNAMIC_KEY.name())
                            .dimensionKeyId(adaptationAbTestCommonConfigLoader.getLabelDimensionKeyId(labelCode))
                            .dimensionDynamicKeyParams(dataMap)
                            .build())
                    .dimensionVal(selectedAbExperiment.getExperimentId())
                    .build());
        } else {
            return Pair.of(selectedAbExperiment, null);
        }
    }

    private boolean atLeastOneExperimentTagSatisfied(JSONArray jsonArray, List<String> experimentTags) {
        if (Objects.isNull(jsonArray) || jsonArray.size() == 0) {
            return false;
        }
        if (CollectionUtils.isEmpty(experimentTags)) {
            return true;
        }

        AtomicReference<Boolean> result = new AtomicReference<>(false);
        experimentTags.forEach(experimentTag -> {
            if (result.get()) {
                return;
            }
            for (int i=0; i<jsonArray.size(); i++) {
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                if (jsonObject.containsKey("experimentTag") && jsonObject.getString("experimentTag").equals(experimentTag)) {
                    result.set(true);
                    return;
                }
            }
        });

        return result.get();
    }

    private ExperimentNode selectOneExperiment(UserSegment userSegment) {
        WeightedRoundRobinUtils weightedRoundRobinUtils;
        String abExperimentNodesVal = redisCacheService.get(RedisKey.getAbExperimentNodesRedisKey(userSegment.getExternalExperimentGroupId()));
        if (StringUtils.isEmpty(abExperimentNodesVal)) {
            weightedRoundRobinUtils = new WeightedRoundRobinUtils(userSegment.getAbExperimentList().stream().map(abExperiment -> new ExperimentNode(abExperiment.getExperimentId(),abExperiment.getWeight())).collect(Collectors.toList()));
        } else {
            weightedRoundRobinUtils = new WeightedRoundRobinUtils(JSON.parseArray(abExperimentNodesVal, ExperimentNode.class));
        }
        ExperimentNode experimentNode = weightedRoundRobinUtils.selectNode();
        redisCacheService.set(RedisKey.getAbExperimentNodesRedisKey(userSegment.getExternalExperimentGroupId()), JSON.toJSONString(weightedRoundRobinUtils.getExperimentNodes()), 7L, TimeUnit.DAYS);
        return experimentNode;
    }

    @Override
    public Result<List<UserAbExperimentGroupResponse>> findUserAbExperimentGroupInfo(FindUserAbExperimentGroupRequest request) {
        Result<List<UserAbExperimentGroupResponse>> result = new Result<>();
        Objects.requireNonNull(request.getExperimentGroupId(), "Null experimentGroupId.");
        Objects.requireNonNull(request.getUserIdList(), "Null userIdList.");
        Assert.isTrue(request.getUserIdList().size() <= adaptationAbTestCommonConfigLoader.findAbExperimentGroupMaxUserGroupSize(1000), "too large userIdList.size");

        List<UserAbExperimentGroupResponse> responses = new ArrayList<>();
        String labelCode = "ab_test_tag";

        // 1 find user segment by experimentGroupId
        UserSegment userSegment = userSegmentQueryService.findUserSegment(UserSegmentTypeEnum.PROMOTION_PLAN, RuleStatusEnum.ENABLED, request.getExperimentGroupId());
        if (Objects.isNull(userSegment)) {
            log.info("[findUserAbExperimentGroupInfo] - Null userSegment, experimentGroupId: {}.", request.getExperimentGroupId());
            return result.success(responses);
        }

        Map<String, AbExperiment> experimentMap = userSegment.getAbExperimentList().stream().collect(Collectors.toMap(AbExperiment::getExperimentId, abExperiment -> abExperiment));
        WeightedRoundRobinUtils weightedRoundRobin = new WeightedRoundRobinUtils(userSegment.getAbExperimentList().stream().map(abExperiment -> new ExperimentNode(abExperiment.getExperimentId(), abExperiment.getWeight())).collect(Collectors.toList()));

        CompletionService<Pair<Long, String>> executorCompletionService = new ExecutorCompletionService<>(executorService);
        List<Future<Pair<Long, String>>> futures = new ArrayList<>();

        request.getUserIdList().forEach(userId -> futures.add(executorCompletionService.submit(() -> {
            // 2 find user label dimension
            String selectedExperimentId = null;
            if (adaptationAbTestCommonConfigLoader.addAbTestLabelSwitchIsOpen()) {
                selectedExperimentId = labelDimensionQueryService.findLabelDimensions(FindUserLabelDimensionsRequest.builder()
                        .userId(userId)
                        .labelCode(labelCode)
                        .build(), true, true).stream()
                        .filter(ldResponse -> ldResponse.getDimensionKey().split("#")[0].equals(request.getExperimentGroupId()))
                        .map(LabelDimensionResponse::getDimensionVal).findFirst().orElse(null);
            }
            if (StringUtils.isEmpty(selectedExperimentId)) {
                // 3 weight round robin
                ExperimentNode experimentNode = weightedRoundRobin.selectNode();
                selectedExperimentId = experimentNode.getExperimentId();
            }
            return Pair.of(userId, selectedExperimentId);
        })));

        Map<String, List<Long>> experimentAndUserMap = new ConcurrentHashMap<>(16);

        for (int i=0; i<futures.size(); i++) {
            Pair<Long, String> pair;
            try {
                pair = executorCompletionService.take().get();
            } catch (InterruptedException | ExecutionException e) {
                log.error("[findUserAbExperimentGroupInfo] - executorCompletionService exception.", e);
                continue;
            }

            if (Objects.isNull(pair) || StringUtils.isEmpty(pair.getRight())) {
                log.error("[findUserAbExperimentGroupInfo] - select ab experiment failure.");
                continue;
            }
            if (!experimentMap.containsKey(pair.getRight())) {
                log.error("[findUserAbExperimentGroupInfo] - no ab experiment, selectedExperimentId: {}", pair.getRight());
                continue;
            }

            // 4 add user label dimension
            if (adaptationAbTestCommonConfigLoader.addAbTestLabelSwitchIsOpen()) {
                executorService.submit(() -> {
                    Map<String, String> dataMap = new HashMap<>();
                    dataMap.put("experimentGroupId", experimentMap.get(pair.getRight()).getExternalExperimentGroupId());
                    dataMap.put("experimentId", experimentMap.get(pair.getRight()).getExternalExperimentCode());

                    List<LabelDimensionRequest> labelDimensionRequestList = Collections.singletonList(LabelDimensionRequest.builder()
                            .labelDimensionKeyRequest(LabelDimensionKeyRequest.builder()
                                    .dimensionKeyDefineType(LabelDimensionKeyDefineTypeEnum.DYNAMIC_KEY.name())
                                    .dimensionKeyId(adaptationAbTestCommonConfigLoader.getLabelDimensionKeyId(labelCode))
                                    .dimensionDynamicKeyParams(dataMap)
                                    .build())
                            .dimensionVal(experimentMap.get(pair.getRight()).getExperimentId())
                            .build());

                    if (adaptationAbTestCommonConfigLoader.addAbTestSyncSwitchIsOpen()) {
                        labelDimensionCommandService.labelDimensionOperation(SequenceIdUtils.generateSequenceId(), pair.getLeft(), labelCode, labelDimensionRequestList);
                    } else {
                        Message message = new Message();
                        message.setTopic("xxx");
                        message.setTag("labelDimensionOperation");
                        String msgId = mqProducer.syncSend(message, LabelDimensionOperationRequest.builder()
                                .userId(pair.getLeft())
                                .labelCode(labelCode)
                                .labelDimensionRequestList(labelDimensionRequestList)
                                .build());
                        log.info("[findUserAbExperimentGroupInfo] - send abTestLabelDimensionOperation message, userId: {}, msgId: {}.", pair.getLeft(), msgId);
                    }
                });
            }

            List<Long> userIdList = new ArrayList<>();
            if (experimentAndUserMap.containsKey(pair.getRight())) {
                userIdList = experimentAndUserMap.get(pair.getRight());
            }
            userIdList.add(pair.getLeft());
            experimentAndUserMap.put(pair.getRight(), userIdList);
        }

        // 5 build response
        experimentAndUserMap.forEach((experimentId, userIdList) -> {
            if (experimentMap.containsKey(experimentId)) {
                responses.add(UserAbExperimentGroupResponse.builder()
                        .experimentGroupId(experimentMap.get(experimentId).getExternalExperimentGroupId())
                        .experimentCode(experimentMap.get(experimentId).getExternalExperimentCode())
                        .experimentExtInfo(experimentMap.get(experimentId).getExternalExperimentExtInfo())
                        .experimentId(experimentId)
                        .userIdList(userIdList)
                        .build());
            }
        });

        return result.success(responses);
    }

    @Override
    public Result<List<String>> findUserUsableCoupons(FindUserUsableCouponsRequest request) {
        Result<List<String>> result = new Result<>();
        return result.success(this.findUsableCoupons(request.getUserId()).stream().map(LabelDimensionResponse::getDimensionVal).collect(Collectors.toList()));
    }

    @Override
    public Result<UserUsableCouponTemplatesResponse> findUserUsableCouponTemplates(FindUserUsableCouponTemplatesRequest request) {
        Result<UserUsableCouponTemplatesResponse> result = new Result<>();
        Boolean hasLabel = userLabelQueryService.userHasLabel(FindUserHasLabelRequest.builder()
                .userId(request.getUserId())
                .labelCode("usable_coupons")
                .build());
        if (CollectionUtils.isEmpty(request.getCouponTemplates()) || !hasLabel) {
            return result.success(UserUsableCouponTemplatesResponse.builder()
                    .hasSyncUsableCoupon(hasLabel)
                    .usableCouponTemplates(new ArrayList<>())
                    .build());
        }
        return result.success(UserUsableCouponTemplatesResponse.builder()
                .hasSyncUsableCoupon(true)
                .usableCouponTemplates(this.findUsableCoupons(request.getUserId()).stream()
                        .filter(ld -> JSON.parseObject(ld.getDimensionVal()).containsKey("couponId"))
                        .map(ld -> JSON.parseObject(ld.getDimensionVal()).getLong("couponId")).distinct()
                        .collect(Collectors.toList()).stream().filter(couponId -> request.getCouponTemplates().contains(couponId)).collect(Collectors.toList()))
                .build());
    }

    private List<LabelDimensionResponse> findUsableCoupons(Long userId) {
        Objects.requireNonNull(userId, "Null userId.");
        List<Pair<String, List<LabelDimensionResponse>>> labelDimensionResponses = labelDimensionQueryService.findLabelDimensions(FindUserLabelDimensionsRequestV2.builder()
                .userId(userId)
                .labelCodes(Arrays.asList("usable_coupons", "locked_coupons", "confirmed_coupons", "invalided_coupons"))
                .build(), true, true);
        if (CollectionUtils.isEmpty(labelDimensionResponses)) {
            return new ArrayList<>();
        }

        List<LabelDimensionResponse> usableCouponLabelDimensions = labelDimensionResponses.stream().filter(pair -> pair.getLeft().equals("usable_coupons")).findFirst().orElse(Pair.of("usable_coupons", new ArrayList<>())).getRight();
        if (CollectionUtils.isEmpty(usableCouponLabelDimensions)) {
            return new ArrayList<>();
        }
        List<LabelDimensionResponse> lockedCouponLabelDimensions = labelDimensionResponses.stream().filter(pair -> pair.getLeft().equals("locked_coupons")).findFirst().orElse(Pair.of("locked_coupons", new ArrayList<>())).getRight();
        List<LabelDimensionResponse> confirmedCouponLabelDimensions = labelDimensionResponses.stream().filter(pair -> pair.getLeft().equals("confirmed_coupons")).findFirst().orElse(Pair.of("confirmed_coupons", new ArrayList<>())).getRight();
        List<LabelDimensionResponse> invalidedCouponLabelDimensions = labelDimensionResponses.stream().filter(pair -> pair.getLeft().equals("invalided_coupons")).findFirst().orElse(Pair.of("invalided_coupons", new ArrayList<>())).getRight();
        if (!CollectionUtils.isEmpty(lockedCouponLabelDimensions) || !CollectionUtils.isEmpty(confirmedCouponLabelDimensions) || !CollectionUtils.isEmpty(invalidedCouponLabelDimensions)) {
            List<String> lockedCouponAssetIdList = lockedCouponLabelDimensions.stream().map(LabelDimensionResponse::getDimensionKey).distinct().collect(Collectors.toList());
            List<String> confirmedCouponAssetIdList = confirmedCouponLabelDimensions.stream().map(LabelDimensionResponse::getDimensionKey).distinct().collect(Collectors.toList());
            List<String> invalidedCouponAssetIdList = invalidedCouponLabelDimensions.stream().map(LabelDimensionResponse::getDimensionKey).distinct().collect(Collectors.toList());
            usableCouponLabelDimensions.removeIf(item -> lockedCouponAssetIdList.contains(item.getDimensionKey()) || confirmedCouponAssetIdList.contains(item.getDimensionKey()) || invalidedCouponAssetIdList.contains(item.getDimensionKey()));
        }

        List<String> expireCouponAssetIds = new ArrayList<>();

        Iterator<LabelDimensionResponse> it = usableCouponLabelDimensions.iterator();
        while (it.hasNext()) {
            LabelDimensionResponse item = it.next();
            JSONObject couponAssetJson = JSONObject.parseObject(item.getDimensionVal());
            if (couponAssetJson.containsKey("effectiveEndTime")) {
                Date effectiveEndTime = couponAssetJson.getDate("effectiveEndTime");
                if (effectiveEndTime.before(new Date())) {
                    it.remove();
                    expireCouponAssetIds.add(item.getDimensionKey());
                }
            }
        }

        if (adaptationCouponCommonConfigLoader.clearExpireCouponSwitchIsOpen() && !CollectionUtils.isEmpty(expireCouponAssetIds)) {
            // delete
            Message deleteUsableMsg = new Message();
            deleteUsableMsg.setTopic("xxx");
            deleteUsableMsg.setTag("deleteLabelDimensionOperation");
            String deleteUsableMsgId = mqProducer.syncSend(deleteUsableMsg, DeleteLabelDimensionOperationRequest.builder()
                    .userId(userId)
                    .labelCode("usable_coupons")
                    .labelDimensionKeyList(expireCouponAssetIds)
                    .build());
            log.info("[findUserUsableCoupons] - async delete usable, userId: {}, couponAssetIds: {}, msgId: {}", userId, JSON.toJSONString(expireCouponAssetIds), deleteUsableMsgId);
        }

        return usableCouponLabelDimensions;
    }

    @Override
    public Result<Boolean> saveUserBehaviorRuleInfo(SaveUserBehaviorRuleRequest request) {
        Result<Boolean> result = new Result<>();
        Objects.requireNonNull(request.getSaveUserBehaviorRuleInfoRequestList(), "Null saveUserBehaviorRuleInfoRequest.");
        List<String> behaviorIds = request.getSaveUserBehaviorRuleInfoRequestList().stream().map(SaveUserBehaviorRuleInfoRequest::getBehaviorId).distinct().collect(Collectors.toList());
        Assert.isTrue(CollectionUtils.isEmpty(labelRuleQueryService.findLabelRules(behaviorIds, true)), "Exist labelRule, behaviorIds=" + JSON.toJSONString(behaviorIds));
        request.getSaveUserBehaviorRuleInfoRequestList().forEach(saveUserBehaviorRuleInfoRequest -> labelRuleCommandService.saveLabelRuleInfo(SaveLabelRuleInfoRequest.builder()
                .ruleName(saveUserBehaviorRuleInfoRequest.getBehaviorName())
                .ruleDesc(saveUserBehaviorRuleInfoRequest.getBehaviorDesc())
                .ruleType(Objects.requireNonNull(RuleTypeEnum.getByName(saveUserBehaviorRuleInfoRequest.getBehaviorRuleOperateType())).name())
                .ruleScriptType(RuleScriptTypeEnum.GROOVY.name())
                .ruleScriptContent(RuleScriptUtils.buildCommonRuleScript(saveUserBehaviorRuleInfoRequest.buildRuleExpression(), labelRuleFieldMappingConfigLoader.getFieldMapping(Objects.requireNonNull(UserBehaviorTypeEnum.getByName(saveUserBehaviorRuleInfoRequest.getBehaviorType())).getContextId())))
                .ruleEffectiveStartTime(saveUserBehaviorRuleInfoRequest.getBehaviorRuleEffectiveStartTime())
                .ruleEffectiveEndTime(saveUserBehaviorRuleInfoRequest.getBehaviorRuleEffectiveEndTime())
                .ruleContextId(Objects.requireNonNull(UserBehaviorTypeEnum.getByName(saveUserBehaviorRuleInfoRequest.getBehaviorType())).getContextId())
                .ruleGroupId(Objects.requireNonNull(UserBehaviorTypeEnum.getByName(saveUserBehaviorRuleInfoRequest.getBehaviorType())).getContextId())
                .labelCode(Objects.requireNonNull(UserBehaviorTypeEnum.getByName(saveUserBehaviorRuleInfoRequest.getBehaviorType())).getLabelCode())
                .labelDimensionKeyId(Objects.requireNonNull(UserBehaviorTypeEnum.getByName(saveUserBehaviorRuleInfoRequest.getBehaviorType())).getLabelDimensionKeyId())
                .labelDimensionKeyDefineType(LabelDimensionKeyDefineTypeEnum.FIXED_KEY.name())
                .labelDimensionFixedKey(saveUserBehaviorRuleInfoRequest.getBehaviorId())
                .externalTags(!CollectionUtils.isEmpty(saveUserBehaviorRuleInfoRequest.getBehaviorTags()) ? Joiner.on(",").join(saveUserBehaviorRuleInfoRequest.getBehaviorTags()) : null)
                .creator("external system")
                .build()));
        return result.success(true);
    }

    @Override
    public Result<Boolean> updateUserBehaviorRuleInfo(UpdateUserBehaviorRuleRequest request) {
        Result<Boolean> result = new Result<>();
        Objects.requireNonNull(request.getUpdateUserBehaviorRuleInfoRequestList(), "Null updateUserBehaviorRuleInfoRequest.");
        List<String> behaviorIds = request.getUpdateUserBehaviorRuleInfoRequestList().stream().map(UpdateUserBehaviorRuleInfoRequest::getBehaviorId).distinct().collect(Collectors.toList());
        Map<String, Long> behaviorIdAndRuleIdMap = labelRuleQueryService.findLabelRules(behaviorIds, false).stream().collect(Collectors.toMap(labelRule -> labelRule.getRuleDefineLabel().getLabelDimensionFixedKey(), LabelRule::getRuleId));
        request.getUpdateUserBehaviorRuleInfoRequestList().forEach(updateUserBehaviorRuleInfoRequest -> {
            if (!behaviorIdAndRuleIdMap.containsKey(updateUserBehaviorRuleInfoRequest.getBehaviorId())) {
                return;
            }
            labelRuleCommandService.saveLabelRuleInfo(SaveLabelRuleInfoRequest.builder()
                    .ruleId(behaviorIdAndRuleIdMap.get(updateUserBehaviorRuleInfoRequest.getBehaviorId()))
                    .modifier(updateUserBehaviorRuleInfoRequest.getModifier())
                    .ruleEffectiveEndTime(updateUserBehaviorRuleInfoRequest.getBehaviorRuleEffectiveEndTime())
                    .build());
        });
        return result.success(true);
    }

    @Override
    public Result<Boolean> deleteUserBehaviorRuleInfo(DeleteUserBehaviorRuleRequest request) {
        Result<Boolean> result = new Result<>();
        Objects.requireNonNull(request.getBehaviorIdList(), "Null behaviorIdList.");
        labelRuleQueryService.findLabelRules(request.getBehaviorIdList(), false).forEach(labelRule -> labelRuleCommandService.deleteLabelRuleInfo(labelRule.getRuleId()));
        return result.success(true);
    }

    private int findTouchFatigue(Long userId, TouchFatigueTypeEnum touchFatigueTypeEnum, String resources) {
        String labelCode = Objects.requireNonNull(touchFatigueTypeEnum).getLabelCode();
        Map<String, String> dataMap = new HashMap<>();
        dataMap.put("resource", resources);
        Pair<Long, String> pair = labelDimensionConfigLoader.buildDimensionKeyIdAndDimensionKey(labelCode, LabelDimensionKeyRequest.builder()
                .dimensionKeyDefineType(LabelDimensionKeyDefineTypeEnum.DYNAMIC_KEY.name())
                .dimensionKeyId(adaptationTouchFatigueCommonConfigLoader.getLabelDimensionKeyId(labelCode))
                .dimensionDynamicKeyParams(dataMap)
                .build());
        return labelAdaptationQueryService.findUserTouchFatigue(userId, labelCode, pair.getLeft(), pair.getRight());
    }

}
