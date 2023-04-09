package com.hn.onelabel.server.infrastructure.db.query;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class SortModel {
    private String sortField;
    /**
     * @see SortTypeEnum
     */
    private String sortType;
}
