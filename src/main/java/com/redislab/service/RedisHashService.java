package com.redislab.service;

import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.Objects;

@Service
public class RedisHashService {

    private final HashOperations<String, String, String> hashOperations;
    private final StringRedisTemplate redisTemplate;

    public RedisHashService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.hashOperations = redisTemplate.opsForHash();
    }

    // --------------------------------------------------
    // HSET
    // --------------------------------------------------

    public boolean setField(
            String key,
            String field,
            String value) {

        validateKey(key);
        validateField(field);
        validateValue(value);

        Long result =
                hashOperations.putIfAbsent(key, field, value)
                        ? 1L
                        : 0L;

        return result == 1L;
    }

    // --------------------------------------------------
    // HSET / Update Field
    // --------------------------------------------------

    public void set(
            String key,
            String field,
            String value) {

        validateKey(key);
        validateField(field);
        validateValue(value);

        hashOperations.put(key, field, value);
    }

    // --------------------------------------------------
    // HSET Multiple
    // --------------------------------------------------

    public void setMultiple(
            String key,
            Map<String, String> fields) {

        validateKey(key);

        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one field is required"
            );
        }

        for (Map.Entry<String, String> entry : fields.entrySet()) {
            validateField(entry.getKey());
            validateValue(entry.getValue());
        }

        hashOperations.putAll(key, fields);
    }

    // --------------------------------------------------
    // HGET
    // --------------------------------------------------

    public String get(
            String key,
            String field) {

        validateKey(key);
        validateField(field);

        return hashOperations.get(key, field);
    }

    // --------------------------------------------------
    // HMGET
    // --------------------------------------------------

    public List<String> getMultiple(
            String key,
            List<String> fields) {

        validateKey(key);

        if (fields == null || fields.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one field is required"
            );
        }

        fields.forEach(this::validateField);

        return hashOperations.multiGet(key, fields);
    }

    // --------------------------------------------------
    // HGETALL
    // --------------------------------------------------

    public Map<String, String> getAll(String key) {

        validateKey(key);

        return hashOperations.entries(key);
    }

    // --------------------------------------------------
    // HEXISTS
    // --------------------------------------------------

    public boolean fieldExists(
            String key,
            String field) {

        validateKey(key);
        validateField(field);

        return hashOperations.hasKey(key, field);
    }

    // --------------------------------------------------
    // HDEL
    // --------------------------------------------------

    public long deleteFields(
            String key,
            String... fields) {

        validateKey(key);

        if (fields == null || fields.length == 0) {
            throw new IllegalArgumentException(
                    "At least one field is required"
            );
        }

        for (String field : fields) {
            validateField(field);
        }

        Long result =
                hashOperations.delete(key, (Object[]) fields);

        return Objects.requireNonNullElse(result, 0L);
    }

    // --------------------------------------------------
    // HLEN
    // --------------------------------------------------

    public long length(String key) {

        validateKey(key);

        Long result = hashOperations.size(key);

        return Objects.requireNonNullElse(result, 0L);
    }

    // --------------------------------------------------
    // HKEYS
    // --------------------------------------------------

    public java.util.Set<String> keys(String key) {

        validateKey(key);

        return hashOperations.keys(key);
    }

    // --------------------------------------------------
    // HVALS
    // --------------------------------------------------

    public List<String> values(String key) {

        validateKey(key);

        return hashOperations.values(key);
    }

    // --------------------------------------------------
    // HINCRBY
    // --------------------------------------------------

    public Long incrementBy(
            String key,
            String field,
            long amount) {

        validateKey(key);
        validateField(field);

        return hashOperations.increment(
                key,
                field,
                amount
        );
    }

    // --------------------------------------------------
    // HINCRBYFLOAT
    // --------------------------------------------------

    public Double incrementByFloat(
            String key,
            String field,
            double amount) {

        validateKey(key);
        validateField(field);

        return hashOperations.increment(
                key,
                field,
                amount
        );
    }

    // --------------------------------------------------
    // HSETNX
    // --------------------------------------------------

    public boolean setIfAbsent(
            String key,
            String field,
            String value) {

        validateKey(key);
        validateField(field);
        validateValue(value);

        return hashOperations.putIfAbsent(
                key,
                field,
                value
        );
    }

    // --------------------------------------------------
    // DELETE ENTIRE HASH
    // --------------------------------------------------

    public boolean deleteHash(String key) {

        validateKey(key);

        Boolean result = redisTemplate.delete(key);

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

    private void validateField(String field) {

        if (field == null || field.isBlank()) {
            throw new IllegalArgumentException(
                    "Hash field must not be blank"
            );
        }
    }

    private void validateValue(String value) {

        if (value == null) {
            throw new IllegalArgumentException(
                    "Hash value must not be null"
            );
        }
    }
}