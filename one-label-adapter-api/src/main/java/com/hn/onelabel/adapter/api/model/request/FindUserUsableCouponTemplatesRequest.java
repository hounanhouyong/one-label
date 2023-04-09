package com.hn.onelabel.adapter.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FindUserUsableCouponTemplatesRequest {
    private Long userId;
    private List<Long> couponTemplates;
}
