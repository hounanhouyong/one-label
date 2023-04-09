package com.hn.onelabel.server.infrastructure.db;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LabelRuleContextAttributesDO extends BaseDO {
    private Long ruleContextId;
    /**
     * @see com.hn.onelabel.api.enums.RuleContextAttributeLoadTypeEnum
     */
    private String attributeLoadType;
    /**
     * @see com.hn.onelabel.api.enums.RuleContextAttributeLoadFromEnum
     */
    private String attributeLoadFrom;
    private String attributeName;
    private String attributeCode;
    private String attributeDesc;
    /**
     * @see com.hn.onelabel.api.enums.RuleContextAttributeTypeEnum
     */
    private String attributeType;
    private Long attributeParentId;
    private Integer attributeLevel;
    private String attributePath;
}
