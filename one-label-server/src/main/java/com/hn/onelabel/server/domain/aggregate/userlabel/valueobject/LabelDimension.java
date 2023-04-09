package com.hn.onelabel.server.domain.aggregate.userlabel.valueobject;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
public class LabelDimension {
    private String labelCode;
    private Long dimensionKeyId;
    private String dimensionKey;
    private String dimensionVal;
}
