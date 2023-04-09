package com.hn.onelabel.server.infrastructure.db;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LabelDimensionDataDO extends BaseDO {
    private Long userId;
    private Long labelDimensionKeyId;
    private String labelDimension;
}
