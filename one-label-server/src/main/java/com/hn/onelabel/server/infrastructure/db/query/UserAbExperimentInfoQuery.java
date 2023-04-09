package com.hn.onelabel.server.infrastructure.db.query;

import lombok.Data;

import java.util.List;

@Data
public class UserAbExperimentInfoQuery extends BaseQuery {
    private Long segmentRuleId;
    private String experimentId;
    private List<String> experimentIdList;
}
