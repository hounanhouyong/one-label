package com.hn.onelabel.adapter.api.enums;

import org.springframework.util.StringUtils;

public enum RuleOperateTypeEnum {
    ADD_LABEL_DIMENSION
    ;

    public static RuleOperateTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (RuleOperateTypeEnum ruleOperateTypeEnum : values()) {
            if (ruleOperateTypeEnum.name().equals(name)) {
                return ruleOperateTypeEnum;
            }
        }
        return null;
    }
}
