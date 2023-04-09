package com.hn.onelabel.adapter.api.model.request;

import com.hn.onelabel.adapter.api.enums.SegmentSceneEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SaveUserSegmentRequest {
    /**
     * @see SegmentSceneEnum
     */
    private String segmentScene;
    private String experimentGroupId;
    private List<SaveExperimentInfoRequest> saveExperimentInfoRequestList;
    private List<String> userIdList;
}
