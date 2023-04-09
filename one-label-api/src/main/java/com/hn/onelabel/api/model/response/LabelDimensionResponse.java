package com.hn.onelabel.api.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LabelDimensionResponse {
    private Long dimensionKeyId;
    private String dimensionKey;
    private String dimensionVal;
}
