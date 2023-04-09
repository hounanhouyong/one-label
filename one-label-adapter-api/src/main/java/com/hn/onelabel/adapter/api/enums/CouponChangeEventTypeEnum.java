package com.hn.onelabel.adapter.api.enums;

import org.springframework.util.StringUtils;

public enum CouponChangeEventTypeEnum {
    LOCKED,
    UNLOCKED,
    CONFIRMED,
    REFUNDED,
    INVALIDED,
    RETURNED
    ;

    public static CouponChangeEventTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (CouponChangeEventTypeEnum couponChangeEventTypeEnum : values()) {
            if (couponChangeEventTypeEnum.name().equals(name)) {
                return couponChangeEventTypeEnum;
            }
        }
        return null;
    }

}
