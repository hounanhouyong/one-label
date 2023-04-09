package com.hn.onelabel.server.infrastructure.db.mapper;

import com.hn.onelabel.server.infrastructure.db.LabelRuleInfoDO;
import com.hn.onelabel.server.infrastructure.db.query.LabelRuleInfoQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LabelRuleInfoMapper {
    void insertEntity(LabelRuleInfoDO entity);
    void batchInsert(List<LabelRuleInfoDO> list);
    LabelRuleInfoDO selectByPrimaryKey(Long id);
    List<LabelRuleInfoDO> selectByPrimaryKeyList(List<Long> list);
    void updateByPrimaryKey(LabelRuleInfoDO entity);
    void logicalDeleteByPrimaryKey(Long id);
    void physicalDeleteByPrimaryKey(Long id);
    List<LabelRuleInfoDO> selectByCondition(LabelRuleInfoQuery query);
}
