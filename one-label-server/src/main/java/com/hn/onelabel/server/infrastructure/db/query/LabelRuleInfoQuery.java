package com.hn.onelabel.server.infrastructure.db.query;

import lombok.Data;

import java.util.List;

@Data
public class LabelRuleInfoQuery extends BaseQuery {
    private Long ruleContextId;
    private Long ruleGroupId;
    /**
     * @see com.hn.onelabel.api.enums.RuleStatusEnum
     */
    private String ruleStatus;
    private String labelCode;
    private String labelDimensionFixedKey;
    private List<String> labelDimensionFixedKeyList;
    private boolean isValid = true;
}
