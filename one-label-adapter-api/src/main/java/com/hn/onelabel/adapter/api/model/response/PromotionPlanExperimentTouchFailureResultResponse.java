package com.hn.onelabel.adapter.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class PromotionPlanExperimentTouchFailureResultResponse {
    private Long promotionPlanId;
    private Long experimentId;
    private List<ChannelTouchFailureResult> channelTouchFailureResultList;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class ChannelTouchFailureResult {
        private Integer channelCode;
        private List<Long> userIdList;
        private Object[] sortValues;
    }
}
