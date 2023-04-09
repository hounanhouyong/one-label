package com.hn.onelabel.server.infrastructure.db.converter;

import com.hn.onelabel.server.domain.aggregate.labelrule.LabelRule;
import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleDefineLabel;
import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleEffectiveTime;
import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleScript;
import com.hn.onelabel.server.infrastructure.db.LabelRuleInfoDO;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-04-01T18:43:13+0800",
    comments = "version: 1.3.0.Final, compiler: javac, environment: Java 1.8.0_231 (Oracle Corporation)"
)
public class LabelRuleInfoDOConverterImpl implements LabelRuleInfoDOConverter {

    @Override
    public LabelRuleInfoDO from(LabelRule record) {
        if ( record == null ) {
            return null;
        }

        LabelRuleInfoDO labelRuleInfoDO = new LabelRuleInfoDO();

        labelRuleInfoDO.setRuleEffectiveEndTime( localDateTime2Date( recordRuleEffectiveTimeRuleEffectiveEndTime( record ) ) );
        labelRuleInfoDO.setRuleEffectiveStartTime( localDateTime2Date( recordRuleEffectiveTimeRuleEffectiveStartTime( record ) ) );
        labelRuleInfoDO.setRuleScriptType( recordRuleScriptRuleScriptType( record ) );
        labelRuleInfoDO.setLabelDimensionKeyId( recordRuleDefineLabelLabelDimensionKeyId( record ) );
        labelRuleInfoDO.setId( record.getRuleId() );
        labelRuleInfoDO.setRuleScriptContent( recordRuleScriptRuleScriptContent( record ) );
        labelRuleInfoDO.setLabelCode( recordRuleDefineLabelLabelCode( record ) );
        labelRuleInfoDO.setLabelDimensionKeyDefineType( recordRuleDefineLabelLabelDimensionKeyDefineType( record ) );
        labelRuleInfoDO.setLabelDimensionFixedKey( recordRuleDefineLabelLabelDimensionFixedKey( record ) );
        labelRuleInfoDO.setRuleContextId( record.getRuleContextId() );
        labelRuleInfoDO.setRuleGroupId( record.getRuleGroupId() );
        labelRuleInfoDO.setRuleType( record.getRuleType() );
        labelRuleInfoDO.setRuleName( record.getRuleName() );
        labelRuleInfoDO.setRuleDesc( record.getRuleDesc() );
        labelRuleInfoDO.setExternalTag( record.getExternalTag() );

        return labelRuleInfoDO;
    }

    @Override
    public List<LabelRuleInfoDO> from(List<LabelRule> list) {
        if ( list == null ) {
            return null;
        }

        List<LabelRuleInfoDO> list1 = new ArrayList<LabelRuleInfoDO>( list.size() );
        for ( LabelRule labelRule : list ) {
            list1.add( from( labelRule ) );
        }

        return list1;
    }

    @Override
    public LabelRule to(LabelRuleInfoDO record) {
        if ( record == null ) {
            return null;
        }

        LabelRule labelRule = new LabelRule();

        labelRule.setRuleDefineLabel( labelRuleInfoDOToRuleDefineLabel( record ) );
        labelRule.setRuleEffectiveTime( labelRuleInfoDOToRuleEffectiveTime( record ) );
        labelRule.setRuleScript( labelRuleInfoDOToRuleScript( record ) );
        labelRule.setRuleId( record.getId() );
        labelRule.setRuleContextId( record.getRuleContextId() );
        labelRule.setRuleGroupId( record.getRuleGroupId() );
        labelRule.setRuleName( record.getRuleName() );
        labelRule.setRuleDesc( record.getRuleDesc() );
        labelRule.setRuleType( record.getRuleType() );
        labelRule.setExternalTag( record.getExternalTag() );

        return labelRule;
    }

    @Override
    public List<LabelRule> to(List<LabelRuleInfoDO> list) {
        if ( list == null ) {
            return null;
        }

        List<LabelRule> list1 = new ArrayList<LabelRule>( list.size() );
        for ( LabelRuleInfoDO labelRuleInfoDO : list ) {
            list1.add( to( labelRuleInfoDO ) );
        }

        return list1;
    }

    private LocalDateTime recordRuleEffectiveTimeRuleEffectiveEndTime(LabelRule labelRule) {
        if ( labelRule == null ) {
            return null;
        }
        RuleEffectiveTime ruleEffectiveTime = labelRule.getRuleEffectiveTime();
        if ( ruleEffectiveTime == null ) {
            return null;
        }
        LocalDateTime ruleEffectiveEndTime = ruleEffectiveTime.getRuleEffectiveEndTime();
        if ( ruleEffectiveEndTime == null ) {
            return null;
        }
        return ruleEffectiveEndTime;
    }

    private LocalDateTime recordRuleEffectiveTimeRuleEffectiveStartTime(LabelRule labelRule) {
        if ( labelRule == null ) {
            return null;
        }
        RuleEffectiveTime ruleEffectiveTime = labelRule.getRuleEffectiveTime();
        if ( ruleEffectiveTime == null ) {
            return null;
        }
        LocalDateTime ruleEffectiveStartTime = ruleEffectiveTime.getRuleEffectiveStartTime();
        if ( ruleEffectiveStartTime == null ) {
            return null;
        }
        return ruleEffectiveStartTime;
    }

    private String recordRuleScriptRuleScriptType(LabelRule labelRule) {
        if ( labelRule == null ) {
            return null;
        }
        RuleScript ruleScript = labelRule.getRuleScript();
        if ( ruleScript == null ) {
            return null;
        }
        String ruleScriptType = ruleScript.getRuleScriptType();
        if ( ruleScriptType == null ) {
            return null;
        }
        return ruleScriptType;
    }

    private Long recordRuleDefineLabelLabelDimensionKeyId(LabelRule labelRule) {
        if ( labelRule == null ) {
            return null;
        }
        RuleDefineLabel ruleDefineLabel = labelRule.getRuleDefineLabel();
        if ( ruleDefineLabel == null ) {
            return null;
        }
        Long labelDimensionKeyId = ruleDefineLabel.getLabelDimensionKeyId();
        if ( labelDimensionKeyId == null ) {
            return null;
        }
        return labelDimensionKeyId;
    }

    private String recordRuleScriptRuleScriptContent(LabelRule labelRule) {
        if ( labelRule == null ) {
            return null;
        }
        RuleScript ruleScript = labelRule.getRuleScript();
        if ( ruleScript == null ) {
            return null;
        }
        String ruleScriptContent = ruleScript.getRuleScriptContent();
        if ( ruleScriptContent == null ) {
            return null;
        }
        return ruleScriptContent;
    }

    private String recordRuleDefineLabelLabelCode(LabelRule labelRule) {
        if ( labelRule == null ) {
            return null;
        }
        RuleDefineLabel ruleDefineLabel = labelRule.getRuleDefineLabel();
        if ( ruleDefineLabel == null ) {
            return null;
        }
        String labelCode = ruleDefineLabel.getLabelCode();
        if ( labelCode == null ) {
            return null;
        }
        return labelCode;
    }

    private String recordRuleDefineLabelLabelDimensionKeyDefineType(LabelRule labelRule) {
        if ( labelRule == null ) {
            return null;
        }
        RuleDefineLabel ruleDefineLabel = labelRule.getRuleDefineLabel();
        if ( ruleDefineLabel == null ) {
            return null;
        }
        String labelDimensionKeyDefineType = ruleDefineLabel.getLabelDimensionKeyDefineType();
        if ( labelDimensionKeyDefineType == null ) {
            return null;
        }
        return labelDimensionKeyDefineType;
    }

    private String recordRuleDefineLabelLabelDimensionFixedKey(LabelRule labelRule) {
        if ( labelRule == null ) {
            return null;
        }
        RuleDefineLabel ruleDefineLabel = labelRule.getRuleDefineLabel();
        if ( ruleDefineLabel == null ) {
            return null;
        }
        String labelDimensionFixedKey = ruleDefineLabel.getLabelDimensionFixedKey();
        if ( labelDimensionFixedKey == null ) {
            return null;
        }
        return labelDimensionFixedKey;
    }

    protected RuleDefineLabel labelRuleInfoDOToRuleDefineLabel(LabelRuleInfoDO labelRuleInfoDO) {
        if ( labelRuleInfoDO == null ) {
            return null;
        }

        RuleDefineLabel ruleDefineLabel = new RuleDefineLabel();

        ruleDefineLabel.setLabelDimensionFixedKey( labelRuleInfoDO.getLabelDimensionFixedKey() );
        ruleDefineLabel.setLabelDimensionKeyDefineType( labelRuleInfoDO.getLabelDimensionKeyDefineType() );
        ruleDefineLabel.setLabelDimensionKeyId( labelRuleInfoDO.getLabelDimensionKeyId() );
        ruleDefineLabel.setLabelCode( labelRuleInfoDO.getLabelCode() );

        return ruleDefineLabel;
    }

    protected RuleEffectiveTime labelRuleInfoDOToRuleEffectiveTime(LabelRuleInfoDO labelRuleInfoDO) {
        if ( labelRuleInfoDO == null ) {
            return null;
        }

        RuleEffectiveTime ruleEffectiveTime = new RuleEffectiveTime();

        ruleEffectiveTime.setRuleEffectiveStartTime( date2LocalDateTime( labelRuleInfoDO.getRuleEffectiveStartTime() ) );
        ruleEffectiveTime.setRuleEffectiveEndTime( date2LocalDateTime( labelRuleInfoDO.getRuleEffectiveEndTime() ) );

        return ruleEffectiveTime;
    }

    protected RuleScript labelRuleInfoDOToRuleScript(LabelRuleInfoDO labelRuleInfoDO) {
        if ( labelRuleInfoDO == null ) {
            return null;
        }

        RuleScript ruleScript = new RuleScript();

        ruleScript.setRuleScriptContent( labelRuleInfoDO.getRuleScriptContent() );
        ruleScript.setRuleScriptType( labelRuleInfoDO.getRuleScriptType() );

        return ruleScript;
    }
}
