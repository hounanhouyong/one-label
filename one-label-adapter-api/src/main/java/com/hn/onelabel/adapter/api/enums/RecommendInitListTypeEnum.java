package com.hn.onelabel.adapter.api.enums;

import org.springframework.util.StringUtils;

public enum RecommendInitListTypeEnum {
    DEFAULT,
    USER_IN_THE_INTRODUCTION_PERIOD
    ;

    public static RecommendInitListTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (RecommendInitListTypeEnum recommendInitListTypeEnum : values()) {
            if (recommendInitListTypeEnum.name().equals(name)) {
                return recommendInitListTypeEnum;
            }
        }
        return null;
    }
}
