package com.hn.onelabel.server.infrastructure.db;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class LabelRuleInfoDO extends BaseDO {
    private Long ruleContextId;
    private Long ruleGroupId;
    /**
     * @see com.hn.onelabel.api.enums.RuleTypeEnum
     */
    private String ruleType;
    private String ruleName;
    private String ruleDesc;
    /**
     * @see com.hn.onelabel.api.enums.RuleStatusEnum
     */
    private String ruleStatus;
    private Date ruleEffectiveStartTime;
    private Date ruleEffectiveEndTime;
    /**
     * @see com.hn.onelabel.api.enums.RuleScriptTypeEnum
     */
    private String ruleScriptType;
    private String ruleScriptContent;
    private String labelCode;
    private Long labelDimensionKeyId;
    /**
     * @see com.hn.onelabel.api.enums.LabelDimensionKeyDefineTypeEnum
     */
    private String labelDimensionKeyDefineType;
    private String labelDimensionFixedKey;
    private String externalTag;
    private String creator;
    private String modifier;
}
