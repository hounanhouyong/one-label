package com.hn.onelabel.api.enums;

import org.springframework.util.StringUtils;

public enum RuleContextAttributeLoadTypeEnum {
    DEFAULT,
    PRE_LOAD
    ;

    public static RuleContextAttributeLoadTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (RuleContextAttributeLoadTypeEnum loadTypeEnum : values()) {
            if (loadTypeEnum.name().equals(name)) {
                return loadTypeEnum;
            }
        }
        return null;
    }
}
