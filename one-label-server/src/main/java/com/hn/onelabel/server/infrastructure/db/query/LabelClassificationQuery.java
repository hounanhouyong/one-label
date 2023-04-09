package com.hn.onelabel.server.infrastructure.db.query;

import lombok.Data;

@Data
public class LabelClassificationQuery extends BaseQuery {
    private String classificationNameKeywords;
    private String classificationCode;
    private Long classificationParentId;
    private String creatorKeyword;
}
