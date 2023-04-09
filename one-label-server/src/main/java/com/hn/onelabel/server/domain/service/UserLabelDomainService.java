package com.hn.onelabel.server.domain.service;

import com.alibaba.fastjson.JSONObject;
import com.hn.onelabel.api.enums.LabelOperationTypeEnum;
import com.hn.onelabel.server.domain.aggregate.labelrule.LabelRule;
import com.hn.onelabel.server.domain.aggregate.userlabel.UserLabel;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.Label;
import com.hn.onelabel.server.domain.aggregate.userlabel.valueobject.LabelDimension;
import org.apache.commons.lang3.tuple.Pair;

import java.util.List;
import java.util.Map;

public interface UserLabelDomainService {

    List<LabelRule> hits(JSONObject ruleContext, List<LabelRule> labelRules);

    UserLabel labelOperation(Long userId, List<Label> labels, List<LabelDimension> labelDimensions, List<LabelRule> labelRules, Map<String, Label> addLabelMap);

    UserLabel labelOperation(Long userId, List<Label> labels, List<LabelDimension> labelDimensions, LabelOperationTypeEnum labelOperationTypeEnum, String labelCode, Label addLabel);

    Pair<UserLabel, List<LabelDimension>> labelDimensionOperation(Long userId, List<Label> labels, List<LabelDimension> labelDimensions, Pair<String, List<LabelDimension>> addLabelDimensionPair, Pair<String, List<LabelDimension>> deleteLabelDimensionPair);
}
