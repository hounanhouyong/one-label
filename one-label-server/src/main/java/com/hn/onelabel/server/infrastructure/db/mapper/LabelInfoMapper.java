package com.hn.onelabel.server.infrastructure.db.mapper;

import com.hn.onelabel.server.infrastructure.db.LabelInfoDO;
import com.hn.onelabel.server.infrastructure.db.query.LabelInfoQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LabelInfoMapper {
    void insertEntity(LabelInfoDO entity);
    void batchInsert(List<LabelInfoDO> list);
    LabelInfoDO selectByPrimaryKey(Long id);
    List<LabelInfoDO> selectByPrimaryKeyList(List<Long> list);
    void updateByPrimaryKey(LabelInfoDO entity);
    void logicalDeleteByPrimaryKey(Long id);
    void physicalDeleteByPrimaryKey(Long id);
    List<LabelInfoDO> selectByCondition(LabelInfoQuery query);
}
