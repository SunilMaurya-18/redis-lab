package com.redislab;

import com.redislab.service.RedisBenchmarkService;
import com.redislab.service.RedisDiagnosticsService;
import com.redislab.service.RedisDistributedLockService;
import com.redislab.service.RedisRateLimitService;
import org.junit.jupiter.api.Test;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.junit.jupiter.api.Assertions.assertThrows;

class RedisAdvancedValidationTests {

    @Test
    void lockRejectsNonPositiveTtlBeforeRedisAccess() {
        RedisDistributedLockService service = new RedisDistributedLockService(new StringRedisTemplate());
        assertThrows(IllegalArgumentException.class, () -> service.tryLock("lock", "owner", 0));
    }

    @Test
    void rateLimiterRejectsInvalidWindowBeforeRedisAccess() {
        RedisRateLimitService service = new RedisRateLimitService(new StringRedisTemplate());
        assertThrows(IllegalArgumentException.class, () -> service.atomicFixedWindow("user:1", 10, 0));
    }

    @Test
    void diagnosticsRejectsInvalidScanCountBeforeRedisAccess() {
        RedisDiagnosticsService service = new RedisDiagnosticsService(new StringRedisTemplate());
        assertThrows(IllegalArgumentException.class, () -> service.scan("*", 0));
    }

    @Test
    void benchmarkRejectsInvalidMeasuredIterationsBeforeRedisAccess() {
        RedisBenchmarkService service = new RedisBenchmarkService(new StringRedisTemplate());
        assertThrows(IllegalArgumentException.class, () -> service.run("bench", 10, 0, 10));
    }
}
