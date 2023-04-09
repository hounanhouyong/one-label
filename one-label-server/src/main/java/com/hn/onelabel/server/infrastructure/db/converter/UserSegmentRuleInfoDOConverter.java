package com.hn.onelabel.server.infrastructure.db.converter;

import com.hn.onelabel.server.domain.aggregate.usersegment.UserSegment;
import com.hn.onelabel.server.infrastructure.db.UserSegmentRuleInfoDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.factory.Mappers;

import java.util.List;

@Mapper
public interface UserSegmentRuleInfoDOConverter {
    UserSegmentRuleInfoDOConverter INSTANCE = Mappers.getMapper(UserSegmentRuleInfoDOConverter.class);
    @Mappings({
            @Mapping(source = "record.ruleScript.ruleScriptType", target = "ruleScriptType"),
            @Mapping(source = "record.ruleScript.ruleScriptContent", target = "ruleScriptContent")
    })
    UserSegmentRuleInfoDO from(UserSegment record);
    List<UserSegmentRuleInfoDO> from(List<UserSegment> list);
    @Mappings({
            @Mapping(source = "record.id", target = "segmentId"),
            @Mapping(source = "record.ruleScriptType", target = "ruleScript.ruleScriptType"),
            @Mapping(source = "record.ruleScriptContent", target = "ruleScript.ruleScriptContent")
    })
    UserSegment to(UserSegmentRuleInfoDO record);
    List<UserSegment> to(List<UserSegmentRuleInfoDO> list);
}
