package com.hn.onelabel.adapter.api.enums;

import lombok.Getter;
import org.springframework.util.StringUtils;

public enum FieldValueDataTypeEnum {
    STRING("String"),
    INTEGER("Integer"),
    LONG("Long"),
    BIGDECIMAL("BigDecimal"),
    BOOLEAN("Boolean"),
    LIST_STRING("List<String>"),
    LIST_INTEGER("List<Integer>"),
    LIST_LONG("List<Long>"),
    LIST_BIGDECIMAL("List<BigDecimal>")
    ;

    @Getter
    private final String dataType;

    FieldValueDataTypeEnum(String dataType) {
        this.dataType = dataType;
    }

    public static FieldValueDataTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (FieldValueDataTypeEnum fieldValueDataTypeEnum : values()) {
            if (fieldValueDataTypeEnum.name().equals(name)) {
                return fieldValueDataTypeEnum;
            }
        }
        return null;
    }

    public static boolean isCollectionDataType(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return name.equals(LIST_STRING.name()) || name.equals(LIST_INTEGER.name()) || name.equals(LIST_LONG.name()) || name.equals(LIST_BIGDECIMAL.name());
    }
}
