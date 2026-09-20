package com.redislab.service;

import org.springframework.data.redis.core.RedisOperations;
import org.springframework.data.redis.core.SessionCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedisTransactionService {

    private final StringRedisTemplate redisTemplate;

    public RedisTransactionService(
            StringRedisTemplate redisTemplate) {

        this.redisTemplate = redisTemplate;
    }


    // MULTI + EXEC


    public List<Object> executeTransaction(
            String key1,
            String value1,
            String key2,
            String value2) {

        validateKey(key1);
        validateKey(key2);
        validateValue(value1);
        validateValue(value2);

        List<Object> results =
                redisTemplate.execute(
                        new SessionCallback<List<Object>>() {

                            @Override
                            @SuppressWarnings("unchecked")
                            public <K, V> List<Object> execute(
                                    RedisOperations<K, V> operations) {

                                RedisOperations<String, String> redis =
                                        (RedisOperations<String, String>) operations;

                                redis.multi();

                                redis.opsForValue()
                                        .set(key1, value1);

                                redis.opsForValue()
                                        .set(key2, value2);

                                return redis.exec();
                            }
                        }
                );

        return results == null
                ? List.of()
                : results;
    }


    // MULTI + DISCARD


    public void discardTransaction(
            String key,
            String value) {

        validateKey(key);
        validateValue(value);

        redisTemplate.execute(
                new SessionCallback<Object>() {

                    @Override
                    @SuppressWarnings("unchecked")
                    public <K, V> Object execute(
                            RedisOperations<K, V> operations) {

                        RedisOperations<String, String> redis =
                                (RedisOperations<String, String>) operations;

                        redis.multi();

                        redis.opsForValue()
                                .set(key, value);

                        redis.discard();

                        return null;
                    }
                }
        );
    }


    // WATCH + MULTI + EXEC


    public List<Object> watchAndSet(
            String key,
            String expectedValue,
            String newValue) {

        validateKey(key);
        validateValue(expectedValue);
        validateValue(newValue);

        List<Object> result =
                redisTemplate.execute(
                        new SessionCallback<List<Object>>() {

                            @Override
                            @SuppressWarnings("unchecked")
                            public <K, V> List<Object> execute(
                                    RedisOperations<K, V> operations) {

                                RedisOperations<String, String> redis =
                                        (RedisOperations<String, String>) operations;

                                // WATCH key
                                redis.watch(key);

                                // READ current value
                                String currentValue =
                                        redis.opsForValue()
                                                .get(key);

                                // Check expected value
                                if (!expectedValue.equals(
                                        currentValue)) {

                                    redis.unwatch();

                                    return List.of(
                                            "WATCH_CONDITION_FAILED"
                                    );
                                }

                                // Start transaction
                                redis.multi();

                                // Queue update
                                redis.opsForValue()
                                        .set(
                                                key,
                                                newValue
                                        );

                                // Execute transaction
                                return redis.exec();
                            }
                        }
                );

        return result == null
                ? List.of()
                : result;
    }


    // WATCH + INCREMENT


    public List<Object> watchAndIncrement(
            String key,
            long amount) {

        validateKey(key);

        List<Object> result =
                redisTemplate.execute(
                        new SessionCallback<List<Object>>() {

                            @Override
                            @SuppressWarnings("unchecked")
                            public <K, V> List<Object> execute(
                                    RedisOperations<K, V> operations) {

                                RedisOperations<String, String> redis =
                                        (RedisOperations<String, String>) operations;

                                // WATCH
                                redis.watch(key);

                                // READ
                                String currentValue =
                                        redis.opsForValue()
                                                .get(key);

                                long current =
                                        currentValue == null
                                                ? 0L
                                                : Long.parseLong(
                                                currentValue
                                        );

                                // CALCULATE
                                long updated =
                                        current + amount;

                                // MULTI
                                redis.multi();

                                // QUEUE SET
                                redis.opsForValue()
                                        .set(
                                                key,
                                                String.valueOf(
                                                        updated
                                                )
                                        );

                                // EXEC
                                return redis.exec();
                            }
                        }
                );

        return result == null
                ? List.of()
                : result;
    }


    // VALIDATION


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
}