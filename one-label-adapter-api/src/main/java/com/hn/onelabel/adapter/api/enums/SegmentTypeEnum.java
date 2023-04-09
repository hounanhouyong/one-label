package com.hn.onelabel.adapter.api.enums;

import org.springframework.util.StringUtils;

public enum SegmentTypeEnum {
    PROMOTION_PLAN
    ;

    public static SegmentTypeEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (SegmentTypeEnum segmentTypeEnum : values()) {
            if (segmentTypeEnum.name().equals(name)) {
                return segmentTypeEnum;
            }
        }
        return null;
    }
}
