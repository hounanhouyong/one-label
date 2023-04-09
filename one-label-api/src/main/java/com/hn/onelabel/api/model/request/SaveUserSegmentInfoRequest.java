package com.hn.onelabel.api.model.request;

import com.hn.onelabel.api.enums.RuleScriptTypeEnum;
import com.hn.onelabel.api.enums.UserSegmentTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SaveUserSegmentInfoRequest {
    /**
     * @see UserSegmentTypeEnum
     */
    private String segmentType;
    private String segmentName;
    private String segmentDesc;
    /**
     * @see RuleScriptTypeEnum
     */
    private String ruleScriptType;
    private String ruleScriptContent;
    private String externalExperimentGroupId;
    private String creator;

    private List<SaveUserAbExperimentInfoRequest> saveUserAbExperimentInfoRequestList;
}
