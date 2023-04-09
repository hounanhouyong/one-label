package com.hn.onelabel.server.infrastructure.db.mapper;

import com.hn.onelabel.server.infrastructure.db.LabelHitRecordsDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface LabelHitRecordsMapper {
    void insertEntity(@Param("ds") Integer ds, @Param("item") LabelHitRecordsDO entity);
}
