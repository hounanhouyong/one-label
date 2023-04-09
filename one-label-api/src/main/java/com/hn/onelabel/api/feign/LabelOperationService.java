package com.hn.onelabel.api.feign;

import com.hn.onelabel.api.common.Result;
import com.hn.onelabel.api.model.request.*;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

@FeignClient(
        name = "one-label",
        url = "${xxx}",
        decode404 = true,
        contextId = "LabelOperationService"
)
public interface LabelOperationService {

    String PREFIX = "/userlabel";

    @PostMapping(PREFIX + "/userHasLabel")
    Result<Boolean> userHasLabel(@RequestBody FindUserHasLabelRequest request);

    @PostMapping(PREFIX + "/userHasLabels")
    Result<Boolean> userHasLabels(@RequestBody FindUserHasLabelsRequest request);

    @PostMapping(PREFIX + "/findUserLabels")
    Result<List<String>> findUserLabels(@RequestBody FindUserHasLabelsRequest request);

    @PostMapping(PREFIX + "/labelOperation")
    Result<Boolean> labelOperation(@RequestBody LabelOperationRequest request);

    @PostMapping(PREFIX + "/userLabelOperation")
    Result<Boolean> userLabelOperation(@RequestBody UserLabelOperationRequest request);

    @PostMapping(PREFIX + "/hitLabelRulesOperation")
    Result<Boolean> hitLabelRulesOperation(@RequestBody HitLabelRulesOperationRequest request);
}
