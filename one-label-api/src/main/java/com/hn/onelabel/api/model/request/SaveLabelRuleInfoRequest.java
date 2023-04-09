package com.hn.onelabel.api.model.request;

import com.hn.onelabel.api.enums.LabelDimensionKeyDefineTypeEnum;
import com.hn.onelabel.api.enums.RuleScriptTypeEnum;
import com.hn.onelabel.api.enums.RuleTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SaveLabelRuleInfoRequest {
    private Long ruleId;
    private Long ruleContextId;
    private Long ruleGroupId;
    /**
     * @see RuleTypeEnum
     */
    private String ruleType;
    private String ruleName;
    private String ruleDesc;
    private LocalDateTime ruleEffectiveStartTime;
    private LocalDateTime ruleEffectiveEndTime;
    /**
     * @see RuleScriptTypeEnum
     */
    private String ruleScriptType;
    private String ruleScriptContent;
    private String labelCode;
    private Long labelDimensionKeyId;
    /**
     * @see LabelDimensionKeyDefineTypeEnum
     */
    private String labelDimensionKeyDefineType;
    private String labelDimensionFixedKey;
    private String externalTags;
    private String creator;
    private String modifier;
}
