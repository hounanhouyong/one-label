package com.hn.onelabel.adapter.api.model.request;

import com.hn.onelabel.adapter.api.enums.ExperimentTagEnum;
import com.hn.onelabel.adapter.api.enums.SegmentSceneEnum;
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
public class FindUserSegmentInfoRequest {
    private Long userId;
    /**
     * @see SegmentTypeEnum
     */
    private String segmentType;
    /**
     * @see SegmentSceneEnum
     */
    private String segmentScene;
    /**
     * @see ExperimentTagEnum
     */
    private List<String> experimentTags;
    private List<String> experimentGroupIdList;
}
