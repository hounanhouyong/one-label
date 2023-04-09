package com.hn.onelabel.server.infrastructure.db;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LabelRuleGroupDO extends BaseDO {
    private String ruleGroupName;
    private String ruleGroupDesc;
    private Long ruleContextId;
}
