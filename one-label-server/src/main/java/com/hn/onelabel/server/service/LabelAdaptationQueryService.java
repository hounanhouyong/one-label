package com.hn.onelabel.server.service;

import com.hn.onelabel.adapter.api.model.response.RecommendProductResponse;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.LabelDimension;
import org.apache.commons.lang3.tuple.Triple;

import java.util.List;

public interface LabelAdaptationQueryService {

    Triple<List<RecommendProductResponse>, List<LabelDimension>, List<Triple<String, String, Integer>>> findRecommendProduct(Long userId, String labelCode);

    int findUserTouchFatigue(Long userId, String labelCode, Long labelDimensionKeyId, String labelDimensionKey);
}
