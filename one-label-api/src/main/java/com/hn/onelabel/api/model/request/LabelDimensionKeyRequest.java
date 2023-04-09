package com.hn.onelabel.api.model.request;

import com.hn.onelabel.api.enums.LabelDimensionKeyDefineTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LabelDimensionKeyRequest {
    private Long dimensionKeyId;
    /**
     * @see LabelDimensionKeyDefineTypeEnum
     */
    private String dimensionKeyDefineType;
    private String dimensionFixedKey;
    private Map<String, String> dimensionDynamicKeyParams;
}
