package com.hn.onelabel.adapter.api.enums;

import lombok.Getter;
import org.springframework.util.StringUtils;

public enum RecommendTypeEnum {
    RECOMMEND_ORDER_POSITION("recommend_list_order_position")
    ;

    @Getter
    private final String labelCode;

    RecommendTypeEnum(String labelCode) {
        this.labelCode = labelCode;
    }

    public static RecommendTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (RecommendTypeEnum recommendTypeEnum : values()) {
            if (recommendTypeEnum.name().equals(name)) {
                return recommendTypeEnum;
            }
        }
        return null;
    }
}
