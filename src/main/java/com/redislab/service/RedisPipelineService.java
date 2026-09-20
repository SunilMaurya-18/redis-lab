package com.redislab.service;

import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class RedisPipelineService {
    private final StringRedisTemplate redisTemplate;
    public RedisPipelineService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    public List<Object> setBatch(
            String prefix,
            int count
    ) {
        validatePrefix(prefix);
        validateCount(count);

        return redisTemplate.executePipelined(
                new RedisCallback<Object>() {
                    @Override
                    public Object doInRedis(
                            RedisConnection connection
                    ) {
                        StringRedisConnection redis = new DefaultStringRedisConnection(connection);
                        for (int i = 1; i <= count; i++) {
                            redis.set(
                                    prefix + ":" + i,
                                    "value-" + i
                            );
                        }
                        return null;
                    }
                }
        );
    }
    public List<Object> getBatch(String prefix, int count) {
        validatePrefix(prefix);
        validateCount(count);
        return redisTemplate.executePipelined(
                new RedisCallback<Object>() {
                    @Override
                    public Object doInRedis(
                            RedisConnection connection
                    ) {
                        StringRedisConnection redis = new DefaultStringRedisConnection(connection);
                        for (int i = 1; i <= count; i++) {
                            redis.get(
                                    prefix + ":" + i
                            );
                        }
                        return null;
                    }
                }
        );
    }
    public List<Object> mixedPipeline(
            String prefix
    ) {
        validatePrefix(prefix);
        return redisTemplate.executePipelined(
                new RedisCallback<Object>() {
                    @Override
                    public Object doInRedis(
                            RedisConnection connection
                    ) {
                        StringRedisConnection redis = new DefaultStringRedisConnection(connection);
                        redis.set(prefix + ":name", "Sunil");
                        redis.set(prefix + ":role", "Java Developer");
                        redis.set(prefix + ":language", "Java");
                        redis.get(prefix + ":name");
                        redis.get(prefix + ":role");
                        redis.get(prefix + ":language");
                        return null;
                    }
                }
        );
    }
    public List<Object> largeBatch(
            String prefix,
            int count) {
        validatePrefix(prefix);
        validateCount(count);

        List<Object> results =
                new ArrayList<>();

        final int batchSize = 1000;

        for (int start = 1;
             start <= count;
             start += batchSize) {

            final int batchStart = start;
            final int batchEnd =
                    Math.min(
                            start + batchSize - 1,
                            count
                    );

            List<Object> batchResults =
                    redisTemplate.executePipelined(
                            new RedisCallback<Object>() {

                                @Override
                                public Object doInRedis(
                                        RedisConnection connection) {

                                    StringRedisConnection redis =
                                            new DefaultStringRedisConnection(
                                                    connection
                                            );

                                    for (int i = batchStart;
                                         i <= batchEnd;
                                         i++) {

                                        redis.set(
                                                prefix + ":" + i,
                                                "value-" + i
                                        );
                                    }

                                    return null;
                                }
                            }
                    );

            results.addAll(batchResults);
        }

        return results;
    }

    // VALIDATION
    private void validatePrefix(
            String prefix) {

        if (prefix == null ||
                prefix.isBlank()) {

            throw new IllegalArgumentException(
                    "Prefix must not be blank"
            );
        }
    }

    private void validateCount(
            int count) {
        if (count <= 0) {

            throw new IllegalArgumentException(
                    "Count must be greater than zero"
            );
        }
        if (count > 100_000) {

            throw new IllegalArgumentException(
                    "Count must not exceed 100000"
            );
        }
    }
}

