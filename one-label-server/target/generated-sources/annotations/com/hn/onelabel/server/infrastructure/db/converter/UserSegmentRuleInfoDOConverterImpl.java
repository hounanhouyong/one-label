package com.hn.onelabel.server.infrastructure.db.converter;

import com.hn.onelabel.server.domain.aggregate.labelrule.valueobject.RuleScript;
import com.hn.onelabel.server.domain.aggregate.usersegment.UserSegment;
import com.hn.onelabel.server.infrastructure.db.UserSegmentRuleInfoDO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-04-01T18:43:13+0800",
    comments = "version: 1.3.0.Final, compiler: javac, environment: Java 1.8.0_231 (Oracle Corporation)"
)
public class UserSegmentRuleInfoDOConverterImpl implements UserSegmentRuleInfoDOConverter {

    @Override
    public UserSegmentRuleInfoDO from(UserSegment record) {
        if ( record == null ) {
            return null;
        }

        UserSegmentRuleInfoDO userSegmentRuleInfoDO = new UserSegmentRuleInfoDO();

        userSegmentRuleInfoDO.setRuleScriptContent( recordRuleScriptRuleScriptContent( record ) );
        userSegmentRuleInfoDO.setRuleScriptType( recordRuleScriptRuleScriptType( record ) );
        userSegmentRuleInfoDO.setSegmentType( record.getSegmentType() );
        userSegmentRuleInfoDO.setSegmentName( record.getSegmentName() );
        userSegmentRuleInfoDO.setExternalExperimentGroupId( record.getExternalExperimentGroupId() );

        return userSegmentRuleInfoDO;
    }

    @Override
    public List<UserSegmentRuleInfoDO> from(List<UserSegment> list) {
        if ( list == null ) {
            return null;
        }

        List<UserSegmentRuleInfoDO> list1 = new ArrayList<UserSegmentRuleInfoDO>( list.size() );
        for ( UserSegment userSegment : list ) {
            list1.add( from( userSegment ) );
        }

        return list1;
    }

    @Override
    public UserSegment to(UserSegmentRuleInfoDO record) {
        if ( record == null ) {
            return null;
        }

        UserSegment userSegment = new UserSegment();

        userSegment.setRuleScript( userSegmentRuleInfoDOToRuleScript( record ) );
        userSegment.setSegmentId( record.getId() );
        userSegment.setSegmentType( record.getSegmentType() );
        userSegment.setSegmentName( record.getSegmentName() );
        userSegment.setExternalExperimentGroupId( record.getExternalExperimentGroupId() );

        return userSegment;
    }

    @Override
    public List<UserSegment> to(List<UserSegmentRuleInfoDO> list) {
        if ( list == null ) {
            return null;
        }

        List<UserSegment> list1 = new ArrayList<UserSegment>( list.size() );
        for ( UserSegmentRuleInfoDO userSegmentRuleInfoDO : list ) {
            list1.add( to( userSegmentRuleInfoDO ) );
        }

        return list1;
    }

    private String recordRuleScriptRuleScriptContent(UserSegment userSegment) {
        if ( userSegment == null ) {
            return null;
        }
        RuleScript ruleScript = userSegment.getRuleScript();
        if ( ruleScript == null ) {
            return null;
        }
        String ruleScriptContent = ruleScript.getRuleScriptContent();
        if ( ruleScriptContent == null ) {
            return null;
        }
        return ruleScriptContent;
    }

    private String recordRuleScriptRuleScriptType(UserSegment userSegment) {
        if ( userSegment == null ) {
            return null;
        }
        RuleScript ruleScript = userSegment.getRuleScript();
        if ( ruleScript == null ) {
            return null;
        }
        String ruleScriptType = ruleScript.getRuleScriptType();
        if ( ruleScriptType == null ) {
            return null;
        }
        return ruleScriptType;
    }

    protected RuleScript userSegmentRuleInfoDOToRuleScript(UserSegmentRuleInfoDO userSegmentRuleInfoDO) {
        if ( userSegmentRuleInfoDO == null ) {
            return null;
        }

        RuleScript ruleScript = new RuleScript();

        ruleScript.setRuleScriptContent( userSegmentRuleInfoDO.getRuleScriptContent() );
        ruleScript.setRuleScriptType( userSegmentRuleInfoDO.getRuleScriptType() );

        return ruleScript;
    }
}
