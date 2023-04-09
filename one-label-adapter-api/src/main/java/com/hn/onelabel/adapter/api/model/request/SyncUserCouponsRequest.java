package com.hn.onelabel.adapter.api.model.request;

import com.hn.onelabel.adapter.api.enums.CouponUsageStatusEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SyncUserCouponsRequest {
    private Long userId;
    /**
     * @see CouponUsageStatusEnum
     */
    private String couponUsageStatus;
    private List<SyncUserCouponRequest> syncUserCouponRequestList;
}
