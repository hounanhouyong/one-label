package com.hn.onelabel.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class DeleteLabelDimensionOperationRequest {
    private Long userId;
    private String labelCode;
    private List<String> labelDimensionKeyList;
}