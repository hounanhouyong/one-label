package com.hn.onelabel.server.infrastructure.db.repository.impl;

import com.hn.onelabel.server.domain.aggregate.userlabel.repository.LabelRepository;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.Label;
import com.hn.onelabel.server.infrastructure.cache.LocalCacheService;
import com.hn.onelabel.server.infrastructure.db.LabelInfoDO;
import com.hn.onelabel.server.infrastructure.db.mapper.LabelInfoMapper;
import com.hn.onelabel.server.infrastructure.db.query.LabelInfoQuery;
import com.hn.onelabel.server.infrastructure.db.query.SortModel;
import com.hn.onelabel.server.infrastructure.db.query.SortTypeEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Repository
public class LabelRepositoryImpl implements LabelRepository {

    @Autowired
    private LocalCacheService localCacheService;
    @Autowired
    private LabelInfoMapper labelInfoMapper;

    @Override
    public Label findByCode(String labelCode) {
        return localCacheService.findLabelByLabelCode(labelCode);
    }

    @Override
    public List<String> findLabelCodeList(int pageNo, int pageSize) {
        Assert.isTrue(pageNo > 0, "Error pageNo.");
        Assert.isTrue(pageSize > 0, "Error pageSize.");
        LabelInfoQuery dynamicQuery = new LabelInfoQuery();
        dynamicQuery.setUsePaging(true);
        dynamicQuery.setOffset((pageNo - 1) * pageSize);
        dynamicQuery.setRows(pageSize);
        dynamicQuery.setUseSorting(true);
        List<SortModel> sortModelList = new ArrayList<>();
        sortModelList.add(new SortModel("create_time", SortTypeEnum.DESC.name()));
        dynamicQuery.setSortModelList(sortModelList);
        List<LabelInfoDO> labelInfoList = labelInfoMapper.selectByCondition(dynamicQuery);
        if (CollectionUtils.isEmpty(labelInfoList)) {
            return null;
        }
        return labelInfoList.stream().map(LabelInfoDO::getLabelCode).collect(Collectors.toList());
    }

}
