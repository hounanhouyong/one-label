package com.hn.onelabel.server.infrastructure.db;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LabelOperationRecordsDO extends BaseDO {
    private String sequenceId;
    private Long userId;
    private String labelCode;
    /**
     * @see com.hn.onelabel.api.enums.LabelOperationTypeEnum
     */
    private String operationType;
    private String operationData;
    private String oldLabel;
    private String newLabel;
}
