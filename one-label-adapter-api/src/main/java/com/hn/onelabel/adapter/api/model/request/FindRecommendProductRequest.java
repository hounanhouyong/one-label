package com.hn.onelabel.adapter.api.model.request;

import com.hn.onelabel.adapter.api.enums.RecommendTypeEnum;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class FindRecommendProductRequest {
    private Long userId;
    /**
     * @see RecommendTypeEnum
     */
    private String recommendType;
}
