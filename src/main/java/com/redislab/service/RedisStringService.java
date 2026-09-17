package com.redislab.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Service
public class RedisStringService {

    private final StringRedisTemplate redisTemplate;

    public RedisStringService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // --------------------------------------------------
    // SET
    // --------------------------------------------------

    public void set(String key, String value) {
        validateKey(key);
        validateValue(value);

        redisTemplate.opsForValue().set(key, value);
    }

    // --------------------------------------------------
    // GET
    // --------------------------------------------------

    public String get(String key) {
        validateKey(key);

        return redisTemplate.opsForValue().get(key);
    }

    // --------------------------------------------------
    // SET NX
    // --------------------------------------------------

    public boolean setIfAbsent(String key, String value) {
        validateKey(key);
        validateValue(value);

        Boolean result =
                redisTemplate.opsForValue().setIfAbsent(key, value);

        return Boolean.TRUE.equals(result);
    }

    // --------------------------------------------------
    // SET XX
    // --------------------------------------------------

    public boolean setIfPresent(String key, String value) {
        validateKey(key);
        validateValue(value);

        Boolean result =
                redisTemplate.opsForValue().setIfPresent(key, value);

        return Boolean.TRUE.equals(result);
    }

    // --------------------------------------------------
    // MSET
    // --------------------------------------------------

    public void setMultiple(List<String> keys, List<String> values) {

        if (keys == null || values == null) {
            throw new IllegalArgumentException("Keys and values are required");
        }

        if (keys.size() != values.size()) {
            throw new IllegalArgumentException(
                    "Number of keys must match number of values"
            );
        }

        if (keys.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one key-value pair is required"
            );
        }

        java.util.Map<String, String> data =
                new java.util.LinkedHashMap<>();

        for (int i = 0; i < keys.size(); i++) {

            validateKey(keys.get(i));
            validateValue(values.get(i));

            data.put(keys.get(i), values.get(i));
        }

        redisTemplate.opsForValue().multiSet(data);
    }

    // --------------------------------------------------
    // MGET
    // --------------------------------------------------

    public List<String> getMultiple(List<String> keys) {

        if (keys == null || keys.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one key is required"
            );
        }

        keys.forEach(this::validateKey);

        return redisTemplate.opsForValue().multiGet(keys);
    }

    // --------------------------------------------------
    // INCR
    // --------------------------------------------------

    public Long increment(String key) {
        validateKey(key);

        return redisTemplate.opsForValue().increment(key);
    }

    // --------------------------------------------------
    // DECR
    // --------------------------------------------------

    public Long decrement(String key) {
        validateKey(key);

        return redisTemplate.opsForValue().decrement(key);
    }

    // --------------------------------------------------
    // INCRBY
    // --------------------------------------------------

    public Long incrementBy(String key, long amount) {
        validateKey(key);

        return redisTemplate.opsForValue().increment(key, amount);
    }

    // --------------------------------------------------
    // DECRBY
    // --------------------------------------------------

    public Long decrementBy(String key, long amount) {
        validateKey(key);

        return redisTemplate.opsForValue().increment(key, -amount);
    }

    // --------------------------------------------------
    // APPEND
    // --------------------------------------------------

    public Integer append(String key, String value) {
        validateKey(key);
        validateValue(value);

        return redisTemplate.opsForValue().append(key, value);
    }

    // --------------------------------------------------
    // STRLEN
    // --------------------------------------------------

    public Long length(String key) {
        validateKey(key);

        return redisTemplate.opsForValue().size(key);
    }

    // --------------------------------------------------
    // GETSET
    // --------------------------------------------------

    public String getAndSet(String key, String value) {
        validateKey(key);
        validateValue(value);

        return redisTemplate.opsForValue().getAndSet(key, value);
    }

    // --------------------------------------------------
    // GET DEL
    // --------------------------------------------------

    public String getAndDelete(String key) {
        validateKey(key);

        return redisTemplate.opsForValue().getAndDelete(key);
    }

    // --------------------------------------------------
    // EXISTS
    // --------------------------------------------------

    public boolean exists(String key) {
        validateKey(key);

        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    // --------------------------------------------------
    // DELETE
    // --------------------------------------------------

    public boolean delete(String key) {
        validateKey(key);

        Boolean result = redisTemplate.delete(key);

        return Boolean.TRUE.equals(result);
    }

    // --------------------------------------------------
    // SET WITH TTL
    // --------------------------------------------------

    public void setWithTtl(String key, String value, long seconds) {

        validateKey(key);
        validateValue(value);

        if (seconds <= 0) {
            throw new IllegalArgumentException(
                    "TTL must be greater than zero"
            );
        }

        redisTemplate.opsForValue()
                .set(key, value, Duration.ofSeconds(seconds));
    }

    // --------------------------------------------------
    // TTL
    // --------------------------------------------------

    public Long getTtl(String key) {
        validateKey(key);

        return redisTemplate.getExpire(key);
    }

    // --------------------------------------------------
    // EXPIRE
    // --------------------------------------------------

    public boolean expire(String key, long seconds) {

        validateKey(key);

        if (seconds <= 0) {
            throw new IllegalArgumentException(
                    "Expiration must be greater than zero"
            );
        }

        Boolean result =
                redisTemplate.expire(
                        key,
                        Duration.ofSeconds(seconds)
                );

        return Boolean.TRUE.equals(result);
    }

    // --------------------------------------------------
    // PERSIST
    // --------------------------------------------------

    public boolean persist(String key) {

        validateKey(key);

        Boolean result = redisTemplate.persist(key);

        return Boolean.TRUE.equals(result);
    }

    // --------------------------------------------------
    // Validation
    // --------------------------------------------------

    private void validateKey(String key) {

        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException(
                    "Redis key must not be blank"
            );
        }
    }

    private void validateValue(String value) {

        if (value == null) {
            throw new IllegalArgumentException(
                    "Redis value must not be null"
            );
        }
    }
}