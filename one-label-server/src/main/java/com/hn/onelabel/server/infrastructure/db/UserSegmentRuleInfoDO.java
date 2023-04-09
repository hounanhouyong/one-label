package com.hn.onelabel.server.infrastructure.db;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserSegmentRuleInfoDO extends BaseDO {
    /**
     * @see com.hn.onelabel.api.enums.UserSegmentTypeEnum
     */
    private String segmentType;
    private String segmentName;
    private String segmentDesc;
    /**
     * @see com.hn.onelabel.api.enums.RuleScriptTypeEnum
     */
    private String ruleScriptType;
    private String ruleScriptContent;
    /**
     * @see com.hn.onelabel.api.enums.RuleStatusEnum
     */
    private String status;
    private String externalExperimentGroupId;
    private String creator;
    private String modifier;
}
