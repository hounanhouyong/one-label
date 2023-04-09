package com.hn.onelabel.api.enums;

import org.springframework.util.StringUtils;

public enum DatasourceEnum {
    REDIS,
    ES,
    HOLO
    ;

    public static DatasourceEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (DatasourceEnum datasourceEnum : values()) {
            if (datasourceEnum.name().equals(name)) {
                return datasourceEnum;
            }
        }
        return null;
    }
}
