package com.hn.onelabel.adapter.api.enums;

import lombok.Getter;
import org.springframework.util.StringUtils;

public enum TouchFatigueTypeEnum {
    TOUCH_FATIGUE_POP_UP("touch_fatigue_popup"),
    TOUCH_FATIGUE_RECOMMEND("touch_fatigue_recommend_position"),
    TOUCH_FATIGUE_POP_UP_CYCLE("touch_fatigue_popup_cycle"),
    BROWSE_ANNUAL_ACCOUNT_FATIGUE("browse_annual_account_fatigue")
    ;

    @Getter
    private final String labelCode;

    TouchFatigueTypeEnum(String labelCode) {
        this.labelCode = labelCode;
    }

    public static TouchFatigueTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (TouchFatigueTypeEnum touchFatigueTypeEnum : values()) {
            if (touchFatigueTypeEnum.name().equals(name)) {
                return touchFatigueTypeEnum;
            }
        }
        return null;
    }
}
