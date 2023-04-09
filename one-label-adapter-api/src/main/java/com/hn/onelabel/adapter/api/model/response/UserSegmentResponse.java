package com.hn.onelabel.adapter.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserSegmentResponse {
    private String experimentGroupId;
    private String experimentId;
    private String experimentCode;
    private String experimentExtInfo;
}
