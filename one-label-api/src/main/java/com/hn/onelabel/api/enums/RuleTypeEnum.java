package com.hn.onelabel.api.enums;

import org.springframework.util.StringUtils;

public enum RuleTypeEnum {
    ADD_LABEL,
    DELETE_LABEL,
    ADD_LABEL_DIMENSION
    ;

    public static RuleTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (RuleTypeEnum ruleTypeEnum : values()) {
            if (ruleTypeEnum.name().equals(name)) {
                return ruleTypeEnum;
            }
        }
        return null;
    }

    public static boolean isLabelDimensionRuleType(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return name.equals(ADD_LABEL_DIMENSION.name());
    }

}
