package com.hn.onelabel.api.model.request;

import com.hn.onelabel.api.enums.LabelOperationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UserLabelOperationRequest {
    private Long userId;
    private String labelCode;
    /**
     * @see LabelOperationTypeEnum
     */
    private String operationType;
}
