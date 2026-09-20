package com.redislab.service;

import com.redislab.model.RateLimitResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class RedisRateLimitService {

    private static final RedisScript<Long> ATOMIC_FIXED_WINDOW = RedisScript.of(
            "local current = redis.call('incr', KEYS[1]) " +
                    "if current == 1 then redis.call('pexpire', KEYS[1], ARGV[1]) end " +
                    "return current",
            Long.class
    );

    private final StringRedisTemplate redisTemplate;

    public RedisRateLimitService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Intentionally non-atomic teaching implementation. INCR and EXPIRE are
     * separate commands, so a process failure between them can leave a key
     * without a TTL.
     */
    public RateLimitResponse simpleFixedWindow(
            String scope,
            long limit,
            long windowMs) {

        validate(scope, limit, windowMs);
        String key = bucketKey(scope, windowMs);
        Long current = redisTemplate.opsForValue().increment(key);
        if (Long.valueOf(1L).equals(current)) {
            redisTemplate.expire(key, Duration.ofMillis(windowMs));
        }
        return response(key, limit, current == null ? 0L : current);
    }

    public RateLimitResponse atomicFixedWindow(
            String scope,
            long limit,
            long windowMs) {

        validate(scope, limit, windowMs);
        String key = bucketKey(scope, windowMs);
        Long current = redisTemplate.execute(
                ATOMIC_FIXED_WINDOW,
                List.of(key),
                Long.toString(windowMs)
        );
        return response(key, limit, current == null ? 0L : current);
    }

    public long ttlSeconds(String scope, long windowMs) {
        validate(scope, 1, windowMs);
        return redisTemplate.getExpire(
                bucketKey(scope, windowMs),
                TimeUnit.SECONDS
        );
    }

    private RateLimitResponse response(String key, long limit, long current) {
        long remaining = Math.max(0L, limit - current);
        Long ttl = redisTemplate.getExpire(key, TimeUnit.MILLISECONDS);
        long resetAfterSeconds = ttl == null || ttl < 0
                ? 0L
                : Math.max(1L, (ttl + 999L) / 1000L);
        return new RateLimitResponse(
                key,
                current <= limit,
                limit,
                current,
                remaining,
                resetAfterSeconds
        );
    }

    private String bucketKey(String scope, long windowMs) {
        long bucket = System.currentTimeMillis() / windowMs;
        return "redislab:ratelimit:" + scope + ":" + windowMs + ":" + bucket;
    }

    private void validate(String scope, long limit, long windowMs) {
        if (scope == null || scope.isBlank()) {
            throw new IllegalArgumentException("Rate-limit scope must not be blank");
        }
        if (limit <= 0 || limit > 1_000_000) {
            throw new IllegalArgumentException("Limit must be between 1 and 1000000");
        }
        if (windowMs <= 0 || windowMs > 86_400_000) {
            throw new IllegalArgumentException("Window must be between 1 and 86400000 milliseconds");
        }
    }
}
