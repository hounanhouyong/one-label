package com.hn.onelabel.adapter.api.model.request;

import com.hn.onelabel.adapter.api.enums.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.lang3.tuple.Triple;
import org.springframework.util.Assert;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SaveUserBehaviorRuleInfoRequest {
    private String behaviorId;
    /**
     * @see UserBehaviorTypeEnum
     */
    private String behaviorType;
    private List<String> behaviorTags;
    private String behaviorName;
    private String behaviorDesc;
    /**
     * @see RuleOperateTypeEnum
     */
    private String behaviorRuleOperateType;
    private LocalDateTime behaviorRuleEffectiveStartTime = LocalDateTime.now();
    private LocalDateTime behaviorRuleEffectiveEndTime = LocalDateTime.of(2099, 1, 1, 0, 0, 0);
    private Boolean onlyEffectiveForTheFirstTime = false;
    private SaveRuleInfoRequest behaviorRule;

    public void paramsIsValid() {
        Objects.requireNonNull(this.behaviorId, "Null behaviorId.");
        Objects.requireNonNull(UserBehaviorTypeEnum.getByName(this.behaviorType), "Error behaviorType.");
        Objects.requireNonNull(this.behaviorName, "Null behaviorName.");
        Objects.requireNonNull(RuleOperateTypeEnum.getByName(this.behaviorRuleOperateType), "Error behaviorRuleOperateType.");
        Assert.isTrue(!Objects.isNull(this.behaviorRuleEffectiveStartTime) && !Objects.isNull(this.behaviorRuleEffectiveEndTime) && this.behaviorRuleEffectiveStartTime.isBefore(this.behaviorRuleEffectiveEndTime), "Error behaviorRuleEffectiveTime.");
        Objects.requireNonNull(this.onlyEffectiveForTheFirstTime, "Null onlyEffectiveForTheFirstTime.");
        Assert.isTrue(this.behaviorRule.paramsIsValid(), "Error behaviorRule.");
    }

    public Triple<RuleLogicalOperatorEnum, String, Pair<String, List<String>>> buildRuleExpression() {
        if (this.onlyEffectiveForTheFirstTime) {
            this.behaviorRule.getRuleConditions().add(SaveRuleConditionInfoRequest.builder()
                    .relationalOperator(RuleRelationalOperatorEnum.EQUAL.name())
                    .fieldName(Objects.requireNonNull(UserBehaviorTypeEnum.getByName(this.behaviorType)).getLabelCode())
                    .fieldValue("false")
                    .fieldValueDataType(FieldValueDataTypeEnum.BOOLEAN.name())
                    .build());
        }
        this.paramsIsValid();
        return this.behaviorRule.buildRuleExpression();
    }
}
