package com.redislab.service;

import org.springframework.data.redis.core.SetOperations;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class RedisSetService {

    private final SetOperations<String, String> setOperations;
    private final StringRedisTemplate redisTemplate;

    public RedisSetService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        this.setOperations = redisTemplate.opsForSet();
    }

    public Long add(
            String key,
            String value) {

        validateKey(key);
        validateValue(value);

        return setOperations.add(
                key,
                value
        );
    }

    public Long addMultiple(
            String key,
            String... values) {

        validateKey(key);

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException(
                    "At least one value is required"
            );
        }

        for (String value : values) {
            validateValue(value);
        }

        return setOperations.add(
                key,
                values
        );
    }

    public Long remove(
            String key,
            String... values) {

        validateKey(key);

        if (values == null || values.length == 0) {
            throw new IllegalArgumentException(
                    "At least one value is required"
            );
        }

        for (String value : values) {
            validateValue(value);
        }

        return setOperations.remove(
                key,
                (Object[]) values
        );
    }

    public boolean isMember(
            String key,
            String value) {

        validateKey(key);
        validateValue(value);

        Boolean result =
                setOperations.isMember(
                        key,
                        value
                );

        return Boolean.TRUE.equals(result);
    }

    public Set<String> members(String key) {

        validateKey(key);

        return setOperations.members(key);
    }

    public Long size(String key) {

        validateKey(key);

        Long result =
                setOperations.size(key);

        return result == null ? 0L : result;
    }

    public String pop(String key) {

        validateKey(key);

        return setOperations.pop(key);
    }

    public List<String> pop(
            String key,
            long count) {

        validateKey(key);

        if (count <= 0) {
            throw new IllegalArgumentException(
                    "Count must be greater than zero"
            );
        }

        return setOperations.pop(
                key,
                count
        );
    }

    public String randomMember(String key) {

        validateKey(key);

        return setOperations.randomMember(key);
    }

    public List<String> randomMembers(
            String key,
            long count) {

        validateKey(key);

        if (count <= 0) {
            throw new IllegalArgumentException(
                    "Count must be greater than zero"
            );
        }

        return setOperations.randomMembers(
                key,
                count
        );
    }

    public boolean move(
            String source,
            String destination,
            String value) {

        validateKey(source);
        validateKey(destination);
        validateValue(value);

        Boolean result =
                setOperations.move(
                        source,
                        value,
                        destination
                );

        return Boolean.TRUE.equals(result);
    }

    public Set<String> intersect(
            String key1,
            String key2) {

        validateKey(key1);
        validateKey(key2);

        return setOperations.intersect(
                key1,
                key2
        );
    }

    public Set<String> union(
            String key1,
            String key2) {

        validateKey(key1);
        validateKey(key2);

        return setOperations.union(
                key1,
                key2
        );
    }

    public Set<String> difference(
            String key1,
            String key2) {

        validateKey(key1);
        validateKey(key2);

        return setOperations.difference(
                key1,
                key2
        );
    }

    public boolean deleteSet(String key) {

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

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    "Set value must not be blank"
            );
        }
    }
}