package com.hn.onelabel.server.common.utils;

import com.google.common.base.Joiner;
import com.hn.onelabel.adapter.api.enums.RuleLogicalOperatorEnum;
import com.hn.onelabel.adapter.api.enums.RuleRelationalOperatorEnum;
import com.hn.onelabel.api.enums.RuleContextAttributeTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.util.CollectionUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Slf4j
public class RuleScriptUtils {

    private static final String GROOVY_SCRIPT_TEMPLATE_FOR_EQUATION_RULE = "class AbstractionComputeScript {  public boolean compute(def context) {    if (%s) {      return true;    } else {      return false;    }}}";
    private static final String GROOVY_SCRIPT_TEMPLATE_FOR_COMMON_RULE = "class AbstractionComputeScript {  public boolean compute(def context) {  %s  if (%s) {      return true;    } else {      return false;    }}}";

    public static String buildRuleScript4equation(String ruleExpression, Map<String, Pair<String, String>> fieldMapping) {
        if (CollectionUtils.isEmpty(fieldMapping)) {
            log.error("No fieldMapping config.");
            return null;
        }
        List<String> expressions = new ArrayList<>();
        String[] subRuleExpressions = ruleExpression.split("&&");
        for (String subRuleExpression : subRuleExpressions) {
            expressions.add(buildRuleExpression4equation(subRuleExpression, fieldMapping));
        }
        return String.format(GROOVY_SCRIPT_TEMPLATE_FOR_EQUATION_RULE, Joiner.on(" && ").join(expressions));
    }

    /**
     * <RuleLogicalOperatorEnum, ruleExpression, defineExpression>
     */
    public static String buildCommonRuleScript(Triple<RuleLogicalOperatorEnum, String, Pair<String, List<String>>> triple, Map<String, Pair<String, String>> fieldMapping) {
        if (CollectionUtils.isEmpty(fieldMapping)) {
            log.error("No fieldMapping config.");
            return null;
        }
        List<String> expressions = new ArrayList<>();
        String[] subRules = triple.getMiddle().split(triple.getLeft().getSymbol());
        for (String subRule : subRules) {
            expressions.add(buildRuleExpression4common(subRule, triple.getRight().getRight(), fieldMapping));
        }
        return String.format(GROOVY_SCRIPT_TEMPLATE_FOR_COMMON_RULE, !Objects.isNull(triple.getRight().getLeft()) ? triple.getRight().getLeft() : "", Joiner.on(" " + triple.getLeft().getSymbol() + " ").join(expressions));
    }

    private static String buildRuleExpression4equation(String subRuleExpression, Map<String, Pair<String, String>> fieldMapping) {
        List<String> subExpressions = new ArrayList<>();
        String[] subSubRuleExpressions = subRuleExpression.split("\\|\\|");
        for (String subSubRuleExpression : subSubRuleExpressions) {
            subSubRuleExpression = subSubRuleExpression.replace("(", "");
            subSubRuleExpression = subSubRuleExpression.replace(")", "");
            String[] equation = subSubRuleExpression.split("=");
            String key = equation[0];
            String val = equation[1];
            key = key.replaceAll(" ", "");
            val = val.replaceAll(" ", "");
            val = val.replaceAll("'", "");
            if (fieldMapping.containsKey(key)) {
                Pair<String, String> pair = fieldMapping.get(key);
                StringBuilder expressionStringBuilder = new StringBuilder("(").append(pair.getLeft()).append("==");
                buildValue(expressionStringBuilder, pair.getRight(), val, new ArrayList<>());
                expressionStringBuilder.append(")");
                subExpressions.add(expressionStringBuilder.toString());
            }
        }
        if (subExpressions.size() == 1) {
            return subExpressions.get(0);
        }
        return "( " + Joiner.on(" || ").join(subExpressions) + " )";
    }

    private static String buildRuleExpression4common(String subRuleExpression, List<String> defineKeyList, Map<String, Pair<String, String>> fieldMapping) {
        subRuleExpression = subRuleExpression.trim();
        subRuleExpression = subRuleExpression.replaceFirst("\\(", "");
        if (String.valueOf(subRuleExpression.charAt(subRuleExpression.length() - 1)).equals(")")) {
            subRuleExpression = subRuleExpression.substring(0, subRuleExpression.length() - 1);
        }
        Triple<RuleRelationalOperatorEnum, String ,String> triple = getFieldAndValue(subRuleExpression);
        if (fieldMapping.containsKey(triple.getMiddle())) {
            Pair<String, String> pair = fieldMapping.get(triple.getMiddle());
            StringBuilder expressionStringBuilder = new StringBuilder("(")
                    .append(pair.getLeft())
                    .append(triple.getLeft().getSymbolPrefix())
                    .append(triple.getLeft().getSymbol())
                    .append(triple.getLeft().getValuePrefix());
            buildValue(expressionStringBuilder, pair.getRight(), triple.getRight(), defineKeyList);
            expressionStringBuilder.append(triple.getLeft().getValueSuffix());
            expressionStringBuilder.append(")");
            return expressionStringBuilder.toString();
        } else {
            return "(" +
                    triple.getMiddle() +
                    triple.getLeft().getSymbolPrefix() +
                    triple.getLeft().getSymbol() +
                    triple.getLeft().getValuePrefix() +
                    triple.getRight() +
                    triple.getLeft().getValueSuffix() +
                    ")";
        }
    }

    private static Triple<RuleRelationalOperatorEnum, String ,String> getFieldAndValue(String subRuleExpression) {
        RuleRelationalOperatorEnum relationalOperatorEnum = Objects.requireNonNull(RuleRelationalOperatorEnum.ruleExpressionContainsRelationalOperator(subRuleExpression));
        String regex = relationalOperatorEnum.getSymbolPrefixRegex() + "@" + relationalOperatorEnum.getSymbol() + "@" + relationalOperatorEnum.getValuePrefixRegex();
        String[] fieldAndValue = subRuleExpression.split(regex);
        String field = fieldAndValue[0];
        field = field.replaceAll(" ", "");
        String value = "";
        if (fieldAndValue.length > 1) {
            value = fieldAndValue[1];
            value = value.replace(relationalOperatorEnum.getValueSuffix(), "");
            value = value.replaceAll(" ", "");
            value = value.replaceAll("'", "");
        }
        return Triple.of(relationalOperatorEnum, field, value);
    }

    private static void buildValue(StringBuilder expressionStringBuilder, String valueAttributeType, String value, List<String> defineKeyList) {
        if (!CollectionUtils.isEmpty(defineKeyList) && defineKeyList.contains(value)) {
            expressionStringBuilder.append(value);
            return;
        }
        switch (Objects.requireNonNull(RuleContextAttributeTypeEnum.getByCode(valueAttributeType))) {
            case STRING:
            case DATE:
                expressionStringBuilder.append("'").append(value).append("'");
                break;
            case LONG:
                expressionStringBuilder.append(Long.parseLong(value));
                break;
            case INTEGER:
                expressionStringBuilder.append(Integer.parseInt(value));
                break;
            case BOOLEAN:
                expressionStringBuilder.append(value);
                break;
            case BIGDECIMAL:
                expressionStringBuilder.append(new BigDecimal(value));
                break;
        }
    }

}
