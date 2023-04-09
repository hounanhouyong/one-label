package com.hn.onelabel.adapter.api.feign;

import com.hn.onelabel.adapter.api.common.Result;
import com.hn.onelabel.adapter.api.model.request.*;
import com.hn.onelabel.adapter.api.model.response.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "one-label",
        url = "${xxx}",
        decode404 = true,
        contextId = "LabelAdaptationService"
)
public interface LabelAdaptationService {

    String PREFIX = "/userlabel/adapter";

    @PostMapping(PREFIX + "/findRecommendProduct")
    Result<List<RecommendProductResponse>> findRecommendProduct(@RequestBody FindRecommendProductRequest request);

    @PostMapping(PREFIX + "/findUserTouchFatigue")
    Result<Integer> findUserTouchFatigue(@RequestBody FindUserTouchFatigueRequest request);

    @PostMapping(PREFIX + "/findUserTouchFatigueAndIncr")
    Result<Integer> findUserTouchFatigueAndIncr(@RequestBody FindUserTouchFatigueRequest request);

    @PostMapping(PREFIX + "/findPromotionPlanTouchStatistics")
    Result<List<PromotionPlanExperimentTouchStatisticsResponse>> findPromotionPlanTouchStatistics(@RequestBody FindPromotionPlanTouchStatisticsRequest request);

    @PostMapping(PREFIX + "/findPromotionPlanTouchFailureResult")
    Result<List<PromotionPlanExperimentTouchFailureResultResponse>> findPromotionPlanTouchFailureResult(@RequestBody FindPromotionPlanTouchResultDetailsRequest request);

    @PostMapping(PREFIX + "/saveUserSegmentRuleInfo")
    Result<Boolean> saveUserSegmentRuleInfo(@RequestBody SaveUserSegmentRuleInfoRequest request);

    @PostMapping(PREFIX + "/findUserSegmentInfo")
    Result<List<UserSegmentResponse>> findUserSegmentInfo(@RequestBody FindUserSegmentInfoRequest request);

    @PostMapping(PREFIX + "/findUserAbExperimentGroupInfo")
    Result<List<UserAbExperimentGroupResponse>> findUserAbExperimentGroupInfo(@RequestBody FindUserAbExperimentGroupRequest request);

    @PostMapping(PREFIX + "/findUserUsableCoupons")
    Result<List<String>> findUserUsableCoupons(@RequestBody FindUserUsableCouponsRequest request);

    @PostMapping(PREFIX + "/saveUserBehaviorRuleInfo")
    Result<Boolean> saveUserBehaviorRuleInfo(@RequestBody SaveUserBehaviorRuleRequest request);

    @PostMapping(PREFIX + "/updateUserBehaviorRuleInfo")
    Result<Boolean> updateUserBehaviorRuleInfo(@RequestBody UpdateUserBehaviorRuleRequest request);

    @PostMapping(PREFIX + "/deleteUserBehaviorRuleInfo")
    Result<Boolean> deleteUserBehaviorRuleInfo(@RequestBody DeleteUserBehaviorRuleRequest request);

    @PostMapping(PREFIX + "/findUserUsableCouponTemplates")
    Result<UserUsableCouponTemplatesResponse> findUserUsableCouponTemplates(@RequestBody FindUserUsableCouponTemplatesRequest request);
}
