package com.hn.onelabel.api.enums;

import org.springframework.util.StringUtils;

public enum LabelDimensionKeyInvalidTypeEnum {
    ABSOLUTE_TIME,
    RELATIVE_TIME
    ;

    public static LabelDimensionKeyInvalidTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (LabelDimensionKeyInvalidTypeEnum labelDimensionKeyInvalidTypeEnum : values()) {
            if (labelDimensionKeyInvalidTypeEnum.name().equals(name)) {
                return labelDimensionKeyInvalidTypeEnum;
            }
        }
        return null;
    }

}
