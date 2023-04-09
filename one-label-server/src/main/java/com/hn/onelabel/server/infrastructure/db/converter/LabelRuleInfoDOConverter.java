package com.hn.onelabel.server.infrastructure.db.converter;

import com.hn.onelabel.server.domain.aggregate.labelrule.LabelRule;
import com.hn.onelabel.server.infrastructure.db.LabelRuleInfoDO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;
import org.mapstruct.Named;
import org.mapstruct.factory.Mappers;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Mapper
public interface LabelRuleInfoDOConverter {
    LabelRuleInfoDOConverter INSTANCE = Mappers.getMapper(LabelRuleInfoDOConverter.class);
    @Mappings({
            @Mapping(source = "record.ruleId", target = "id"),
            @Mapping(source = "record.ruleScript.ruleScriptType", target = "ruleScriptType"),
            @Mapping(source = "record.ruleScript.ruleScriptContent", target = "ruleScriptContent"),
            @Mapping(source = "record.ruleEffectiveTime.ruleEffectiveStartTime", target = "ruleEffectiveStartTime", qualifiedByName = "localDateTime2Date"),
            @Mapping(source = "record.ruleEffectiveTime.ruleEffectiveEndTime", target = "ruleEffectiveEndTime", qualifiedByName = "localDateTime2Date"),
            @Mapping(source = "record.ruleDefineLabel.labelCode", target = "labelCode"),
            @Mapping(source = "record.ruleDefineLabel.labelDimensionKeyId", target = "labelDimensionKeyId"),
            @Mapping(source = "record.ruleDefineLabel.labelDimensionKeyDefineType", target = "labelDimensionKeyDefineType"),
            @Mapping(source = "record.ruleDefineLabel.labelDimensionFixedKey", target = "labelDimensionFixedKey")
    })
    LabelRuleInfoDO from(LabelRule record);
    List<LabelRuleInfoDO> from(List<LabelRule> list);
    @Mappings({
            @Mapping(source = "record.id", target = "ruleId"),
            @Mapping(source = "record.ruleScriptType", target = "ruleScript.ruleScriptType"),
            @Mapping(source = "record.ruleScriptContent", target = "ruleScript.ruleScriptContent"),
            @Mapping(source = "record.ruleEffectiveStartTime", target = "ruleEffectiveTime.ruleEffectiveStartTime", qualifiedByName = "date2LocalDateTime"),
            @Mapping(source = "record.ruleEffectiveEndTime", target = "ruleEffectiveTime.ruleEffectiveEndTime", qualifiedByName = "date2LocalDateTime"),
            @Mapping(source = "record.labelCode", target = "ruleDefineLabel.labelCode"),
            @Mapping(source = "record.labelDimensionKeyId", target = "ruleDefineLabel.labelDimensionKeyId"),
            @Mapping(source = "record.labelDimensionKeyDefineType", target = "ruleDefineLabel.labelDimensionKeyDefineType"),
            @Mapping(source = "record.labelDimensionFixedKey", target = "ruleDefineLabel.labelDimensionFixedKey")
    })
    LabelRule to(LabelRuleInfoDO record);
    List<LabelRule> to(List<LabelRuleInfoDO> list);


    @Named("localDateTime2Date")
    default Date localDateTime2Date(LocalDateTime localDateTime) {
        if (Objects.isNull(localDateTime)) {
            return null;
        }
        ZoneId zoneId = ZoneId.of("GMT+08:00");
        ZonedDateTime zdt = localDateTime.atZone(zoneId);
        return Date.from(zdt.toInstant());
    }

    @Named("date2LocalDateTime")
    default LocalDateTime date2LocalDateTime(Date date) {
        if (Objects.isNull(date)) {
            return null;
        }
        Instant instant = date.toInstant();
        ZoneId zoneId = ZoneId.of("GMT+08:00");
        return instant.atZone(zoneId).toLocalDateTime();
    }
}
