package com.hn.onelabel.server.service;

import com.hn.onelabel.api.model.request.SaveLabelRuleInfoRequest;

public interface LabelRuleCommandService {

    void saveLabelRuleInfo(SaveLabelRuleInfoRequest request);

    void deleteLabelRuleInfo(Long ruleId);
}
