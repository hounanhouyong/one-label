package com.hn.onelabel.server.infrastructure.db.mapper;

import com.hn.onelabel.server.infrastructure.db.LabelClassificationDO;
import com.hn.onelabel.server.infrastructure.db.query.LabelClassificationQuery;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface LabelClassificationMapper {
    void insertEntity(LabelClassificationDO entity);
    void batchInsert(List<LabelClassificationDO> list);
    LabelClassificationDO selectByPrimaryKey(Long id);
    List<LabelClassificationDO> selectByPrimaryKeyList(List<Long> list);
    void updateByPrimaryKey(LabelClassificationDO entity);
    void logicalDeleteByPrimaryKey(Long id);
    void physicalDeleteByPrimaryKey(Long id);
    List<LabelClassificationDO> selectByCondition(LabelClassificationQuery query);
}
