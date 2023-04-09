package com.hn.onelabel.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FindUserLabelDimensionHotKeyRequest {
    private Long userId;
    private String labelCode;
    private Long labelDimensionKeyId;
    private String labelDimensionHotKey;
}
