package com.hn.onelabel.api.feign;

import com.hn.onelabel.api.common.Result;
import com.hn.onelabel.api.model.request.DeleteLabelRuleInfoRequest;
import com.hn.onelabel.api.model.request.FindUserLabelsHashFieldStatisticsRequest;
import com.hn.onelabel.api.model.request.SaveLabelRuleInfoRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "one-label",
        url = "${xxx}",
        decode404 = true,
        contextId = "LabelAdminService"
)
public interface LabelAdminService {

    String PREFIX = "/label/admin";

    @PostMapping(PREFIX + "/findUserLabelsHashFieldStatistics")
    Result<Integer> findUserLabelsHashFieldStatistics(@RequestBody FindUserLabelsHashFieldStatisticsRequest request);

    @PostMapping(PREFIX + "/saveLabelRuleInfo")
    Result<Boolean> saveLabelRuleInfo(@RequestBody SaveLabelRuleInfoRequest request);

    @PostMapping(PREFIX + "/deleteLabelRuleInfo")
    Result<Boolean> deleteLabelRuleInfo(@RequestBody DeleteLabelRuleInfoRequest request);
}
