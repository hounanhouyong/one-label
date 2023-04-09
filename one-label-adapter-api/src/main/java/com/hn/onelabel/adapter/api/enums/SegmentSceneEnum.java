package com.hn.onelabel.adapter.api.enums;

import lombok.Getter;
import org.springframework.util.StringUtils;

public enum SegmentSceneEnum {
    WELFARE_CENTER("welfare_center_recommend_position")
    ;

    @Getter
    private final String labelCode;

    SegmentSceneEnum(String labelCode) {
        this.labelCode = labelCode;
    }

    public static SegmentSceneEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (SegmentSceneEnum segmentSceneEnum : values()) {
            if (segmentSceneEnum.name().equals(name)) {
                return segmentSceneEnum;
            }
        }
        return null;
    }

}
