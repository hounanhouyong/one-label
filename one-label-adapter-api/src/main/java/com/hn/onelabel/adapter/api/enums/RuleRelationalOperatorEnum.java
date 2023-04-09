package com.hn.onelabel.adapter.api.enums;

import lombok.Getter;
import org.springframework.util.StringUtils;

public enum RuleRelationalOperatorEnum {
    EQUAL("==", "", "", "", "", "", "", "", ""),
    NOT_EQUAL("!=", "", "","", "", "", "", "", ""),
    GT(">", "", "","", "","", "", "", ""),
    GTE(">=", "", "","", "", "", "", "", ""),
    LT("<", "", "","", "", "", "", "", ""),
    LTE("<=", "", "","", "", "", "", "", ""),
    EQUALS("equals", "\\.", ".","", "", "\\(", "(", "\\)", ")"),
    IN("in", " ", " ","", "", " ", " ", "",""),
    EVERY_IN("every", "\\.", ".","", "", " \\{ it in ", " { it in ", " \\}"," }"),
    CONTAINS_ALL("containsAll", "\\.", ".", "", "", "\\(", "(", "\\)", ")")
    ;

    @Getter
    private final String symbol;
    @Getter
    private final String symbolPrefixRegex;
    @Getter
    private final String symbolPrefix;
    @Getter
    private final String fieldPrefixRegex;
    @Getter
    private final String fieldPrefix;
    @Getter
    private final String valuePrefixRegex;
    @Getter
    private final String valuePrefix;
    @Getter
    private final String valueSuffixRegex;
    @Getter
    private final String valueSuffix;

    RuleRelationalOperatorEnum(String symbol, String symbolPrefixRegex, String symbolPrefix, String fieldPrefixRegex, String fieldPrefix, String valuePrefixRegex, String valuePrefix, String valueSuffixRegex, String valueSuffix) {
        this.symbol = symbol;
        this.symbolPrefixRegex = symbolPrefixRegex;
        this.symbolPrefix = symbolPrefix;
        this.fieldPrefixRegex = fieldPrefixRegex;
        this.fieldPrefix = fieldPrefix;
        this.valuePrefixRegex = valuePrefixRegex;
        this.valuePrefix = valuePrefix;
        this.valueSuffixRegex = valueSuffixRegex;
        this.valueSuffix = valueSuffix;
    }

    public static RuleRelationalOperatorEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (RuleRelationalOperatorEnum ruleRelationalOperatorEnum : values()) {
            if (ruleRelationalOperatorEnum.name().equals(name)) {
                return ruleRelationalOperatorEnum;
            }
        }
        return null;
    }

    public static boolean isCollectionRelationalOperator(String name) {
        if (StringUtils.isEmpty(name)) {
            return false;
        }
        return name.equals(IN.name()) || name.equals(EVERY_IN.name()) || name.equals(CONTAINS_ALL.name());
    }

    public static RuleRelationalOperatorEnum ruleExpressionContainsRelationalOperator(String ruleExpression) {
        if (StringUtils.isEmpty(ruleExpression)) {
            return null;
        }
        for (RuleRelationalOperatorEnum ruleRelationalOperatorEnum : values()) {
            if (ruleExpression.indexOf("@" + ruleRelationalOperatorEnum.getSymbol() + "@") > 0) {
                return ruleRelationalOperatorEnum;
            }
        }
        return null;
    }
}
