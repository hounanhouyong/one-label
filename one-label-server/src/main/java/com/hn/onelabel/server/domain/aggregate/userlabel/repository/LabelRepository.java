package com.hn.onelabel.server.domain.aggregate.userlabel.repository;

import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.Label;

import java.util.List;

public interface LabelRepository {

    Label findByCode(String labelCode);

    List<String> findLabelCodeList(int pageNo, int pageSize);
}
