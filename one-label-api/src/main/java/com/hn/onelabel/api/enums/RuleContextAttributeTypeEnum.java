package com.hn.onelabel.api.enums;

import lombok.Getter;
import org.springframework.util.StringUtils;

public enum RuleContextAttributeTypeEnum {
    STRING("String"),
    INTEGER("Integer"),
    LONG("Long"),
    OBJECT("Object"),
    ARRAY("Array"),
    BOOLEAN("Boolean"),
    DATE("Date"),
    BIGDECIMAL("BigDecimal")
    ;

    @Getter
    private final String code;


    RuleContextAttributeTypeEnum(String code) {
        this.code = code;
    }

    public static RuleContextAttributeTypeEnum getByCode(String code) {
        if (StringUtils.isEmpty(code)) {
            return null;
        }
        for (RuleContextAttributeTypeEnum attributeTypeEnum : values()) {
            if (attributeTypeEnum.getCode().equals(code)) {
                return attributeTypeEnum;
            }
        }
        return null;
    }

}
