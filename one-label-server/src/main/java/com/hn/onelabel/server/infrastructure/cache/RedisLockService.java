package com.hn.onelabel.server.infrastructure.cache;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class RedisLockService {

    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    public boolean tryLock(String lockKey, String lockOwner, long timeoutSeconds) {
        Objects.requireNonNull(lockKey, "Null lockKey.");
        try {
            return Boolean.TRUE.equals(redisTemplate.opsForValue().setIfAbsent(lockKey, lockOwner, timeoutSeconds, TimeUnit.SECONDS));
        } catch (Exception e) {
            log.info("[RedisLockService] - try lock failure, k: {}, v: {}, timeout: {}.", lockKey, lockOwner, timeoutSeconds, e);
        }
        return false;
    }

    public void releaseLock(String lockKey, String lockOwner) {
        Objects.requireNonNull(lockKey, "Null lockKey.");
        Objects.requireNonNull(lockOwner, "Null lockOwner.");
        String currentLockOwner = redisTemplate.opsForValue().get(lockKey);
        if (StringUtils.isEmpty(currentLockOwner) || currentLockOwner.equals(lockOwner)) {
            redisTemplate.delete(lockKey);
        } else {
            log.info("[RedisLockService] - release lock failure, lockOwner: {}, currentLockOwner: {}", lockOwner, currentLockOwner);
        }
    }

}
