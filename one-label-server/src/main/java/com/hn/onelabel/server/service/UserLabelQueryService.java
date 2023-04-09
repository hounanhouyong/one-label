package com.hn.onelabel.server.service;

import com.hn.onelabel.api.model.request.FindUserHasLabelRequest;
import com.hn.onelabel.api.model.request.FindUserHasLabelsRequest;

import java.util.List;

public interface UserLabelQueryService {
    Boolean userHasLabel(FindUserHasLabelRequest request);
    Boolean userHasLabels(FindUserHasLabelsRequest request);
    List<String> findUserLabels(FindUserHasLabelsRequest request);
}
