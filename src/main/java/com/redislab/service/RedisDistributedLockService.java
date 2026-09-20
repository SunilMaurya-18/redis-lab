package com.redislab.service;

import com.redislab.model.LockResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.UUID;


@Service
public class RedisDistributedLockService {

    private static final RedisScript<Long> RELEASE_SCRIPT = RedisScript.of(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('del', KEYS[1]) else return 0 end",
            Long.class
    );

    private static final RedisScript<Long> RENEW_SCRIPT = RedisScript.of(
            "if redis.call('get', KEYS[1]) == ARGV[1] then " +
                    "return redis.call('pexpire', KEYS[1], ARGV[2]) else return 0 end",
            Long.class
    );

    private final StringRedisTemplate redisTemplate;

    public RedisDistributedLockService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public LockResponse tryLock(String key, String owner, long ttlMs) {
        validate(key, "Lock key");
        validate(owner, "Owner");
        validateTtl(ttlMs);

        String token = owner + ":" + UUID.randomUUID();
        Boolean acquired = redisTemplate.opsForValue().setIfAbsent(
                key,
                token,
                Duration.ofMillis(ttlMs)
        );

        return new LockResponse(
                key,
                Boolean.TRUE.equals(acquired),
                Boolean.TRUE.equals(acquired) ? token : null,
                Boolean.TRUE.equals(acquired) ? ttlMs : redisTemplate.getExpire(key, java.util.concurrent.TimeUnit.MILLISECONDS)
        );
    }

    public boolean release(String key, String token) {
        validate(key, "Lock key");
        validate(token, "Lock token");
        Long result = redisTemplate.execute(RELEASE_SCRIPT, List.of(key), token);
        return Long.valueOf(1L).equals(result);
    }

    public boolean renew(String key, String token, long ttlMs) {
        validate(key, "Lock key");
        validate(token, "Lock token");
        validateTtl(ttlMs);
        Long result = redisTemplate.execute(
                RENEW_SCRIPT,
                List.of(key),
                token,
                Long.toString(ttlMs)
        );
        return Long.valueOf(1L).equals(result);
    }

    public Long ttl(String key) {
        validate(key, "Lock key");
        return redisTemplate.getExpire(key, java.util.concurrent.TimeUnit.MILLISECONDS);
    }

    private void validate(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
    }

    private void validateTtl(long ttlMs) {
        if (ttlMs <= 0 || ttlMs > 86_400_000) {
            throw new IllegalArgumentException("TTL must be between 1 and 86400000 milliseconds");
        }
    }
}
