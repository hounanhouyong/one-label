package com.hn.onelabel.server.application.rpc;

import com.hn.onelabel.api.common.Result;
import com.hn.onelabel.api.feign.UserSegmentAdminService;
import com.hn.onelabel.api.model.request.SaveUserSegmentInfoRequest;
import com.hn.onelabel.server.service.UserSegmentCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@Service
public class UserSegmentAdminServiceImpl implements UserSegmentAdminService {

    @Autowired
    private UserSegmentCommandService userSegmentCommandService;

    @Override
    public Result<Boolean> saveUserSegmentInfo(SaveUserSegmentInfoRequest request) {
        Result<Boolean> result = new Result<>();
        userSegmentCommandService.saveUserSegmentInfo(request);
        return result.success(true);
    }
}
