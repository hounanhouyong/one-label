package com.hn.onelabel.api.enums;

import org.springframework.util.StringUtils;

public enum LabelDimensionUpdateStrategyEnum {
    // 标签下的维度KEY相同时更新
    KEY_UPDATE,
    // 标签下所有的维度先删除再写入
    OVER_WRITE
    ;

    public static LabelDimensionUpdateStrategyEnum getByName(String name) {
        if (StringUtils.isEmpty(name)) {
            return null;
        }
        for (LabelDimensionUpdateStrategyEnum labelDimensionUpdateStrategyEnum : values()) {
            if (labelDimensionUpdateStrategyEnum.name().equals(name)) {
                return labelDimensionUpdateStrategyEnum;
            }
        }
        return null;
    }

}
