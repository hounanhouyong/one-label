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
public class PromotionPlanExperimentTouchStatisticsResponse {
    private Long promotionPlanId;
    private Long experimentId;
    private List<ChannelTouchStatistics> channelTouchStatisticsList;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class ChannelTouchStatistics {
        private Integer channelCode;
        private Long totalTouch;
        private Long totalTouchSuccess;
        private Long totalTouchFailure;
        private Long totalNoTouch;
    }
}
