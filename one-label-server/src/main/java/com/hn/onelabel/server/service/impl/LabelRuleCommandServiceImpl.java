package com.hn.onelabel.server.service.impl;

import com.hn.onelabel.api.enums.LabelDimensionKeyDefineTypeEnum;
import com.hn.onelabel.api.enums.RuleScriptTypeEnum;
import com.hn.onelabel.api.enums.RuleTypeEnum;
import com.hn.onelabel.api.model.request.SaveLabelRuleInfoRequest;
import com.hn.onelabel.server.domain.aggregate.labelrule.LabelRule;
import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleDefineLabel;
import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleEffectiveTime;
import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleScript;
import com.hn.onelabel.server.infrastructure.db.repository.impl.LabelRuleRepository;
import com.hn.onelabel.server.service.LabelRuleCommandService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;

import java.util.Objects;

@Slf4j
@Service
public class LabelRuleCommandServiceImpl implements LabelRuleCommandService {

    @Autowired
    private LabelRuleRepository labelRuleRepository;

    @Override
    public void saveLabelRuleInfo(SaveLabelRuleInfoRequest request) {
        if (Objects.isNull(request.getRuleId())) {
            Objects.requireNonNull(request.getRuleContextId(), "Null ruleContextId.");
            Objects.requireNonNull(request.getRuleGroupId(), "Null ruleGroupId.");
            Objects.requireNonNull(RuleTypeEnum.getByName(request.getRuleType()), "Error ruleType.");
            Objects.requireNonNull(request.getRuleName(), "Null ruleName.");
            Objects.requireNonNull(RuleScriptTypeEnum.getByName(request.getRuleScriptType()), "Error ruleScriptType.");
            Objects.requireNonNull(request.getRuleScriptContent(), "Null ruleScriptContent.");
            Objects.requireNonNull(request.getRuleEffectiveStartTime(), "Null ruleEffectiveStartTime.");
            Objects.requireNonNull(request.getRuleEffectiveEndTime(), "Null ruleEffectiveEndTime.");
            Assert.isTrue(request.getRuleEffectiveEndTime().isAfter(request.getRuleEffectiveStartTime()), "Error ruleEffectiveTime.");
            Objects.requireNonNull(request.getLabelCode(), "Null labelCode.");
            if (RuleTypeEnum.isLabelDimensionRuleType(request.getRuleType())) {
                Objects.requireNonNull(request.getLabelDimensionKeyId(), "Null labelDimensionKeyId.");
                Objects.requireNonNull(LabelDimensionKeyDefineTypeEnum.getByName(request.getLabelDimensionKeyDefineType()), "Error labelDimensionKeyDefineType.");
                if (request.getLabelDimensionKeyDefineType().equals(LabelDimensionKeyDefineTypeEnum.FIXED_KEY.name())) {
                    Objects.requireNonNull(request.getLabelDimensionFixedKey(), "Null labelDimensionFixedKey.");
                }
            }
            labelRuleRepository.saveLabelRuleInfo(
                    new LabelRule(request.getRuleContextId(), request.getRuleGroupId(), null, request.getRuleName(), request.getRuleDesc(), request.getRuleType(),
                            new RuleScript(request.getRuleScriptType(), request.getRuleScriptContent()),
                            new RuleEffectiveTime(request.getRuleEffectiveStartTime(), request.getRuleEffectiveEndTime()),
                            new RuleDefineLabel(request.getLabelCode(), request.getLabelDimensionKeyId(), request.getLabelDimensionKeyDefineType(), request.getLabelDimensionFixedKey()),
                            request.getExternalTags()),
                    request.getCreator()
            );
        } else {
            Objects.requireNonNull(request.getRuleId(), "Null ruleId.");
            labelRuleRepository.updateLabelRuleInfo(
                    new LabelRule(request.getRuleContextId(), request.getRuleGroupId(), request.getRuleId(), request.getRuleName(), request.getRuleDesc(), request.getRuleType(),
                            new RuleScript(request.getRuleScriptType(), request.getRuleScriptContent()),
                            new RuleEffectiveTime(request.getRuleEffectiveStartTime(), request.getRuleEffectiveEndTime()),
                            new RuleDefineLabel(request.getLabelCode(), request.getLabelDimensionKeyId(), request.getLabelDimensionKeyDefineType(), request.getLabelDimensionFixedKey()),
                            request.getExternalTags()),
                    request.getModifier()
            );
        }
    }

    @Override
    public void deleteLabelRuleInfo(Long ruleId) {
        Objects.requireNonNull(ruleId, "Null ruleId.");
        labelRuleRepository.deleteLabelRuleInfo(ruleId);
    }
}
