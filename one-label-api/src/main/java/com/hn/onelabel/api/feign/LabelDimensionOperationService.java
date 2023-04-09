package com.hn.onelabel.api.feign;

import com.hn.onelabel.api.common.Result;
import com.hn.onelabel.api.model.request.FindUserLabelDimensionsRequest;
import com.hn.onelabel.api.model.request.LabelDimensionIncrRequest;
import com.hn.onelabel.api.model.request.LabelDimensionOperationRequest;
import com.hn.onelabel.api.model.response.LabelDimensionResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "one-label",
        url = "${xxx}",
        decode404 = true,
        contextId = "LabelDimensionOperationService"
)
public interface LabelDimensionOperationService {

    String PREFIX = "/label-dimension";

    @PostMapping(PREFIX + "/labelDimensionOperation")
    Result<Boolean> labelDimensionOperation(@RequestBody LabelDimensionOperationRequest request);

    @PostMapping(PREFIX + "/findLabelDimensions")
    Result<List<LabelDimensionResponse>> findLabelDimensions(@RequestBody FindUserLabelDimensionsRequest request);

    @PostMapping(PREFIX + "/labelDimensionIncr")
    Result<Boolean> labelDimensionIncr(@RequestBody LabelDimensionIncrRequest request);
}
