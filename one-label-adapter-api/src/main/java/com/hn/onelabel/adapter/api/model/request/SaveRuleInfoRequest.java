package com.hn.onelabel.adapter.api.model.request;

import com.alibaba.fastjson.JSON;
import com.google.common.base.Joiner;
import com.hn.onelabel.adapter.api.enums.FieldValueDataTypeEnum;
import com.hn.onelabel.adapter.api.enums.RuleLogicalOperatorEnum;
import com.hn.onelabel.adapter.api.enums.RuleRelationalOperatorEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicInteger;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SaveRuleInfoRequest {
    /**
     * @see RuleLogicalOperatorEnum
     */
    private String logicalOperator;
    private List<SaveRuleConditionInfoRequest> ruleConditions;

    public boolean paramsIsValid() {
        Objects.requireNonNull(RuleLogicalOperatorEnum.getByName(this.logicalOperator), "Error logicalOperator.");
        Assert.isTrue(!CollectionUtils.isEmpty(this.ruleConditions), "Null ruleConditions.");
        Assert.isTrue(this.ruleConditions.stream().allMatch(SaveRuleConditionInfoRequest::isValid), "Error ruleConditions.");
        return true;
    }

    public Triple<RuleLogicalOperatorEnum, String, Pair<String, List<String>>> buildRuleExpression() {
        this.paramsIsValid();
        List<String> defineList = new ArrayList<>();
        List<String> defineKeyList = new ArrayList<>();
        List<String> expressions = new ArrayList<>();
        AtomicInteger stepNo= new AtomicInteger(1);
        for (SaveRuleConditionInfoRequest condition : this.ruleConditions) {
            Triple<String, String, String> expressionTriple = buildRuleConditionExpression(stepNo.getAndIncrement(), condition);
            expressions.add(expressionTriple.getLeft());
            if (!StringUtils.isEmpty(expressionTriple.getMiddle())) {
                defineList.add(expressionTriple.getMiddle());
                defineKeyList.add(expressionTriple.getRight());
            }
        }
        return Triple.of(
                RuleLogicalOperatorEnum.getByName(this.logicalOperator),
                Joiner.on(" " + Objects.requireNonNull(RuleLogicalOperatorEnum.getByName(this.logicalOperator)).getSymbol() + " ").join(expressions),
                Pair.of(!CollectionUtils.isEmpty(defineList) ? Joiner.on(" ").join(defineList) : null, defineKeyList)
        );
    }

    private static Triple<String, String, String> buildRuleConditionExpression(int stepNo, SaveRuleConditionInfoRequest condition) {
        String defKey = "list" + stepNo;
        StringBuilder defBuilder = new StringBuilder();
        StringBuilder expressionBuilder = new StringBuilder();
        expressionBuilder.append("( ");
        RuleRelationalOperatorEnum relationalOperatorEnum = Objects.requireNonNull(RuleRelationalOperatorEnum.getByName(condition.getRelationalOperator()));
        if (RuleRelationalOperatorEnum.isCollectionRelationalOperator(relationalOperatorEnum.name())) {
            defBuilder.append("def ").append(defKey).append(" = [];");
            switch (Objects.requireNonNull(FieldValueDataTypeEnum.getByName(condition.getFieldValueDataType()))) {
                case LIST_STRING:
                    List<String> stringList = JSON.parseArray(condition.getFieldValue(), String.class);
                    if (!CollectionUtils.isEmpty(stringList)) {
                        stringList.forEach(stringVal -> defBuilder.append(" ").append(defKey).append(".add('").append(stringVal).append("');"));
                    }
                    break;
                case LIST_INTEGER:
                    List<Integer> integerList = JSON.parseArray(condition.getFieldValue(), Integer.class);
                    if (!CollectionUtils.isEmpty(integerList)) {
                        integerList.forEach(integerVal -> defBuilder.append(" ").append(defKey).append(".add(").append(integerVal).append(");"));
                    }
                    break;
                case LIST_LONG:
                    List<Long> longList = JSON.parseArray(condition.getFieldValue(), Long.class);
                    if (!CollectionUtils.isEmpty(longList)) {
                        longList.forEach(longVal -> defBuilder.append(" ").append(defKey).append(".add(").append(longVal).append(");"));
                    }
                    break;
                case LIST_BIGDECIMAL:
                    List<BigDecimal> bigDecimalList = JSON.parseArray(condition.getFieldValue(), BigDecimal.class);
                    if (!CollectionUtils.isEmpty(bigDecimalList)) {
                        bigDecimalList.forEach(bigDecimalVal -> defBuilder.append(" ").append(defKey).append(".add(").append(bigDecimalVal).append(");"));
                    }
                    break;
            }
            expressionBuilder.append(relationalOperatorEnum.getFieldPrefix())
                    .append(condition.getFieldName())
                    .append(relationalOperatorEnum.getSymbolPrefix())
                    .append("@")
                    .append(relationalOperatorEnum.getSymbol())
                    .append("@")
                    .append(relationalOperatorEnum.getValuePrefix())
                    .append(defKey)
                    .append(relationalOperatorEnum.getValueSuffix());
        } else {
            switch (Objects.requireNonNull(FieldValueDataTypeEnum.getByName(condition.getFieldValueDataType()))) {
                case STRING:
                    expressionBuilder.append(relationalOperatorEnum.getFieldPrefix())
                            .append(condition.getFieldName())
                            .append(relationalOperatorEnum.getSymbolPrefix())
                            .append("@")
                            .append(relationalOperatorEnum.getSymbol())
                            .append("@")
                            .append(relationalOperatorEnum.getValuePrefix())
                            .append("'").append(condition.getFieldValue()).append("'")
                            .append(relationalOperatorEnum.getValueSuffix());
                    break;
                case INTEGER:
                case LONG:
                case BIGDECIMAL:
                case BOOLEAN:
                    expressionBuilder.append(relationalOperatorEnum.getFieldPrefix())
                            .append(condition.getFieldName())
                            .append(relationalOperatorEnum.getSymbolPrefix())
                            .append("@")
                            .append(relationalOperatorEnum.getSymbol())
                            .append("@")
                            .append(relationalOperatorEnum.getValuePrefix())
                            .append(condition.getFieldValue())
                            .append(relationalOperatorEnum.getValueSuffix());
                    break;
            }
        }
        expressionBuilder.append(" )");
        return Triple.of(expressionBuilder.toString(), defBuilder.toString(), defKey);
    }
}
