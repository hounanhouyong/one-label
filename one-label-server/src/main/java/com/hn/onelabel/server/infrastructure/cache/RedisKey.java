package com.hn.onelabel.server.infrastructure.cache;

import com.hn.onelabel.server.common.utils.CryptUtils;

import java.util.ArrayList;
import java.util.List;

public class RedisKey {

    private final static String KEY_USER_LABEL_DIMENSION_HOTKEY = "user_data:user_labels_hotkey:%s:%s:%s:%s";
    private final static String KEY_HASH_USER_LABELS = "user_data:user_labels:%s:%s";
    private final static String KEY_USER_LABEL_DIMENSIONS_INVALID = "user_data:user_label_dimensions:%s:%s";
    private final static String KEY_USER_DMP = "user_data:user_dmp:%s";
    private final static String KEY_AB_EXPERIMENT_NODES = "user_segment:ab_experiment_nodes:%s";
    private final static String KEY_LOCK_USER_PREFIX = "user_data:user_lock:";
    private final static String KEY_LOCK_USER_SYNC_PREFIX = "user_data:user_sync_lock:";
    private final static String KEY_USER_LRU = "user_data:user_lru:%s";

    public static String getUserLabelDimensionHotRedisKey(Long userId, String labelCode, Long labelDimensionKeyId, String labelDimensionKey) {
        return String.format(KEY_USER_LABEL_DIMENSION_HOTKEY, userId, labelCode, labelDimensionKeyId, CryptUtils.encrypt(labelDimensionKey, labelCode));
    }

    public static String getUserLabelsRedisHashKey(Long userId) {
        return String.format(KEY_HASH_USER_LABELS, userId % 100000 / 10000, userId % 20000);
    }

    public static List<String> getAllUserLabelsRedisHashKey(int start, int end) {
        List<String> result = new ArrayList<>();
        for (int i=0; i<20000; i++) {
            if (i >= start && i<= end) {
                for (int j=0; j<10; j++) {
                    result.add(String.format(KEY_HASH_USER_LABELS, j, i));
                }
            }
        }
        return result;
    }

    public static String getUserLabelDimensionsInvalidRedisKey(Long userId, String labelCode) {
        return String.format(KEY_USER_LABEL_DIMENSIONS_INVALID, labelCode, userId % 1000);
    }

    public static List<String> getAllUserLabelDimensionInvalidRedisKey(String labelCode) {
        List<String> result = new ArrayList<>();
        for (int i=0; i<1000; i++) {
            result.add(String.format(KEY_USER_LABEL_DIMENSIONS_INVALID, labelCode, i));
        }
        return result;
    }

    public static String getUserDmpRedisKey(Long userId) {
        return String.format(KEY_USER_DMP, userId);
    }

    public static String getAbExperimentNodesRedisKey(String abExperimentGroupId) {
        return String.format(KEY_AB_EXPERIMENT_NODES, abExperimentGroupId);
    }

    public static String getLockUserRedisKey(Long userId) {
        return KEY_LOCK_USER_PREFIX + userId;
    }

    public static String getLockUserSyncRedisKey(Long userId) {
        return KEY_LOCK_USER_SYNC_PREFIX + userId;
    }

    public static String getUserLruRedisKey(Long userId) {
        return String.format(KEY_USER_LRU, userId);
    }
}
