package com.hn.onelabel.adapter.api.model.request;

import com.hn.onelabel.adapter.api.enums.ExperimentTagEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SaveExperimentExtInfoRequest {
    /**
     * @see ExperimentTagEnum
     */
    private String experimentTag;
    private String experimentExtInfo;
}
