package com.hn.onelabel.server.infrastructure.db.mapper;

import com.hn.onelabel.server.infrastructure.db.LabelGroupDO;
import com.hn.onelabel.server.infrastructure.db.query.LabelGroupQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LabelGroupMapper {
    void insertEntity(LabelGroupDO entity);
    void batchInsert(List<LabelGroupDO> list);
    LabelGroupDO selectByPrimaryKey(Long id);
    List<LabelGroupDO> selectByPrimaryKeyList(List<Long> list);
    void updateByPrimaryKey(LabelGroupDO entity);
    void logicalDeleteByPrimaryKey(Long id);
    void physicalDeleteByPrimaryKey(Long id);
    List<LabelGroupDO> selectByCondition(LabelGroupQuery query);
}
