package com.redislab.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.concurrent.TimeUnit;

@Service
public class RedisExpirationService {
    private final StringRedisTemplate redisTemplate;
    private final ValueOperations<String, String> valueOperations;

    public RedisExpirationService(StringRedisTemplate redisTemplate, ValueOperations<String, String> valueOperations) {
        this.redisTemplate = redisTemplate;
        this.valueOperations = redisTemplate.opsForValue();
    }

    public boolean expire(
            String key,
            long seconds
    ) {
        validateKey(key);
        validatePositive(seconds, "Seconds");

        Boolean result = redisTemplate.expire(key, Duration.ofSeconds(seconds));
        return Boolean.TRUE.equals(result);
    }

    public boolean pExpire(
            String key,
            long milliseconds
    ) {
        validateKey(key);
        validatePositive(milliseconds, "Milliseconds");

        Boolean result = redisTemplate.expire(key, Duration.ofMillis(milliseconds));
        return Boolean.TRUE.equals(result);
    }

    public Long ttl(String key) {
        validateKey(key);
        return redisTemplate.getExpire(
                key, TimeUnit.SECONDS
        );
    }

    public Long pTtl(String key) {
        validateKey(key);
        return redisTemplate.getExpire(
                key, TimeUnit.MILLISECONDS);

    }

    public boolean expireAt(
            String key,
            long unixSeconds
    ) {
        validateKey(key);
        Boolean result = redisTemplate.expireAt(
                key,
                Instant.ofEpochSecond(unixSeconds)

        );
        return Boolean.TRUE.equals(result);
    }

    public boolean pExpireAt(
            String key,
            long unixMilliSeconds
    ) {

        validateKey(key);
        Boolean result = redisTemplate.expireAt(
                key,
                Instant.ofEpochMilli(unixMilliSeconds));
        return Boolean.TRUE.equals(result);
    }

    public boolean persist(String key) {
        validateKey(key);
        Boolean result = redisTemplate.persist(key);
        return Boolean.TRUE.equals(result);
    }

    public void setWithExpiration(
            String key,
            String value,
            long seconds
    ) {
        validateKey(key);
        validateValue(value);
        validatePositive(seconds, "Seconds");
        valueOperations.set(key, value, Duration.ofSeconds(seconds));
    }

    public void setWithMillisecondExpiration(
            String key,
            String value,
            long milliseconds
    ) {
        validateKey(key);
        validateValue(value);
        validatePositive(milliseconds, "Milliseconds");
        valueOperations.set(key, value, Duration.ofMillis(milliseconds));
    }

    public boolean setIfAbsentWithExpiration(
            String key,
            String value,
            long seconds) {

        validateKey(key);
        validateValue(value);
        validatePositive(seconds, "Seconds");

        Boolean result =
                valueOperations.setIfAbsent(
                        key,
                        value,
                        Duration.ofSeconds(seconds)
                );

        return Boolean.TRUE.equals(result);
    }

    // ---------------------------------------------------------
    // SET NX PX
    // ---------------------------------------------------------

    public boolean setIfAbsentWithMillisecondExpiration(
            String key,
            String value,
            long milliseconds) {

        validateKey(key);
        validateValue(value);
        validatePositive(milliseconds, "Milliseconds");

        Boolean result =
                valueOperations.setIfAbsent(
                        key,
                        value,
                        Duration.ofMillis(milliseconds)
                );

        return Boolean.TRUE.equals(result);
    }

    // ---------------------------------------------------------
    // Validation
    // ---------------------------------------------------------

    private void validateKey(String key) {

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "Redis key must not be blank"
            );
        }
    }

    private void validateValue(String value) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Redis value must not be blank"
            );
        }
    }

    private void validatePositive(
            long value,
            String name) {

        if (value <= 0) {
            throw new IllegalArgumentException(
                    name + " must be greater than zero"
            );
        }
    }
}


