package com.hn.onelabel.server.infrastructure.db.mapper;

import com.hn.onelabel.server.infrastructure.db.LabelOperationRecordsDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LabelOperationRecordsMapper {
    void insertEntity(@Param("ds") Integer ds, @Param("item") LabelOperationRecordsDO entity);
}
