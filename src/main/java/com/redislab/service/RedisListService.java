package com.redislab.service;

import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Service
public class RedisListService {

    private final ListOperations<String, String> listOperations;
    private final StringRedisTemplate redisTemplate;

    public RedisListService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.listOperations = redisTemplate.opsForList();
    }

    public Long pushLeft(
            String key,
            String value) {

        validateKey(key);
        validateValue(value);

        return listOperations.leftPush(key, value);
    }

    public Long pushRight(
            String key,
            String value) {

        validateKey(key);
        validateValue(value);

        return listOperations.rightPush(key, value);
    }

    public Long pushLeftMultiple(
            String key,
            List<String> values) {

        validateKey(key);
        validateValues(values);

        return listOperations.leftPushAll(
                key,
                values
        );
    }

    public Long pushRightMultiple(
            String key,
            List<String> values) {

        validateKey(key);
        validateValues(values);

        return listOperations.rightPushAll(
                key,
                values
        );
    }

    public String popLeft(String key) {

        validateKey(key);

        return listOperations.leftPop(key);
    }

    public String popRight(String key) {

        validateKey(key);

        return listOperations.rightPop(key);
    }

    public List<String> range(
            String key,
            long start,
            long end) {

        validateKey(key);

        return listOperations.range(
                key,
                start,
                end
        );
    }

    public Long length(String key) {

        validateKey(key);

        Long result = listOperations.size(key);

        return Objects.requireNonNullElse(
                result,
                0L
        );
    }

    public String index(
            String key,
            long index) {

        validateKey(key);

        return listOperations.index(
                key,
                index
        );
    }

    public void set(
            String key,
            long index,
            String value) {

        validateKey(key);
        validateValue(value);

        listOperations.set(
                key,
                index,
                value
        );
    }

    public Long remove(
            String key,
            long count,
            String value) {

        validateKey(key);
        validateValue(value);

        return listOperations.remove(
                key,
                count,
                value
        );
    }

    public void trim(
            String key,
            long start,
            long end) {

        validateKey(key);

        listOperations.trim(
                key,
                start,
                end
        );
    }

    /*
     * Blocking left pop.
     *
     * Redis BLPOP returns:
     *   value → when an element becomes available
     *   null  → when timeout expires
     */
    public String blockingPopLeft(
            String key,
            long timeoutSeconds) {

        validateKey(key);

        if (timeoutSeconds < 0) {
            throw new IllegalArgumentException(
                    "Timeout cannot be negative"
            );
        }

        return listOperations.leftPop(
                key,
                timeoutSeconds,
                TimeUnit.SECONDS
        );
    }

    /*
     * Blocking right pop.
     *
     * Redis BRPOP returns:
     *   value → when an element becomes available
     *   null  → when timeout expires
     */
    public String blockingPopRight(
            String key,
            long timeoutSeconds) {

        validateKey(key);

        if (timeoutSeconds < 0) {
            throw new IllegalArgumentException(
                    "Timeout cannot be negative"
            );
        }

        return listOperations.rightPop(
                key,
                timeoutSeconds,
                TimeUnit.SECONDS
        );
    }

    public boolean deleteList(String key) {

        validateKey(key);

        Boolean deleted =
                redisTemplate.delete(key);

        return Boolean.TRUE.equals(deleted);
    }

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
                    "List value must not be null"
            );
        }
    }

    private void validateValues(List<String> values) {

        if (values == null || values.isEmpty()) {
            throw new IllegalArgumentException(
                    "At least one value is required"
            );
        }

        for (String value : values) {
            validateValue(value);
        }
    }
}