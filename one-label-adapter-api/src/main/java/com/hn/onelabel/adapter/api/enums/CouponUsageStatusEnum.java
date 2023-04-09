package com.hn.onelabel.adapter.api.enums;

import lombok.Getter;
import org.springframework.util.StringUtils;

public enum CouponUsageStatusEnum {

    USABLE("usable_coupons")
    ;

    @Getter
    private final String labelCode;

    CouponUsageStatusEnum(String labelCode) {
        this.labelCode = labelCode;
    }

    public static CouponUsageStatusEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (CouponUsageStatusEnum couponUsageStatusEnum : values()) {
            if (couponUsageStatusEnum.name().equals(name)) {
                return couponUsageStatusEnum;
            }
        }
        return null;
    }

}
