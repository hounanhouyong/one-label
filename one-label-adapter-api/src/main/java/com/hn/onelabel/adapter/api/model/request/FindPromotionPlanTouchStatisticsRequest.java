package com.hn.onelabel.adapter.api.model.request;

import com.hn.onelabel.adapter.api.enums.TouchTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FindPromotionPlanTouchStatisticsRequest {
    private Long promotionPlanId;
    private List<ExperimentInfo> experimentInfoList;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class ExperimentInfo {
        private Long experimentId;
        /**
         * @see TouchTypeEnum
         */
        private List<Integer> channelCodeList;
    }

}
