package com.hn.onelabel.server.infrastructure.db;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class LabelClassificationDO extends BaseDO {
    private String classificationName;
    private String classificationCode;
    private Long classificationParentId;
    private Integer classificationLevel;
    private String classificationPath;
    private String creator;
}
