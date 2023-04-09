package com.hn.onelabel.adapter.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SyncUserCouponRequest {
    private Long couponAssetId;
    private String couponAssetInfo;
}
