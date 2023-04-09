package com.hn.onelabel.server.domain.aggregate.labelrule.valueobject;

import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@Data
public class RuleEffectiveTime {
    private LocalDateTime ruleEffectiveStartTime;
    private LocalDateTime ruleEffectiveEndTime;

    public RuleEffectiveTime(LocalDateTime ruleEffectiveStartTime, LocalDateTime ruleEffectiveEndTime) {
        this.ruleEffectiveStartTime = ruleEffectiveStartTime;
        this.ruleEffectiveEndTime = ruleEffectiveEndTime;
    }
}
