package com.hn.onelabel.server.infrastructure.cache;

import com.alibaba.fastjson.JSON;
import com.hn.onelabel.server.infrastructure.nacos.LogSwitchConfigLoader;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisCacheService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Autowired
    private LogSwitchConfigLoader logSwitchConfigLoader;

    public Boolean exists(String key) {
        if (logSwitchConfigLoader.redisCacheServicePrintLogSwitchIsOpen()) {
            log.info("[redis] - exists, key:{}.", key);
        }
        try {
            return redisTemplate.hasKey(key);
        } catch (Exception e) {
            log.error("redisTemplate exists failure.", e);
        }
        return false;
    }

    public String get(String key) {
        if (logSwitchConfigLoader.redisCacheServicePrintLogSwitchIsOpen()) {
            log.info("[redis] - get, key:{}.", key);
        }
        try {
            return redisTemplate.opsForValue().get(key);
        } catch (Exception e) {
            log.error("redisTemplate get failure.", e);
        }
        return null;
    }

    public void set(String key, String val, long l, TimeUnit timeUnit) {
        if (logSwitchConfigLoader.redisCacheServicePrintLogSwitchIsOpen()) {
            log.info("[redis] - set, key:{}, val: {}, long: {}, timeUnit: {}.", key, val, l, timeUnit);
        }
        try {
            redisTemplate.opsForValue().set(key, val, l, timeUnit);
        } catch (Exception e) {
            log.error("redisTemplate set failure.", e);
        }
    }

    public Boolean setNx(String key, String val, long l, TimeUnit timeUnit) {
        if (logSwitchConfigLoader.redisCacheServicePrintLogSwitchIsOpen()) {
            log.info("[redis] - setNx, key:{}, val: {}, long: {}, timeUnit: {}.", key, val, l, timeUnit);
        }
        try {
            return redisTemplate.opsForValue().setIfAbsent(key, val, l, timeUnit);
        } catch (Exception e) {
            log.error("redisTemplate setNx failure.", e);
        }
        return false;
    }

    public Boolean delete(String key) {
        if (logSwitchConfigLoader.redisCacheServicePrintLogSwitchIsOpen()) {
            log.info("[redis] - delete, key:{}.", key);
        }
        try {
            return redisTemplate.delete(key);
        } catch (Exception e) {
            log.error("redisTemplate delete failure.", e);
        }
        return false;
    }

    public void hSet(String key, String field, String val) {
        if (logSwitchConfigLoader.redisCacheServicePrintLogSwitchIsOpen()) {
            log.info("[redis] - hset, key:{}, field: {}, val: {}.", key, field, val);
        }
        try {
            redisTemplate.opsForHash().put(key, field, val);
        } catch (Exception e) {
            log.error("redisTemplate hSet failure.", e);
        }
    }

    public String hGet(String key, String field) {
        if (logSwitchConfigLoader.redisCacheServicePrintLogSwitchIsOpen()) {
            log.info("[redis] - hget, key:{}, field: {}.", key, field);
        }
        try {
            return (String) redisTemplate.opsForHash().get(key, field);
        } catch (Exception e) {
            log.error("redisTemplate hGet failure.", e);
        }
        return null;
    }

    public List<Object> hVals(String key) {
        if (logSwitchConfigLoader.redisCacheServicePrintLogSwitchIsOpen()) {
            log.info("[redis] - hvals, key:{}.", key);
        }
        try {
            return redisTemplate.opsForHash().values(key);
        } catch (Exception e) {
            log.error("redisTemplate hVals failure.", e);
        }
        return null;
    }

    public void hDel(String key, String field) {
        if (logSwitchConfigLoader.redisCacheServicePrintLogSwitchIsOpen()) {
            log.info("[redis] - hdel, key:{}, field: {}.", key, field);
        }
        try {
            redisTemplate.opsForHash().delete(key, field);
        } catch (Exception e) {
            log.error("redisTemplate hDel failure.", e);
        }
    }

    public void zAdd(String key, String val, double score) {
        if (logSwitchConfigLoader.redisCacheServicePrintLogSwitchIsOpen()) {
            log.info("[redis] - zadd, key: {}, val: {}, score: {}.", key, val, score);
        }
        try {
            redisTemplate.opsForZSet().add(key, val, score);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("redisTemplate zAdd failure.", e);
        }
    }

    public void zRem(String key, String... val) {
        if (logSwitchConfigLoader.redisCacheServicePrintLogSwitchIsOpen()) {
            log.info("[redis] - zrem, key: {}, val: {}.", key, JSON.toJSONString(Arrays.asList(val)));
        }
        try {
            redisTemplate.opsForZSet().remove(key, val);
        } catch (Exception e) {
            log.error("redisTemplate zRem failure.", e);
        }
    }

    public Set<ZSetOperations.TypedTuple<String>> zRevRangeByScoreWithScores(String key, double min, double max, long offset, long count) {
        if (logSwitchConfigLoader.redisCacheServicePrintLogSwitchIsOpen()) {
            log.info("[redis] - zRevRangeByScoreWithScores, key: {}, min: {}, max: {}, offset: {}, count: {}.", key, min, max, offset, count);
        }
        try {
            return redisTemplate.opsForZSet().reverseRangeByScoreWithScores(key, min, max, offset, count);
        } catch (Exception e) {
            log.error("redisTemplate zRevRangeByScoreWithScores failure.", e);
        }
        return new HashSet<>();
    }

}
