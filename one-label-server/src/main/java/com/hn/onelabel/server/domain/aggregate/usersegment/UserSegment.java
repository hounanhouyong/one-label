package com.hn.onelabel.server.domain.aggregate.usersegment;

import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleScript;
import com.hn.onelabel.server.domain.aggregate.usersegment.valueobject.AbExperiment;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
public class UserSegment {
    private Long segmentId;
    /**
     * @see com.hn.onelabel.api.enums.UserSegmentTypeEnum
     */
    private String segmentType;
    private String segmentName;
    private RuleScript ruleScript;
    private String externalExperimentGroupId;
    private List<AbExperiment> abExperimentList;

    public UserSegment(String segmentType, String segmentName, RuleScript ruleScript, String externalExperimentGroupId) {
        this.segmentType = segmentType;
        this.segmentName = segmentName;
        this.ruleScript = ruleScript;
        this.externalExperimentGroupId = externalExperimentGroupId;
    }
}
