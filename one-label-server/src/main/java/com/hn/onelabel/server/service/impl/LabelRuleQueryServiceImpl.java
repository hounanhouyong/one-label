package com.hn.onelabel.server.service.impl;

import com.alibaba.fastjson.JSON;
import com.hn.onelabel.server.domain.aggregate.labelrule.LabelRule;
import com.hn.onelabel.server.infrastructure.db.repository.impl.LabelRuleRepository;
import com.hn.onelabel.server.service.LabelRuleQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class LabelRuleQueryServiceImpl implements LabelRuleQueryService {

    @Autowired
    private LabelRuleRepository labelRuleRepository;

    @Override
    public List<LabelRule> findLabelRules(List<String> labelDimensionKeys, boolean checkEffectiveTimeIsValid) {
        log.info("[findLabelRules] - query: {}.", JSON.toJSONString(labelDimensionKeys));
        List<LabelRule> labelRules = labelRuleRepository.findLabelRules(labelDimensionKeys, checkEffectiveTimeIsValid);
        return !CollectionUtils.isEmpty(labelRules) ? labelRules : new ArrayList<>();
    }
}
