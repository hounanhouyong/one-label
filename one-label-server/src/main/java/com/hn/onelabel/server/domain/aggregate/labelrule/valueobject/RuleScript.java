package com.hn.onelabel.server.domain.aggregate.labelrule.valueobject;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class RuleScript {
    /**
     * @see com.hn.onelabel.api.enums.RuleScriptTypeEnum
     */
    private String ruleScriptType;
    private String ruleScriptContent;

    public RuleScript(String ruleScriptType, String ruleScriptContent) {
        this.ruleScriptType = ruleScriptType;
        this.ruleScriptContent = ruleScriptContent;
    }
}
