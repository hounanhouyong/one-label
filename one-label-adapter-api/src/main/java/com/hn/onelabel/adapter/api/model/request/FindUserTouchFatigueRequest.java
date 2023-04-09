package com.hn.onelabel.adapter.api.model.request;

import com.hn.onelabel.adapter.api.enums.TouchFatigueTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FindUserTouchFatigueRequest {
    private Long userId;
    /**
     * @see TouchFatigueTypeEnum
     */
    private String touchFatigueType;
    private String resourcesKeyword;
    private Integer increaseVal = 1;
}
