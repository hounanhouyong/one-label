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
public class FindPromotionPlanTouchResultDetailsRequest {
    private Long promotionPlanId;
    private List<ExperimentQueryInfo> experimentQueryInfoList;
    private Integer pageSize = 500;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class ExperimentQueryInfo {
        private Long experimentId;
        private List<ChannelQueryInfo> channelQueryInfoList;
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class ChannelQueryInfo {
        /**
         * @see TouchTypeEnum
         */
        private Integer channelCode;
        private Object[] sortValues;
    }

}
