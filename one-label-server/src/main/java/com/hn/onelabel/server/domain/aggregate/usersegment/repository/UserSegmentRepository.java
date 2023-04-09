package com.hn.onelabel.server.domain.aggregate.usersegment.repository;

import com.hn.onelabel.api.enums.RuleStatusEnum;
import com.hn.onelabel.api.enums.UserSegmentTypeEnum;
import com.hn.onelabel.server.domain.aggregate.usersegment.UserSegment;

import java.util.List;

public interface UserSegmentRepository {

    void saveUserSegment(UserSegment userSegment, String creator);

    void disableUserSegment(List<Long> segmentIds);

    List<UserSegment> findUserSegments(UserSegmentTypeEnum segmentTypeEnum, RuleStatusEnum statusEnum, List<String> externalExperimentGroupIdList);

    UserSegment findUserSegment(UserSegmentTypeEnum segmentTypeEnum, RuleStatusEnum statusEnum, String externalExperimentGroupId);
}
