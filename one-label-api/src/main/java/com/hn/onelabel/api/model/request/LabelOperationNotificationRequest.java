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
public class LabelOperationNotificationRequest {
    /**
     * @see LabelOperationTypeEnum
     */
    private String labelOperationType;
    private String labelGroupCode;
    private String labelCode;
    private Long userId;
    private String extInfo;
}
