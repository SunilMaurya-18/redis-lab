package com.redislab.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.stereotype.Service;

import java.util.Set;

@Service
public class RedisSortedSetService {

    private final ZSetOperations<String, String> zSetOperations;
    private final StringRedisTemplate redisTemplate;

    public RedisSortedSetService(
            StringRedisTemplate redisTemplate) {

        this.redisTemplate = redisTemplate;
        this.zSetOperations = redisTemplate.opsForZSet();
    }

    public boolean add(
            String key,
            String member,
            double score) {

        validateKey(key);
        validateMember(member);

        Boolean result =
                zSetOperations.add(
                        key,
                        member,
                        score
                );

        return Boolean.TRUE.equals(result);
    }

    public long remove(
            String key,
            String... members) {

        validateKey(key);

        if (members == null || members.length == 0) {
            throw new IllegalArgumentException(
                    "At least one member is required"
            );
        }

        for (String member : members) {
            validateMember(member);
        }

        Long result =
                zSetOperations.remove(
                        key,
                        (Object[]) members
                );

        return result == null ? 0L : result;
    }

    public Double score(
            String key,
            String member) {

        validateKey(key);
        validateMember(member);

        return zSetOperations.score(
                key,
                member
        );
    }

    public Long rank(
            String key,
            String member) {

        validateKey(key);
        validateMember(member);

        return zSetOperations.rank(
                key,
                member
        );
    }

    public Long reverseRank(
            String key,
            String member) {

        validateKey(key);
        validateMember(member);

        return zSetOperations.reverseRank(
                key,
                member
        );
    }

    public Set<String> range(
            String key,
            long start,
            long end) {

        validateKey(key);

        return zSetOperations.range(
                key,
                start,
                end
        );
    }

    public Set<String> reverseRange(
            String key,
            long start,
            long end) {

        validateKey(key);

        return zSetOperations.reverseRange(
                key,
                start,
                end
        );
    }

    public Set<ZSetOperations.TypedTuple<String>>
    rangeWithScores(
            String key,
            long start,
            long end) {

        validateKey(key);

        return zSetOperations.rangeWithScores(
                key,
                start,
                end
        );
    }

    public Set<String> rangeByScore(
            String key,
            double min,
            double max) {

        validateKey(key);

        return zSetOperations.rangeByScore(
                key,
                min,
                max
        );
    }

    public Set<String> reverseRangeByScore(
            String key,
            double min,
            double max) {

        validateKey(key);

        return zSetOperations.reverseRangeByScore(
                key,
                min,
                max
        );
    }

    public Long countByScore(
            String key,
            double min,
            double max) {

        validateKey(key);

        Long result =
                zSetOperations.count(
                        key,
                        min,
                        max
                );

        return result == null ? 0L : result;
    }

    public Double incrementScore(
            String key,
            String member,
            double delta) {

        validateKey(key);
        validateMember(member);

        return zSetOperations.incrementScore(
                key,
                member,
                delta
        );
    }

    public Long size(String key) {

        validateKey(key);

        Long result =
                zSetOperations.size(key);

        return result == null ? 0L : result;
    }

    public boolean deleteSortedSet(String key) {

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

    private void validateMember(String member) {

        if (member == null || member.isBlank()) {
            throw new IllegalArgumentException(
                    "Sorted set member must not be blank"
            );
        }
    }
}