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
public class ReportPromotionPlanTouchResultRequest {
    /**
     * @see TouchTypeEnum
     */
    private String touchType;
    private String bizJson;
    private List<UserTouchStatus> userTouchStatusList;

    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    @Data
    public static class UserTouchStatus {
        private Long userId;
        private Integer statusCode;
        private String errorMsg;
    }

}
