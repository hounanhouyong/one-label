package com.hn.onelabel.adapter.api.enums;

import lombok.Getter;
import org.springframework.util.StringUtils;

import java.util.Objects;

public enum TouchTypeEnum {
    PROMOTION_PLAN_SMS_TOUCH(2,"promotion_plan_sms_touch"),
    PROMOTION_PLAN_WECHAT_TOUCH(1,"promotion_plan_wechat_touch"),
    PROMOTION_PLAN_ENTERPRISE_WECHAT_TOUCH(3,"promotion_plan_enterprise_wechat_touch")
    ;

    @Getter
    private final Integer channelCode;
    @Getter
    private final String labelCode;

    TouchTypeEnum(Integer channelCode, String labelCode) {
        this.channelCode = channelCode;
        this.labelCode = labelCode;
    }

    public static TouchTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (TouchTypeEnum touchTypeEnum : values()) {
            if (touchTypeEnum.name().equals(name)) {
                return touchTypeEnum;
            }
        }
        return null;
    }

    public static TouchTypeEnum getByChannelCode(Integer channelCode) {
        if (Objects.isNull(channelCode)) {
            return null;
        }
        for (TouchTypeEnum touchTypeEnum : values()) {
            if (touchTypeEnum.getChannelCode().equals(channelCode)) {
                return touchTypeEnum;
            }
        }
        return null;
    }

}
