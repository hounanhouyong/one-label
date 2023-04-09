package com.hn.onelabel.server.service.impl;

import com.hn.onelabel.api.enums.UserSegmentTypeEnum;
import com.hn.onelabel.api.model.request.SaveUserSegmentInfoRequest;
import com.hn.onelabel.server.common.utils.SequenceIdUtils;
import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleScript;
import com.hn.onelabel.server.domain.aggregate.usersegment.UserSegment;
import com.hn.onelabel.server.domain.aggregate.usersegment.repository.UserSegmentRepository;
import com.hn.onelabel.server.domain.aggregate.usersegment.valueobject.AbExperiment;
import com.hn.onelabel.server.service.UserSegmentCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserSegmentCommandServiceImpl implements UserSegmentCommandService {

    @Autowired
    private UserSegmentRepository userSegmentRepository;

    @Override
    public void saveUserSegmentInfo(SaveUserSegmentInfoRequest request) {
        Objects.requireNonNull(UserSegmentTypeEnum.getByName(request.getSegmentType()), "Error segmentType.");
        Objects.requireNonNull(request.getRuleScriptType(), "Null ruleScriptType.");
        Objects.requireNonNull(request.getRuleScriptContent(), "Null ruleScriptContent.");
        Objects.requireNonNull(request.getSaveUserAbExperimentInfoRequestList(), "Null abExperimentInfo.");
        UserSegment userSegment = new UserSegment(request.getSegmentType(), request.getSegmentName(), new RuleScript(request.getRuleScriptType(), request.getRuleScriptContent()), request.getExternalExperimentGroupId());
        userSegment.setAbExperimentList(request.getSaveUserAbExperimentInfoRequestList().stream().map(abRequest -> new AbExperiment(SequenceIdUtils.generateSequenceId(), abRequest.getWeight(), request.getExternalExperimentGroupId(), abRequest.getExternalExperimentCode(), abRequest.getExternalExperimentExtInfo(), abRequest.getExternalExperimentTag())).collect(Collectors.toList()));
        userSegmentRepository.saveUserSegment(userSegment, request.getCreator());
    }

    @Override
    public void disableUserSegment(List<Long> segmentIds) {
        Objects.requireNonNull(segmentIds, "Null segmentIds.");
        userSegmentRepository.disableUserSegment(segmentIds);
    }
}
