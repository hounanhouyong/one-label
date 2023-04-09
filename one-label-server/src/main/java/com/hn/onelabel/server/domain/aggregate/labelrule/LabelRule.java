package com.hn.onelabel.server.domain.aggregate.labelrule;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.hn.onelabel.api.enums.RuleScriptTypeEnum;
import com.hn.onelabel.server.common.utils.GroovyScriptUtil;
import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleDefineLabel;
import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleEffectiveTime;
import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleScript;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.Assert;

import java.util.Objects;

@Slf4j
@NoArgsConstructor
@Data
public class LabelRule {
    private Long ruleContextId;
    private Long ruleGroupId;
    private Long ruleId;
    private String ruleName;
    private String ruleDesc;
    /**
     * @see com.hn.onelabel.api.enums.RuleTypeEnum
     *
     */
    private String ruleType;
    private RuleScript ruleScript;
    private RuleEffectiveTime ruleEffectiveTime;
    private RuleDefineLabel ruleDefineLabel;
    private String externalTag;

    public LabelRule(Long ruleContextId, Long ruleGroupId, Long ruleId, String ruleName, String ruleDesc, String ruleType, RuleScript ruleScript, RuleEffectiveTime ruleEffectiveTime, RuleDefineLabel ruleDefineLabel, String externalTag) {
        this.ruleContextId = ruleContextId;
        this.ruleGroupId = ruleGroupId;
        this.ruleId = ruleId;
        this.ruleName = ruleName;
        this.ruleDesc = ruleDesc;
        this.ruleType = ruleType;
        this.ruleScript = ruleScript;
        this.ruleDefineLabel = ruleDefineLabel;
        this.ruleEffectiveTime = ruleEffectiveTime;
        this.externalTag = externalTag;
    }

    public boolean isHit(JSONObject ruleContext) {
        Assert.isTrue(RuleScriptTypeEnum.GROOVY.name().equals(this.ruleScript.getRuleScriptType()), "Only GROOVY is supported.");
        Object[] args = { ruleContext };
        try {
            return  (Boolean) Objects.requireNonNull(GroovyScriptUtil.invokeMethod(this.ruleScript.getRuleScriptContent(), "compute", args));
        } catch (Exception e) {
            log.error("[LabelRule.isHit] - exception, ruleScript: {}", JSON.toJSONString(this.ruleScript), e);
        }
        return false;
    }
}
