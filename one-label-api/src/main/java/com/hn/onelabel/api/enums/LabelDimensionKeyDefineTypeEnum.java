package com.hn.onelabel.api.enums;

import org.springframework.util.StringUtils;

public enum LabelDimensionKeyDefineTypeEnum {
    FIXED_KEY,
    DYNAMIC_KEY
    ;

    public static LabelDimensionKeyDefineTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (LabelDimensionKeyDefineTypeEnum labelDimensionKeyDefineTypeEnum : values()) {
            if (labelDimensionKeyDefineTypeEnum.name().equals(name)) {
                return labelDimensionKeyDefineTypeEnum;
            }
        }
        return null;
    }

}
