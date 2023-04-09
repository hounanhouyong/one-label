package com.hn.onelabel.server.infrastructure.db.converter;

import com.hn.onelabel.server.domain.aggregate.usersegment.valueobject.AbExperiment;
import com.hn.onelabel.server.infrastructure.db.UserAbExperimentInfoDO;
import java.util.ArrayList;
import java.util.List;
import javax.annotation.Generated;

@Generated(
    value = "org.mapstruct.ap.MappingProcessor",
    date = "2023-04-01T18:43:13+0800",
    comments = "version: 1.3.0.Final, compiler: javac, environment: Java 1.8.0_231 (Oracle Corporation)"
)
public class UserAbExperimentInfoDOConverterImpl implements UserAbExperimentInfoDOConverter {

    @Override
    public UserAbExperimentInfoDO from(Long segmentRuleId, AbExperiment record) {
        if ( segmentRuleId == null && record == null ) {
            return null;
        }

        UserAbExperimentInfoDO userAbExperimentInfoDO = new UserAbExperimentInfoDO();

        if ( segmentRuleId != null ) {
            userAbExperimentInfoDO.setSegmentRuleId( segmentRuleId );
        }
        if ( record != null ) {
            userAbExperimentInfoDO.setExperimentId( record.getExperimentId() );
            userAbExperimentInfoDO.setWeight( record.getWeight() );
            userAbExperimentInfoDO.setExternalExperimentGroupId( record.getExternalExperimentGroupId() );
            userAbExperimentInfoDO.setExternalExperimentCode( record.getExternalExperimentCode() );
            userAbExperimentInfoDO.setExternalExperimentExtInfo( record.getExternalExperimentExtInfo() );
            userAbExperimentInfoDO.setExternalExperimentTag( record.getExternalExperimentTag() );
        }

        return userAbExperimentInfoDO;
    }

    @Override
    public AbExperiment to(UserAbExperimentInfoDO record) {
        if ( record == null ) {
            return null;
        }

        AbExperiment abExperiment = new AbExperiment();

        abExperiment.setExperimentId( record.getExperimentId() );
        abExperiment.setWeight( record.getWeight() );
        abExperiment.setExternalExperimentGroupId( record.getExternalExperimentGroupId() );
        abExperiment.setExternalExperimentCode( record.getExternalExperimentCode() );
        abExperiment.setExternalExperimentExtInfo( record.getExternalExperimentExtInfo() );
        abExperiment.setExternalExperimentTag( record.getExternalExperimentTag() );

        return abExperiment;
    }

    @Override
    public List<AbExperiment> to(List<UserAbExperimentInfoDO> list) {
        if ( list == null ) {
            return null;
        }

        List<AbExperiment> list1 = new ArrayList<AbExperiment>( list.size() );
        for ( UserAbExperimentInfoDO userAbExperimentInfoDO : list ) {
            list1.add( to( userAbExperimentInfoDO ) );
        }

        return list1;
    }
}
