package com.hn.onelabel.server.infrastructure.db;

import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.Date;

@Data
@EqualsAndHashCode(callSuper = false)
public class LabelInfoDO extends BaseDO {
    /**
     * @see com.hn.onelabel.api.enums.LabelTypeEnum
     */
    private String labelType;
    private String labelName;
    private String labelCode;
    private String labelGroupCode;
    private String labelDesc;
    private Long labelRuleId;
    /**
     * @see com.hn.onelabel.api.enums.LabelStatusEnum
     */
    private String labelStatus;
    /**
     * @see com.hn.onelabel.api.enums.LabelRealtimeTypeEnum
     */
    private String labelRealtimeType;
    /**
     * @see com.hn.onelabel.api.enums.LabelRefreshTypeEnum
     */
    private String labelRefreshType;
    /**
     * @see com.hn.onelabel.api.enums.LabelInvalidTypeEnum
     */
    private String labelInvalidType;
    private Date labelInvalidTime;
    private String labelOwner;
    private String labelCreator;
}
