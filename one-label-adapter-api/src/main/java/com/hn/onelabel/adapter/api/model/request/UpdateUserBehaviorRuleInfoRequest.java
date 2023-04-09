package com.hn.onelabel.adapter.api.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Data
public class UpdateUserBehaviorRuleInfoRequest {
    private String behaviorId;
    private LocalDateTime behaviorRuleEffectiveEndTime;
    private String modifier;
}
