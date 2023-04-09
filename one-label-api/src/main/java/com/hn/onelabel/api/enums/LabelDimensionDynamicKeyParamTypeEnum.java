package com.hn.onelabel.api.enums;

import org.springframework.util.StringUtils;

public enum LabelDimensionDynamicKeyParamTypeEnum {
    JAVA_LANG,
    DATE_INT
    ;

    public static LabelDimensionDynamicKeyParamTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (LabelDimensionDynamicKeyParamTypeEnum labelDimensionDynamicKeyParamTypeEnum : values()) {
            if (labelDimensionDynamicKeyParamTypeEnum.name().equals(name)) {
                return labelDimensionDynamicKeyParamTypeEnum;
            }
        }
        return null;
    }

}
