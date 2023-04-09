package com.hn.onelabel.api.model.request;

import com.alibaba.fastjson.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class LabelOperationRequest {
    private JSONObject context;
    private Long ruleContextId;
}
