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
public class UserUsableCouponTemplatesResponse {
    private Boolean hasSyncUsableCoupon;
    private List<Long> usableCouponTemplates;
}
