package com.hn.onelabel.adapter.api.model.request;

import com.hn.onelabel.adapter.api.enums.CouponChangeEventTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SyncUserCouponChangeEventRequest {
    private Long userId;
    /**
     * @see CouponChangeEventTypeEnum
     */
    private String couponChangeEventType;
    private Long couponAssetId;
    private Long couponEventLogId;
}
