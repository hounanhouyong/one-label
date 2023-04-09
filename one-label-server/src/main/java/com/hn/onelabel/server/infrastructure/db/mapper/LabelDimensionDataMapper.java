package com.hn.onelabel.server.infrastructure.db.mapper;

import com.hn.onelabel.server.infrastructure.db.LabelDimensionDataDO;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.util.List;

@Mapper
public interface LabelDimensionDataMapper {
    void insertEntityOnDuplicateKeyUpdate(LabelDimensionDataDO entity);
    LabelDimensionDataDO selectByUserIdAndId(@Param("userId") Long userId, @Param("id") Long id);
    LabelDimensionDataDO selectByUserIdAndDimensionKeyId(@Param("userId") Long userId, @Param("labelDimensionKeyId") Long labelDimensionKeyId);
    List<LabelDimensionDataDO> selectByUserIdAndDimensionKeyIdList(@Param("userId") Long userId, @Param("labelDimensionKeyIdList") List<Long> labelDimensionKeyIdList);
}
