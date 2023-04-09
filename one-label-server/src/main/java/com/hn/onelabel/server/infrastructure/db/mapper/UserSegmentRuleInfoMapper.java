package com.hn.onelabel.server.infrastructure.db.mapper;

import com.hn.onelabel.server.infrastructure.db.UserSegmentRuleInfoDO;
import com.hn.onelabel.server.infrastructure.db.query.UserSegmentRuleInfoQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface UserSegmentRuleInfoMapper {
    void insertEntity(UserSegmentRuleInfoDO entity);
    void batchInsert(List<UserSegmentRuleInfoDO> list);
    UserSegmentRuleInfoDO selectByPrimaryKey(Long id);
    List<UserSegmentRuleInfoDO> selectByPrimaryKeyList(List<Long> list);
    void updateByPrimaryKey(UserSegmentRuleInfoDO entity);
    void logicalDeleteByPrimaryKey(Long id);
    void physicalDeleteByPrimaryKey(Long id);
    List<UserSegmentRuleInfoDO> selectByCondition(UserSegmentRuleInfoQuery query);
}
