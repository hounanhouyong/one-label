package com.hn.onelabel.api.enums;

import org.springframework.util.StringUtils;

public enum LabelDimensionOperationTypeEnum {
    ADD_LABEL_DIMENSION,
    DELETE_LABEL_DIMENSION
    ;

    public static LabelDimensionOperationTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (LabelDimensionOperationTypeEnum labelDimensionOperationTypeEnum : values()) {
            if (labelDimensionOperationTypeEnum.name().equals(name)) {
                return labelDimensionOperationTypeEnum;
            }
        }
        return null;
    }
}
