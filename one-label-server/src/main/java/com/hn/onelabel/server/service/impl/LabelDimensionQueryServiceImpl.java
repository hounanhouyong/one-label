package com.hn.onelabel.server.service.impl;

import com.hn.onelabel.adapter.api.enums.TouchTypeEnum;
import com.hn.onelabel.adapter.api.model.request.FindPromotionPlanTouchResultDetailsRequest;
import com.hn.onelabel.adapter.api.model.request.FindPromotionPlanTouchStatisticsRequest;
import com.hn.onelabel.adapter.api.model.response.PromotionPlanExperimentTouchFailureResultResponse;
import com.hn.onelabel.adapter.api.model.response.PromotionPlanExperimentTouchStatisticsResponse;
import com.hn.onelabel.api.enums.LabelDimensionKeyDefineTypeEnum;
import com.hn.onelabel.api.model.request.FindUserLabelDimensionsRequest;
import com.hn.onelabel.api.model.request.FindUserLabelDimensionsRequestV2;
import com.hn.onelabel.api.model.request.LabelDimensionKeyRequest;
import com.hn.onelabel.api.model.response.LabelDimensionResponse;
import com.hn.onelabel.server.domain.aggregate.userlabel.repository.UserLabelRepository;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.LabelDimension;
import com.hn.onelabel.server.infrastructure.nacos.AdaptationPromotionPlanTouchCommonConfigLoader;
import com.hn.onelabel.server.infrastructure.nacos.LabelDimensionConfigLoader;
import com.hn.onelabel.server.service.LabelDimensionQueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.lucene.search.BooleanClause;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LabelDimensionQueryServiceImpl implements LabelDimensionQueryService {

    @Autowired
    private UserLabelRepository userLabelRepository;

    @Autowired
    private AdaptationPromotionPlanTouchCommonConfigLoader adaptationPromotionPlanTouchCommonConfigLoader;
    @Autowired
    private LabelDimensionConfigLoader labelDimensionConfigLoader;

    @Override
    public List<LabelDimensionResponse> findLabelDimensions(FindUserLabelDimensionsRequest request, boolean used, boolean syncEsDataToRedis) {
        return userLabelRepository.findUserLabelDimensions(request.getUserId(), request.getLabelCode(), used, syncEsDataToRedis).stream().map(labelDimension -> LabelDimensionResponse.builder()
                .dimensionKeyId(labelDimension.getDimensionKeyId())
                .dimensionKey(labelDimension.getDimensionKey())
                .dimensionVal(labelDimension.getDimensionVal())
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<Pair<String, List<LabelDimensionResponse>>> findLabelDimensions(FindUserLabelDimensionsRequestV2 request, boolean used, boolean syncEsDataToRedis) {
        List<Pair<String, List<LabelDimensionResponse>>> responses = new ArrayList<>();
        userLabelRepository.findUserLabelDimensions(request.getUserId(), request.getLabelCodes(), used, syncEsDataToRedis).stream().collect(Collectors.groupingBy(LabelDimension::getLabelCode)).forEach((labelCode, labelDimensions) -> {
            responses.add(Pair.of(labelCode, labelDimensions.stream().map(labelDimension -> LabelDimensionResponse.builder()
                    .dimensionKeyId(labelDimension.getDimensionKeyId())
                    .dimensionKey(labelDimension.getDimensionKey())
                    .dimensionVal(labelDimension.getDimensionVal())
                    .build()).collect(Collectors.toList())));
        });
        return responses;
    }

    @Override
    public List<PromotionPlanExperimentTouchStatisticsResponse> findPromotionPlanTouchStatistics(FindPromotionPlanTouchStatisticsRequest request) {
        return request.getExperimentInfoList().stream().map(experimentInfo -> PromotionPlanExperimentTouchStatisticsResponse.builder()
                .promotionPlanId(request.getPromotionPlanId())
                .experimentId(experimentInfo.getExperimentId())
                .channelTouchStatisticsList(experimentInfo.getChannelCodeList().stream().map(channelCode -> {

                    TouchTypeEnum touchTypeEnum = Objects.requireNonNull(TouchTypeEnum.getByChannelCode(channelCode));

                    String labelCode = touchTypeEnum.getLabelCode();
                    Long labelDimensionKeyId = adaptationPromotionPlanTouchCommonConfigLoader.getLabelDimensionKeyId(labelCode);

                    Map<String, String> dataMap = new HashMap<>();
                    dataMap.put("planId", String.valueOf(request.getPromotionPlanId()));
                    dataMap.put("experimentId", String.valueOf(experimentInfo.getExperimentId()));

                    LabelDimensionKeyRequest labelDimensionKeyRequest = LabelDimensionKeyRequest.builder()
                            .dimensionKeyDefineType(LabelDimensionKeyDefineTypeEnum.DYNAMIC_KEY.name())
                            .dimensionKeyId(labelDimensionKeyId)
                            .dimensionFixedKey(null)
                            .dimensionDynamicKeyParams(dataMap)
                            .build();

                    Pair<Long, String> pair = labelDimensionConfigLoader.buildDimensionKeyIdAndDimensionKey(labelCode, labelDimensionKeyRequest);

                    Long totalTouch = userLabelRepository.countLabelDimensions(labelCode, pair.getRight());
                    Long totalTouchSuccess = userLabelRepository.countLabelDimensions(labelCode, pair.getRight(), "1");
                    Long totalNoTouch = userLabelRepository.countLabelDimensions(labelCode, pair.getRight(), "0");
                    Long totalTouchFailure = totalTouch - totalNoTouch - totalTouchSuccess;

                    return PromotionPlanExperimentTouchStatisticsResponse.ChannelTouchStatistics.builder()
                            .channelCode(touchTypeEnum.getChannelCode())
                            .totalTouch(totalTouch)
                            .totalNoTouch(totalNoTouch)
                            .totalTouchSuccess(totalTouchSuccess)
                            .totalTouchFailure(totalTouchFailure)
                            .build();

                }).collect(Collectors.toList()))
                .build()).collect(Collectors.toList());
    }

    @Override
    public List<PromotionPlanExperimentTouchFailureResultResponse> findPromotionPlanTouchFailureResult(FindPromotionPlanTouchResultDetailsRequest request) {
        return request.getExperimentQueryInfoList().stream().map(experimentQueryInfo -> PromotionPlanExperimentTouchFailureResultResponse.builder()
                .promotionPlanId(request.getPromotionPlanId())
                .experimentId(experimentQueryInfo.getExperimentId())
                .channelTouchFailureResultList(experimentQueryInfo.getChannelQueryInfoList().stream().map(channelQueryInfo -> {

                    String labelCode = Objects.requireNonNull(TouchTypeEnum.getByChannelCode(channelQueryInfo.getChannelCode())).getLabelCode();
                    Long labelDimensionKeyId = adaptationPromotionPlanTouchCommonConfigLoader.getLabelDimensionKeyId(labelCode);

                    Map<String, String> dataMap = new HashMap<>();
                    dataMap.put("planId", String.valueOf(request.getPromotionPlanId()));
                    dataMap.put("experimentId", String.valueOf(experimentQueryInfo.getExperimentId()));

                    LabelDimensionKeyRequest labelDimensionKeyRequest = LabelDimensionKeyRequest.builder()
                            .dimensionKeyDefineType(LabelDimensionKeyDefineTypeEnum.DYNAMIC_KEY.name())
                            .dimensionKeyId(labelDimensionKeyId)
                            .dimensionDynamicKeyParams(dataMap)
                            .build();

                    Pair<Long, String> pair = labelDimensionConfigLoader.buildDimensionKeyIdAndDimensionKey(labelCode, labelDimensionKeyRequest);

                    Pair<List<Long>, Object[]> resultPair = userLabelRepository.findUserIdsByConditionsAndPageBySearchAfter(labelCode, pair.getRight(), "1", BooleanClause.Occur.MUST_NOT, request.getPageSize(), channelQueryInfo.getSortValues());

                    return PromotionPlanExperimentTouchFailureResultResponse.ChannelTouchFailureResult.builder()
                            .channelCode(channelQueryInfo.getChannelCode())
                            .userIdList(Objects.isNull(resultPair) ? new ArrayList<>() : resultPair.getLeft())
                            .sortValues(Objects.isNull(resultPair) ? null : resultPair.getRight())
                            .build();

                }).collect(Collectors.toList()))
                .build()).collect(Collectors.toList());
    }
}
