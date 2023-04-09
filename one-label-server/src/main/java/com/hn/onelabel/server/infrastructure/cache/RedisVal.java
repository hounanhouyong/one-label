package com.hn.onelabel.server.infrastructure.cache;

import org.springframework.util.StringUtils;

public class RedisVal {

    private final static String VAL_USER_LABEL_DIMENSIONS_INVALID = "%s:%s:%s";

    public static String getUserLabelDimensionsInvalidRedisVal(Long userId, Long dimensionKeyId, String dimensionKey) {
        return String.format(VAL_USER_LABEL_DIMENSIONS_INVALID, userId, dimensionKeyId, dimensionKey);
    }

    public static Long getUserIdFromUldInvalidRedisVal(String redisVal) {
        if (StringUtils.isEmpty(redisVal)) {
            return null;
        }
        return Long.parseLong(redisVal.split(":")[0]);
    }

    public static Long getDimensionKeyIdFromUldInvalidRedisVal(String redisVal) {
        if (StringUtils.isEmpty(redisVal)) {
            return null;
        }
        return Long.parseLong(redisVal.split(":")[1]);
    }

    public static String getDimensionKeyFromUldInvalidRedisVal(String redisVal) {
        if (StringUtils.isEmpty(redisVal)) {
            return null;
        }
        return redisVal.split(":")[2];
    }

}
