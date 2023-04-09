package com.hn.onelabel.server.infrastructure.db.query;

import lombok.Data;

@Data
public class LabelInfoQuery extends BaseQuery {
    /**
     * @see com.hn.onelabel.api.enums.LabelTypeEnum
     */
    private String labelType;
    private String labelNameKeywords;
    private String labelCode;
    private String labelGroupCode;
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
    private String labelCreatorKeyword;
}
