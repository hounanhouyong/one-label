package com.hn.onelabel.api.enums;

import org.springframework.util.StringUtils;

public enum LabelDimensionKeyRelativeInvalidTimeUnitEnum {
    D_1,
    H_1,
    M_1,
    S_1
    ;

    public static LabelDimensionKeyRelativeInvalidTimeUnitEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (LabelDimensionKeyRelativeInvalidTimeUnitEnum labelDimensionKeyRelativeInvalidTimeUnitEnum : values()) {
            if (labelDimensionKeyRelativeInvalidTimeUnitEnum.name().equals(name)) {
                return labelDimensionKeyRelativeInvalidTimeUnitEnum;
            }
        }
        return null;
    }

}
