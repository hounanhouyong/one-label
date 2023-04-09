package com.hn.onelabel.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Data
public class LabelDimensionIncrRequest {
    private Long userId;
    private String labelCode;
    private LabelDimensionKeyRequest labelDimensionKeyRequest;
    private Integer increaseVal = 1;
}
