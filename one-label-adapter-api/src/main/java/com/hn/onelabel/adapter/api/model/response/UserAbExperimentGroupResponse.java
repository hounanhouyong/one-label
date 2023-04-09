package com.hn.onelabel.adapter.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserAbExperimentGroupResponse {
    private String experimentGroupId;
    private String experimentId;
    private String experimentCode;
    private String experimentExtInfo;
    private List<Long> userIdList;
}
