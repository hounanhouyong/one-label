package com.hn.onelabel.server.infrastructure.db.query;

import lombok.Data;

@Data
public class LabelHitRecordsQuery extends BaseQuery {
    private Long userId;
    private String labelCode;
}
