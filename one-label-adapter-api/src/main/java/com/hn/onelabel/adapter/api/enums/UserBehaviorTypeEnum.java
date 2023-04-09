package com.hn.onelabel.adapter.api.enums;

import lombok.Getter;
import org.springframework.util.StringUtils;

public enum UserBehaviorTypeEnum {
    MARKETING_BEHAVIOR_REGISTER(1L,"marketing_behavior_register", 15L),
    MARKETING_BEHAVIOR_CONSUMPTION_AND_ORDER_AMOUNT_0(4L,"marketing_behavior_consumption_order_amount_0", 16L),
    MARKETING_BEHAVIOR_CONSUMPTION_AND_ORDER_AMOUNT_GT_0(4L,"marketing_behavior_consumption_order_amount_gt_0", 17L),
    MARKETING_BEHAVIOR_JOIN_WECHAT_COMMUNITY(2L, "marketing_behavior_join_wechat_community", 18L)
    ;

    @Getter
    private final Long contextId;
    @Getter
    private final String labelCode;
    @Getter
    private final Long labelDimensionKeyId;

    UserBehaviorTypeEnum(Long contextId, String labelCode, Long labelDimensionKeyId) {
        this.contextId = contextId;
        this.labelCode = labelCode;
        this.labelDimensionKeyId = labelDimensionKeyId;
    }

    public static UserBehaviorTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (UserBehaviorTypeEnum userBehaviorTypeEnum : values()) {
            if (userBehaviorTypeEnum.name().equals(name)) {
                return userBehaviorTypeEnum;
            }
        }
        return null;
    }
}
