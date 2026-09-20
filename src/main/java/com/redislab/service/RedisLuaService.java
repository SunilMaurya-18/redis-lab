package com.redislab.service;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RedisLuaService {

    private final StringRedisTemplate redisTemplate;

    private final RedisScript<Long> incrementScript;

    private final RedisScript<Long> checkAndSetScript;

    public RedisLuaService(
            StringRedisTemplate redisTemplate,

            @Qualifier("incrementScript")
            RedisScript<Long> incrementScript,

            @Qualifier("checkAndSetScript")
            RedisScript<Long> checkAndSetScript) {

        this.redisTemplate = redisTemplate;
        this.incrementScript = incrementScript;
        this.checkAndSetScript = checkAndSetScript;
    }

    public Long increment(
            String key,
            long amount) {

        validateKey(key);

        return redisTemplate.execute(
                incrementScript,
                List.of(key),
                String.valueOf(amount)
        );
    }

    public boolean checkAndSet(
            String key,
            String expectedValue,
            String newValue) {

        validateKey(key);
        validateValue(expectedValue);
        validateValue(newValue);

        Long result = redisTemplate.execute(
                checkAndSetScript,
                List.of(key),
                expectedValue,
                newValue
        );

        return result != null && result == 1L;
    }

    public String echo(
            String message) {

        validateValue(message);

        RedisScript<String> script =
                RedisScript.of(
                        "return ARGV[1]",
                        String.class
                );

        return redisTemplate.execute(
                script,
                List.of(),
                message
        );
    }

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