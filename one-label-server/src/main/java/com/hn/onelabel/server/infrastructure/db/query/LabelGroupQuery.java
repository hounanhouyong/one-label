package com.hn.onelabel.server.infrastructure.db.query;

import lombok.Data;

@Data
public class LabelGroupQuery extends BaseQuery {
    private String labelGroupNameKeywords;
    private String labelGroupCode;
    private String labelClassificationId;
    private String creatorKeyword;
}
