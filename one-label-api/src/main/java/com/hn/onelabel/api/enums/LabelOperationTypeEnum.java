package com.hn.onelabel.api.enums;

import org.springframework.util.StringUtils;

public enum LabelOperationTypeEnum {
    ADD_LABEL,
    DELETE_LABEL
    ;

    public static LabelOperationTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (LabelOperationTypeEnum labelOperationTypeEnum : values()) {
            if (labelOperationTypeEnum.name().equals(name)) {
                return labelOperationTypeEnum;
            }
        }
        return null;
    }
}
