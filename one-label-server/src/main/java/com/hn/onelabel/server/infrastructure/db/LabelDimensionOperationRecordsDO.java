package com.hn.onelabel.server.infrastructure.db;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LabelDimensionOperationRecordsDO extends BaseDO {
    private String sequenceId;
    private Long userId;
    private String labelCode;
    /**
     * @see com.hn.onelabel.api.enums.LabelDimensionOperationTypeEnum
     */
    private String operationType;
    private String operationData;
    private String oldLabelDimension;
    private String newLabelDimension;
}
