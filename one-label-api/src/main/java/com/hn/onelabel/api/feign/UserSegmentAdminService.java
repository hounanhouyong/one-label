package com.hn.onelabel.api.feign;

import com.hn.onelabel.api.common.Result;
import com.hn.onelabel.api.model.request.SaveUserSegmentInfoRequest;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(
        name = "one-label",
        url = "${xxx}",
        decode404 = true,
        contextId = "UserSegmentAdminService"
)
public interface UserSegmentAdminService {

    String PREFIX = "/user-segment/admin";

    @PostMapping(PREFIX + "/saveUserSegmentInfo")
    Result<Boolean> saveUserSegmentInfo(@RequestBody SaveUserSegmentInfoRequest request);

}
