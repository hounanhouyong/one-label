package com.hn.onelabel.api.enums;

import org.springframework.util.StringUtils;

public enum RuleScriptTypeEnum {
    GROOVY,
    SQL,
    DSL
    ;

    public static RuleScriptTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (RuleScriptTypeEnum ruleScriptTypeEnum : values()) {
            if (ruleScriptTypeEnum.name().equals(name)) {
                return ruleScriptTypeEnum;
            }
        }
        return null;
    }

}
