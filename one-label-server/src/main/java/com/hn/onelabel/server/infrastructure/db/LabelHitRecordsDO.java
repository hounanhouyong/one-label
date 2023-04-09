package com.hn.onelabel.server.infrastructure.db;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LabelHitRecordsDO extends BaseDO {
    private String sequenceId;
    private Long userId;
    private String labelCode;
    /**
     * @see com.hn.onelabel.api.enums.RuleTypeEnum
     */
    private String labelRuleType;
    private Long labelRuleId;
    private Long labelRuleContextId;
    private String labelRuleContext;
}
