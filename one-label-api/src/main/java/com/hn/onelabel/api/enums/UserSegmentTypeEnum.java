package com.hn.onelabel.api.enums;

import org.springframework.util.StringUtils;

public enum UserSegmentTypeEnum {
    PROMOTION_PLAN
    ;

    public static UserSegmentTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (UserSegmentTypeEnum userSegmentTypeEnum : values()) {
            if (userSegmentTypeEnum.name().equals(name)) {
                return userSegmentTypeEnum;
            }
        }
        return null;
    }
}
