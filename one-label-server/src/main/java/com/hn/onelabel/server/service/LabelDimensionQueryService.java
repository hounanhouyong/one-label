package com.hn.onelabel.server.service;

import com.hn.onelabel.adapter.api.model.request.FindPromotionPlanTouchResultDetailsRequest;
import com.hn.onelabel.adapter.api.model.request.FindPromotionPlanTouchStatisticsRequest;
import com.hn.onelabel.adapter.api.model.response.PromotionPlanExperimentTouchFailureResultResponse;
import com.hn.onelabel.adapter.api.model.response.PromotionPlanExperimentTouchStatisticsResponse;
import com.hn.onelabel.api.model.request.FindUserLabelDimensionsRequest;
import com.hn.onelabel.api.model.request.FindUserLabelDimensionsRequestV2;
import com.hn.onelabel.api.model.response.LabelDimensionResponse;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;

public interface LabelDimensionQueryService {

    List<LabelDimensionResponse> findLabelDimensions(FindUserLabelDimensionsRequest request, boolean used, boolean syncEsDataToRedis);

    List<Pair<String, List<LabelDimensionResponse>>> findLabelDimensions(FindUserLabelDimensionsRequestV2 request, boolean used, boolean syncEsDataToRedis);

    List<PromotionPlanExperimentTouchStatisticsResponse> findPromotionPlanTouchStatistics(FindPromotionPlanTouchStatisticsRequest request);

    List<PromotionPlanExperimentTouchFailureResultResponse> findPromotionPlanTouchFailureResult(FindPromotionPlanTouchResultDetailsRequest request);
}
