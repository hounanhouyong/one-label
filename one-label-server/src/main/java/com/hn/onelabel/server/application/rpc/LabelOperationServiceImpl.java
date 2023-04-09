package com.hn.onelabel.server.application.rpc;

import com.hn.onelabel.api.common.Result;
import com.hn.onelabel.api.enums.LabelOperationTypeEnum;
import com.hn.onelabel.api.feign.LabelOperationService;
import com.hn.onelabel.api.model.request.*;
import com.hn.onelabel.server.common.utils.SequenceIdUtils;
import com.hn.onelabel.server.service.UserLabelCommandService;
import com.hn.onelabel.server.service.UserLabelQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@Service
public class LabelOperationServiceImpl implements LabelOperationService {

    @Autowired
    private UserLabelQueryService userLabelQueryService;
    @Autowired
    private UserLabelCommandService userLabelCommandService;

    @Override
    public Result<Boolean> userHasLabel(@RequestBody FindUserHasLabelRequest request) {
        Result<Boolean> result = new Result<>();
        return result.success(userLabelQueryService.userHasLabel(request));
    }

    @Override
    public Result<Boolean> userHasLabels(@RequestBody FindUserHasLabelsRequest request) {
        Result<Boolean> result = new Result<>();
        return result.success(userLabelQueryService.userHasLabels(request));
    }

    @Override
    public Result<List<String>> findUserLabels(FindUserHasLabelsRequest request) {
        Result<List<String>> result = new Result<>();
        return result.success(userLabelQueryService.findUserLabels(request));
    }

    @Override
    public Result<Boolean> labelOperation(LabelOperationRequest request) {
        Result<Boolean> result = new Result<>();
        return result.success(userLabelCommandService.labelOperation(SequenceIdUtils.generateSequenceId(), request.getContext(), request.getRuleContextId()));
    }

    @Override
    public Result<Boolean> userLabelOperation(UserLabelOperationRequest request) {
        Result<Boolean> result = new Result<>();
        return result.success(userLabelCommandService.labelOperation(SequenceIdUtils.generateSequenceId(), request.getUserId(), Objects.requireNonNull(LabelOperationTypeEnum.getByName(request.getOperationType())), request.getLabelCode(), true));
    }

    @Override
    public Result<Boolean> hitLabelRulesOperation(HitLabelRulesOperationRequest request) {
        Result<Boolean> result = new Result<>();
        return result.success(userLabelCommandService.hitLabelRulesOperation(SequenceIdUtils.generateSequenceId(), request));
    }
}
