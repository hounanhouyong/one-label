package com.hn.onelabel.server.infrastructure.db.mapper;

import com.hn.onelabel.server.infrastructure.db.LabelDimensionOperationRecordsDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LabelDimensionOperationRecordsMapper {
    void insertEntity(@Param("ds") Integer ds, @Param("item") LabelDimensionOperationRecordsDO entity);
}
