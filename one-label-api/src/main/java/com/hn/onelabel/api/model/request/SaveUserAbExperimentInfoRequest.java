package com.hn.onelabel.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SaveUserAbExperimentInfoRequest {
    private String externalExperimentGroupId;
    private String externalExperimentCode;
    private Integer weight;
    private String externalExperimentExtInfo;
    private String externalExperimentTag;
}
