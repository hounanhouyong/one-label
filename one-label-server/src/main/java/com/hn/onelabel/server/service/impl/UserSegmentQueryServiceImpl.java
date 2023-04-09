package com.hn.onelabel.server.service.impl;

import com.hn.onelabel.api.enums.RuleStatusEnum;
import com.hn.onelabel.api.enums.UserSegmentTypeEnum;
import com.hn.onelabel.server.domain.aggregate.usersegment.UserSegment;
import com.hn.onelabel.server.domain.aggregate.usersegment.repository.UserSegmentRepository;
import com.hn.onelabel.server.service.UserSegmentQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;

@Slf4j
@Service
public class UserSegmentQueryServiceImpl implements UserSegmentQueryService {

    @Autowired
    private UserSegmentRepository userSegmentRepository;

    @Override
    public List<UserSegment> findUserSegments(UserSegmentTypeEnum segmentTypeEnum, RuleStatusEnum statusEnum, List<String> externalExperimentGroupIdList) {
        Objects.requireNonNull(segmentTypeEnum, "Null segmentTypeEnum.");
        Objects.requireNonNull(statusEnum, "Null statusEnum.");
        Objects.requireNonNull(externalExperimentGroupIdList, "Null externalExperimentGroupIdList.");
        return userSegmentRepository.findUserSegments(segmentTypeEnum, statusEnum, externalExperimentGroupIdList);
    }

    @Override
    public UserSegment findUserSegment(UserSegmentTypeEnum segmentTypeEnum, RuleStatusEnum statusEnum, String externalExperimentGroupId) {
        Objects.requireNonNull(segmentTypeEnum, "Null segmentTypeEnum.");
        Objects.requireNonNull(statusEnum, "Null statusEnum.");
        Objects.requireNonNull(externalExperimentGroupId, "Null externalExperimentGroupId");
        return userSegmentRepository.findUserSegment(segmentTypeEnum, statusEnum, externalExperimentGroupId);
    }
}
