package com.hn.onelabel.server.domain.aggregate.usersegment.valueobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class AbExperiment {
    private String experimentId;
    private Integer weight;
    private String externalExperimentGroupId;
    private String externalExperimentCode;
    private String externalExperimentExtInfo;
    private String externalExperimentTag;
}
