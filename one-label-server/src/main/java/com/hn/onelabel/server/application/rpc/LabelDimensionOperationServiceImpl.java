package com.hn.onelabel.server.application.rpc;

import com.hn.onelabel.api.common.Result;
import com.hn.onelabel.api.feign.LabelDimensionOperationService;
import com.hn.onelabel.api.model.request.FindUserLabelDimensionsRequest;
import com.hn.onelabel.api.model.request.LabelDimensionIncrRequest;
import com.hn.onelabel.api.model.request.LabelDimensionOperationRequest;
import com.hn.onelabel.api.model.response.LabelDimensionResponse;
import com.hn.onelabel.server.common.utils.SequenceIdUtils;
import com.hn.onelabel.server.service.LabelDimensionCommandService;
import com.hn.onelabel.server.service.LabelDimensionQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@Service
public class LabelDimensionOperationServiceImpl implements LabelDimensionOperationService {

    @Autowired
    private LabelDimensionQueryService labelDimensionQueryService;
    @Autowired
    private LabelDimensionCommandService labelDimensionCommandService;

    @Override
    public Result<Boolean> labelDimensionOperation(@RequestBody LabelDimensionOperationRequest request) {
        Result<Boolean> result = new Result<>();
        labelDimensionCommandService.labelDimensionOperation(SequenceIdUtils.generateSequenceId(), request.getUserId(), request.getLabelCode(), request.getLabelDimensionRequestList());
        return result.success(true);
    }

    @Override
    public Result<List<LabelDimensionResponse>> findLabelDimensions(FindUserLabelDimensionsRequest request) {
        Result<List<LabelDimensionResponse>> result = new Result<>();
        return result.success(labelDimensionQueryService.findLabelDimensions(request, true, true));
    }

    @Override
    public Result<Boolean> labelDimensionIncr(LabelDimensionIncrRequest request) {
        Result<Boolean> result = new Result<>();
        labelDimensionCommandService.labelDimensionIncr(SequenceIdUtils.generateSequenceId(), request.getUserId(), request.getLabelCode(), request.getLabelDimensionKeyRequest(), request.getIncreaseVal());
        return result.success(true);
    }

}
