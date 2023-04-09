package com.hn.onelabel.server.service;

import com.hn.onelabel.server.domain.aggregate.labelrule.LabelRule;

import java.util.List;

public interface LabelRuleQueryService {

    List<LabelRule> findLabelRules(List<String> labelDimensionKeys, boolean checkEffectiveTimeIsValid);
}
