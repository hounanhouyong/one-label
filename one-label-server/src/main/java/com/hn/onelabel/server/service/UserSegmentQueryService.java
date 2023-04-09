package com.hn.onelabel.server.service;

import com.hn.onelabel.api.enums.RuleStatusEnum;
import com.hn.onelabel.api.enums.UserSegmentTypeEnum;
import com.hn.onelabel.server.domain.aggregate.usersegment.UserSegment;

import java.util.List;

public interface UserSegmentQueryService {

    List<UserSegment> findUserSegments(UserSegmentTypeEnum segmentTypeEnum, RuleStatusEnum statusEnum, List<String> externalExperimentGroupIdList);

    UserSegment findUserSegment(UserSegmentTypeEnum segmentTypeEnum, RuleStatusEnum statusEnum, String externalExperimentGroupId);
}
