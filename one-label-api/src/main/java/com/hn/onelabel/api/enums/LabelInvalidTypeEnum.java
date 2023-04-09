package com.hn.onelabel.api.enums;

import org.springframework.util.StringUtils;

public enum LabelInvalidTypeEnum {
    PERMANENT,
    TEMPORARY
    ;

    public static LabelInvalidTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (LabelInvalidTypeEnum labelInvalidTypeEnum : values()) {
            if (labelInvalidTypeEnum.name().equals(name)) {
                return labelInvalidTypeEnum;
            }
        }
        return null;
    }

}
