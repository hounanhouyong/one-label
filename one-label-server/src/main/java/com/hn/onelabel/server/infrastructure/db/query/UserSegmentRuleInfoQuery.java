package com.hn.onelabel.server.infrastructure.db.query;

import lombok.Data;

import java.util.List;

@Data
public class UserSegmentRuleInfoQuery extends BaseQuery {
    /**
     * @see com.hn.onelabel.api.enums.UserSegmentTypeEnum
     */
    private String segmentType;
    /**
     * @see com.hn.onelabel.api.enums.RuleStatusEnum
     */
    private String status;
    private String externalExperimentGroupId;
    private List<String> externalExperimentGroupIdList;
}
