package com.hn.onelabel.api.enums;

import org.springframework.util.StringUtils;

public enum RuleContextAttributeLoadFromEnum {
    DEFAULT,
    /**
     * 标签
     */
    LABEL,
    /**
     * 画像
     */
    DMP
    ;

    public static RuleContextAttributeLoadFromEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (RuleContextAttributeLoadFromEnum loadFromEnum : values()) {
            if (loadFromEnum.name().equals(name)) {
                return loadFromEnum;
            }
        }
        return null;
    }

}
