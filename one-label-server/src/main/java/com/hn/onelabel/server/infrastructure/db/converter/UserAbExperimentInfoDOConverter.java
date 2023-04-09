package com.hn.onelabel.server.infrastructure.db.converter;

import com.hn.onelabel.server.domain.aggregate.usersegment.valueobject.AbExperiment;
import com.hn.onelabel.server.infrastructure.db.UserAbExperimentInfoDO;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserAbExperimentInfoDOConverter {
    UserAbExperimentInfoDOConverter INSTANCE = Mappers.getMapper(UserAbExperimentInfoDOConverter.class);
    UserAbExperimentInfoDO from(Long segmentRuleId, AbExperiment record);
    AbExperiment to(UserAbExperimentInfoDO record);
    List<AbExperiment> to(List<UserAbExperimentInfoDO> list);
}
