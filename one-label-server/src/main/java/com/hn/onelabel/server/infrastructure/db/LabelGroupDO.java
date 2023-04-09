package com.hn.onelabel.server.infrastructure.db;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LabelGroupDO extends BaseDO {
    private String labelGroupName;
    private String labelGroupCode;
    private String labelGroupDesc;
    private Long labelClassificationId;
    private String creator;
}
