package com.hn.onelabel.server.infrastructure.db.repository.impl;

import com.hn.onelabel.api.enums.RuleStatusEnum;
import com.hn.onelabel.server.common.utils.LocalDateTimeUtils;
import com.hn.onelabel.server.domain.aggregate.labelrule.LabelRule;
import com.hn.onelabel.server.infrastructure.db.LabelHitRecordsDO;
import com.hn.onelabel.server.infrastructure.db.LabelRuleInfoDO;
import com.hn.onelabel.server.infrastructure.db.converter.LabelRuleInfoDOConverter;
import com.hn.onelabel.server.infrastructure.db.mapper.LabelHitRecordsMapper;
import com.hn.onelabel.server.infrastructure.db.mapper.LabelRuleInfoMapper;
import com.hn.onelabel.server.infrastructure.db.query.LabelRuleInfoQuery;
import com.hn.onelabel.server.infrastructure.nacos.SwitchConfigLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Repository;
import org.springframework.util.CollectionUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;

@Slf4j
@Repository
public class LabelRuleRepository {

    @Autowired
    private LabelRuleInfoMapper labelRuleInfoMapper;
    @Autowired
    private LabelHitRecordsMapper labelHitRecordsMapper;

    @Autowired
    private SwitchConfigLoader switchConfigLoader;

    public List<LabelRule> findLabelRules(Long ruleGroupId, boolean checkEffectiveTimeIsValid) {
        Objects.requireNonNull(ruleGroupId, "Null ruleGroupId.");
        LabelRuleInfoQuery dynamicQuery = new LabelRuleInfoQuery();
        dynamicQuery.setRuleGroupId(ruleGroupId);
        dynamicQuery.setRuleStatus(RuleStatusEnum.ENABLED.name());
        dynamicQuery.setValid(checkEffectiveTimeIsValid);
        return LabelRuleInfoDOConverter.INSTANCE.to(labelRuleInfoMapper.selectByCondition(dynamicQuery));
    }

    public List<LabelRule> findLabelRules(List<String> labelDimensionKeys, boolean checkEffectiveTimeIsValid) {
        Objects.requireNonNull(labelDimensionKeys, "Null labelDimensionKeys.");
        LabelRuleInfoQuery dynamicQuery = new LabelRuleInfoQuery();
        dynamicQuery.setLabelDimensionFixedKeyList(labelDimensionKeys);
        dynamicQuery.setRuleStatus(RuleStatusEnum.ENABLED.name());
        dynamicQuery.setValid(checkEffectiveTimeIsValid);
        return LabelRuleInfoDOConverter.INSTANCE.to(labelRuleInfoMapper.selectByCondition(dynamicQuery));
    }

    public void saveLabelRuleInfo(LabelRule labelRule, String creator) {
        try {
            LabelRuleInfoDO entity = LabelRuleInfoDOConverter.INSTANCE.from(labelRule);
            entity.setRuleStatus(RuleStatusEnum.ENABLED.name());
            entity.setCreator(creator);
            entity.setModifier(creator);
            labelRuleInfoMapper.insertEntity(entity);
        } catch (Exception e) {
            log.error("[saveLabelRuleInfo] - insertEntity exception.", e);
        }
    }

    public void updateLabelRuleInfo(LabelRule labelRule, String modifier) {
        Objects.requireNonNull(labelRule.getRuleId(), "Null ruleId.");
        try {
            LabelRuleInfoDO entity = LabelRuleInfoDOConverter.INSTANCE.from(labelRule);
            entity.setModifier(modifier);
            labelRuleInfoMapper.updateByPrimaryKey(entity);
        } catch (Exception e) {
            log.error("[updateLabelRuleInfo] - updateEntity exception.", e);
        }
    }

    public void deleteLabelRuleInfo(Long ruleId) {
        Objects.requireNonNull(ruleId, "Null ruleId.");
        labelRuleInfoMapper.logicalDeleteByPrimaryKey(ruleId);
    }


    @Async("taskExecutor")
    public void saveLabelHitRecords(String sequenceId, Long userId, List<LabelRule> hitsLabelRules, Long ruleContextId, String ruleContext) {

        if (!switchConfigLoader.insertLabelHitRecordsSwitchIsOpen()) {
            return;
        }

        Objects.requireNonNull(userId, "Null userId.");
        Objects.requireNonNull(ruleContextId, "Null ruleContextId.");
        if (CollectionUtils.isEmpty(hitsLabelRules)) {
            return;
        }
        try {
            hitsLabelRules.forEach(hitsLabelRule -> {
                LabelHitRecordsDO hitRecord = new LabelHitRecordsDO();
                hitRecord.setSequenceId(sequenceId);
                hitRecord.setUserId(userId);
                hitRecord.setLabelCode(hitsLabelRule.getRuleDefineLabel().getLabelCode());
                hitRecord.setLabelRuleId(hitsLabelRule.getRuleId());
                hitRecord.setLabelRuleType(hitsLabelRule.getRuleType());
                hitRecord.setLabelRuleContextId(ruleContextId);
                hitRecord.setLabelRuleContext(ruleContext);
                labelHitRecordsMapper.insertEntity(LocalDateTimeUtils.getFormatDateInt(LocalDateTime.now(), "yyyyMM"), hitRecord);
            });
        } catch (Exception e) {
            log.error("[saveLabelHitRecords] - insertEntity exception.", e);
        }
    }
}
