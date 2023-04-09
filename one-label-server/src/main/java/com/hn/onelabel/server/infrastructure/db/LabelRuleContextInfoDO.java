package com.hn.onelabel.server.infrastructure.db;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LabelRuleContextInfoDO extends BaseDO {
    private String ruleContextName;
    private String ruleContextDesc;
    private String creator;
}
