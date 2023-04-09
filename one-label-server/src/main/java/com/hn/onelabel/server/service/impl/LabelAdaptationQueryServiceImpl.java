package com.hn.onelabel.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.hn.onelabel.adapter.api.enums.RecommendInitListTypeEnum;
import com.hn.onelabel.adapter.api.model.response.RecommendProductResponse;
import com.hn.onelabel.server.domain.aggregate.userlabel.repository.UserLabelRepository;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.LabelDimension;
import com.hn.onelabel.server.infrastructure.nacos.AdaptationProdRecommendCommonConfigLoader;
import com.hn.onelabel.server.infrastructure.nacos.AdaptationProdRecommendListInitConfigLoader;
import com.hn.onelabel.server.infrastructure.nacos.LogSwitchConfigLoader;
import com.hn.onelabel.server.infrastructure.rpc.UserDmpRpcService;
import com.hn.onelabel.server.service.LabelAdaptationQueryService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class LabelAdaptationQueryServiceImpl implements LabelAdaptationQueryService {

    @Autowired
    private UserDmpRpcService userDmpRpcService;

    @Autowired
    private UserLabelRepository userLabelRepository;

    @Autowired
    private AdaptationProdRecommendListInitConfigLoader adaptationProdRecommendListInitConfigLoader;
    @Autowired
    private AdaptationProdRecommendCommonConfigLoader adaptationProdRecommendCommonConfigLoader;
    @Autowired
    private LogSwitchConfigLoader logSwitchConfigLoader;

    @Override
    public Triple<List<RecommendProductResponse>, List<LabelDimension>, List<Triple<String, String, Integer>>> findRecommendProduct(Long userId, String labelCode) {
        Objects.requireNonNull(userId, "Null userId.");
        Objects.requireNonNull(labelCode, "Null labelCode.");

        List<RecommendProductResponse> responses = new ArrayList<>();

        String categoryPreferenceLabel = userDmpRpcService.findAttributeVal4String(userId, adaptationProdRecommendCommonConfigLoader.getCategoryPreferenceDmpLabelCode());
        Integer userLifeCycle = userDmpRpcService.findAttributeVal4Integer(userId, adaptationProdRecommendCommonConfigLoader.getUserLifeCycleDmpLabelCode());
        if (StringUtils.isEmpty(categoryPreferenceLabel)) {
            if (logSwitchConfigLoader.labelAdaptationQueryServicePrintLogSwitchIsOpen()) {
                log.info("[findRecommendProduct] - No categoryPreferenceLabel, labelCode: {}, userId: {}.", adaptationProdRecommendCommonConfigLoader.getCategoryPreferenceDmpLabelCode(), userId);
            }
            List<Triple<String, String, Integer>> initRankingList = adaptationProdRecommendListInitConfigLoader.getInitRankingList(RecommendInitListTypeEnum.USER_IN_THE_INTRODUCTION_PERIOD);
            initRankingList.forEach(triple -> responses.add(RecommendProductResponse.builder()
                    .productId(triple.getLeft())
                    .productCategory(triple.getMiddle())
                    .rankingNo(triple.getRight())
                    .build()));
            return Triple.of(responses, new ArrayList<>(), initRankingList);
        }

        List<Triple<String, String, Integer>> initRankingList = adaptationProdRecommendListInitConfigLoader.getInitRankingList(userLifeCycle);

        List<LabelDimension> labelDimensions = userLabelRepository.findUserLabelDimensions(userId, labelCode, true, false);
        if (CollectionUtils.isEmpty(labelDimensions)) {
            if (logSwitchConfigLoader.labelAdaptationQueryServicePrintLogSwitchIsOpen()) {
                log.info("[findRecommendProduct] - No labelDimensions, init..., labelCode: {}, userId: {}.", labelCode, userId);
            }
            initRankingList.forEach(triple -> {
                if (categoryPreferenceLabel.equals(triple.getMiddle())) {
                    responses.add(RecommendProductResponse.builder()
                            .productId(triple.getLeft())
                            .productCategory(categoryPreferenceLabel)
                            .rankingNo(triple.getRight())
                            .build());
                }
            });
            return Triple.of(responses, new ArrayList<>(), initRankingList);
        } else {
            if (logSwitchConfigLoader.labelAdaptationQueryServicePrintLogSwitchIsOpen()) {
                log.info("[findRecommendProduct] - labelDimensions: {}, categoryPreferenceLabel: {}, userId: {}.", JSON.toJSONString(labelDimensions), categoryPreferenceLabel, userId);
            }
            List<String> labelDimensionKeyList = labelDimensions.stream().map(LabelDimension::getDimensionKey).distinct().collect(Collectors.toList());
            List<LabelDimension> hitLabelDimensions = labelDimensions.stream().filter(labelDimension -> categoryPreferenceLabel.equals(adaptationProdRecommendListInitConfigLoader.getCategoryByProductId(userLifeCycle, labelDimension.getDimensionKey()))).collect(Collectors.toList());
            if (!CollectionUtils.isEmpty(hitLabelDimensions)) {
                if (logSwitchConfigLoader.labelAdaptationQueryServicePrintLogSwitchIsOpen()) {
                    log.info("[findRecommendProduct] - hit labelDimensions: {}, categoryPreferenceLabel: {}, userId: {}.", JSON.toJSONString(hitLabelDimensions), categoryPreferenceLabel, userId);
                }
                hitLabelDimensions.forEach(hitLabelDimension -> responses.add(RecommendProductResponse.builder()
                        .productId(hitLabelDimension.getDimensionKey())
                        .productCategory(categoryPreferenceLabel)
                        .rankingNo(Integer.parseInt(hitLabelDimension.getDimensionVal()))
                        .build()));
            }

            Iterator<Triple<String, String, Integer>> it = initRankingList.iterator();
            while (it.hasNext()) {
                Triple<String, String, Integer> item = it.next();
                if (labelDimensionKeyList.contains(item.getLeft())) {
                    it.remove();
                } else {
                    if (categoryPreferenceLabel.equals(item.getMiddle())) {
                        responses.add(RecommendProductResponse.builder()
                                .productId(item.getLeft())
                                .productCategory(item.getMiddle())
                                .rankingNo(item.getRight())
                                .build());
                    } else {
                        it.remove();
                    }
                }
            }

            return Triple.of(responses, labelDimensions, initRankingList);
        }
    }

    @Override
    public int findUserTouchFatigue(Long userId, String labelCode, Long labelDimensionKeyId, String labelDimensionKey) {
        Objects.requireNonNull(userId, "Null userId.");
        Objects.requireNonNull(labelCode, "Null labelCode.");
        Objects.requireNonNull(labelDimensionKeyId, "Null labelDimensionKeyId.");
        Objects.requireNonNull(labelDimensionKey, "Null labelDimensionKey.");
        LabelDimension labelDimension = userLabelRepository.findUserLabelDimension(userId, labelCode, labelDimensionKeyId, labelDimensionKey, true);
        if (Objects.isNull(labelDimension) || StringUtils.isEmpty(labelDimension.getDimensionVal())) {
            return 0;
        }
        return Integer.parseInt(labelDimension.getDimensionVal());
    }

}
