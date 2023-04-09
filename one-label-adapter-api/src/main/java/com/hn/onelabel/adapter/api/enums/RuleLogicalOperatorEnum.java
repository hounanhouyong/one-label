package com.hn.onelabel.adapter.api.enums;

import lombok.Getter;
import org.springframework.util.StringUtils;

public enum RuleLogicalOperatorEnum {
    AND("&&"),
    OR("||")
    ;

    @Getter
    private final String symbol;

    RuleLogicalOperatorEnum(String symbol) {
        this.symbol = symbol;
    }

    public static RuleLogicalOperatorEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (RuleLogicalOperatorEnum ruleLogicalOperatorEnum : values()) {
            if (ruleLogicalOperatorEnum.name().equals(name)) {
                return ruleLogicalOperatorEnum;
            }
        }
        return null;
    }
}
