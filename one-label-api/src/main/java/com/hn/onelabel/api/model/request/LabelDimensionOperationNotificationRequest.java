package com.hn.onelabel.api.model.request;

import com.hn.onelabel.api.enums.LabelDimensionOperationTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LabelDimensionOperationNotificationRequest {
    /**
     * @see LabelDimensionOperationTypeEnum
     */
    private String labelDimensionOperationType;
    private String labelGroupCode;
    private String labelCode;
    private Long labelDimensionKeyId;
    private String labelDimensionKey;
    private List<String> tags;
    private Long userId;
    private String extInfo;
}
