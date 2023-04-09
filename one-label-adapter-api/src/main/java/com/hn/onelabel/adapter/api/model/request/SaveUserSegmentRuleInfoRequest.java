package com.hn.onelabel.adapter.api.model.request;

import com.hn.onelabel.adapter.api.enums.SegmentTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SaveUserSegmentRuleInfoRequest {
    /**
     * @see SegmentTypeEnum
     */
    private String segmentType;
    private String segmentName;
    private String ruleInfo;
    private String experimentGroupId;
    private List<SaveExperimentInfoRequest> saveExperimentInfoRequestList;
}
