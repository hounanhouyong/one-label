package com.hn.onelabel.server.infrastructure.db.mapper;

import com.hn.onelabel.server.infrastructure.db.UserAbExperimentInfoDO;
import com.hn.onelabel.server.infrastructure.db.query.UserAbExperimentInfoQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserAbExperimentInfoMapper {
    void insertEntity(UserAbExperimentInfoDO entity);
    void batchInsert(List<UserAbExperimentInfoDO> list);
    UserAbExperimentInfoDO selectByPrimaryKey(Long id);
    List<UserAbExperimentInfoDO> selectByPrimaryKeyList(List<Long> list);
    void updateByPrimaryKey(UserAbExperimentInfoDO entity);
    void logicalDeleteByPrimaryKey(Long id);
    void physicalDeleteByPrimaryKey(Long id);
    List<UserAbExperimentInfoDO> selectByCondition(UserAbExperimentInfoQuery query);
}
