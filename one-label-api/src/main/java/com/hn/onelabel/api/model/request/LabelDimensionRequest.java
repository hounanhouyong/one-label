package com.hn.onelabel.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LabelDimensionRequest {
    private LabelDimensionKeyRequest labelDimensionKeyRequest;
    private String dimensionVal;
}
