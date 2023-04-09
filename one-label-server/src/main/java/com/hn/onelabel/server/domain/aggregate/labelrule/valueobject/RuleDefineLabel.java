package com.hn.onelabel.server.domain.aggregate.labelrule.valueobject;

import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Data
public class RuleDefineLabel {
    private String labelCode;
    private Long labelDimensionKeyId;
    /**
     * @see com.hn.onelabel.api.enums.LabelDimensionKeyDefineTypeEnum
     */
    private String labelDimensionKeyDefineType;
    private String labelDimensionFixedKey;

    public RuleDefineLabel(String labelCode, Long labelDimensionKeyId, String labelDimensionKeyDefineType, String labelDimensionFixedKey) {
        this.labelCode = labelCode;
        this.labelDimensionKeyId = labelDimensionKeyId;
        this.labelDimensionKeyDefineType = labelDimensionKeyDefineType;
        this.labelDimensionFixedKey = labelDimensionFixedKey;
    }
}
