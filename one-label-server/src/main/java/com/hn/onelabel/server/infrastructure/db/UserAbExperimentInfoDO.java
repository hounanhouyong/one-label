package com.hn.onelabel.server.infrastructure.db;

import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class UserAbExperimentInfoDO extends BaseDO {
    private Long segmentRuleId;
    private String experimentId;
    private Integer weight;
    private String externalExperimentGroupId;
    private String externalExperimentCode;
    private String externalExperimentExtInfo;
    private String externalExperimentTag;
    private String creator;
    private String modifier;
}
