package com.redislab.controller;

import com.redislab.service.RedisSortedSetService;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/redis/sorted-sets")
public class RedisSortedSetController {

    private final RedisSortedSetService service;

    public RedisSortedSetController(
            RedisSortedSetService service) {

        this.service = service;
    }

    @PostMapping("/add")
    public Map<String, Object> add(
            @RequestParam String key,
            @RequestParam String member,
            @RequestParam double score) {

        boolean added =
                service.add(
                        key,
                        member,
                        score
                );

        return Map.of(
                "operation", "ZADD",
                "key", key,
                "member", member,
                "score", score,
                "added", added
        );
    }

    @DeleteMapping("/remove")
    public Map<String, Object> remove(
            @RequestParam String key,
            @RequestParam String[] members) {

        long removed =
                service.remove(
                        key,
                        members
                );

        return Map.of(
                "operation", "ZREM",
                "key", key,
                "removed", removed
        );
    }

    @GetMapping("/score")
    public Map<String, Object> score(
            @RequestParam String key,
            @RequestParam String member) {

        Double score =
                service.score(
                        key,
                        member
                );

        return Map.of(
                "operation", "ZSCORE",
                "key", key,
                "member", member,
                "score", score == null ? "" : score
        );
    }

    @GetMapping("/rank")
    public Map<String, Object> rank(
            @RequestParam String key,
            @RequestParam String member) {

        Long rank =
                service.rank(
                        key,
                        member
                );

        return Map.of(
                "operation", "ZRANK",
                "key", key,
                "member", member,
                "rank", rank == null ? -1L : rank
        );
    }

    @GetMapping("/reverse-rank")
    public Map<String, Object> reverseRank(
            @RequestParam String key,
            @RequestParam String member) {

        Long rank =
                service.reverseRank(
                        key,
                        member
                );

        return Map.of(
                "operation", "ZREVRANK",
                "key", key,
                "member", member,
                "rank", rank == null ? -1L : rank
        );
    }

    @GetMapping
    public Map<String, Object> range(
            @RequestParam String key,
            @RequestParam(defaultValue = "0") long start,
            @RequestParam(defaultValue = "-1") long end) {

        Set<String> members =
                service.range(
                        key,
                        start,
                        end
                );

        return Map.of(
                "operation", "ZRANGE",
                "key", key,
                "members", members
        );
    }

    @GetMapping("/reverse")
    public Map<String, Object> reverseRange(
            @RequestParam String key,
            @RequestParam(defaultValue = "0") long start,
            @RequestParam(defaultValue = "-1") long end) {

        Set<String> members =
                service.reverseRange(
                        key,
                        start,
                        end
                );

        return Map.of(
                "operation", "ZREVRANGE",
                "key", key,
                "members", members
        );
    }

    @GetMapping("/with-scores")
    public Map<String, Object> rangeWithScores(
            @RequestParam String key,
            @RequestParam(defaultValue = "0") long start,
            @RequestParam(defaultValue = "-1") long end) {

        Set<ZSetOperations.TypedTuple<String>> tuples =
                service.rangeWithScores(
                        key,
                        start,
                        end
                );

        Map<String, Double> results =
                tuples.stream()
                        .collect(Collectors.toMap(
                                ZSetOperations.TypedTuple::getValue,
                                tuple -> tuple.getScore() == null
                                        ? 0.0
                                        : tuple.getScore()
                        ));

        return Map.of(
                "operation", "ZRANGE WITHSCORES",
                "key", key,
                "members", results
        );
    }

    @GetMapping("/by-score")
    public Map<String, Object> rangeByScore(
            @RequestParam String key,
            @RequestParam double min,
            @RequestParam double max) {

        Set<String> members =
                service.rangeByScore(
                        key,
                        min,
                        max
                );

        return Map.of(
                "operation", "ZRANGEBYSCORE",
                "key", key,
                "min", min,
                "max", max,
                "members", members
        );
    }

    @GetMapping("/by-score/reverse")
    public Map<String, Object> reverseRangeByScore(
            @RequestParam String key,
            @RequestParam double min,
            @RequestParam double max) {

        Set<String> members =
                service.reverseRangeByScore(
                        key,
                        min,
                        max
                );

        return Map.of(
                "operation", "ZREVRANGEBYSCORE",
                "key", key,
                "min", min,
                "max", max,
                "members", members
        );
    }

    @GetMapping("/count")
    public Map<String, Object> count(
            @RequestParam String key,
            @RequestParam double min,
            @RequestParam double max) {

        Long count =
                service.countByScore(
                        key,
                        min,
                        max
                );

        return Map.of(
                "operation", "ZCOUNT",
                "key", key,
                "min", min,
                "max", max,
                "count", count
        );
    }

    @PostMapping("/increment")
    public Map<String, Object> increment(
            @RequestParam String key,
            @RequestParam String member,
            @RequestParam double delta) {

        Double score =
                service.incrementScore(
                        key,
                        member,
                        delta
                );

        return Map.of(
                "operation", "ZINCRBY",
                "key", key,
                "member", member,
                "delta", delta,
                "score", score
        );
    }

    @GetMapping("/size")
    public Map<String, Object> size(
            @RequestParam String key) {

        return Map.of(
                "operation", "ZCARD",
                "key", key,
                "size", service.size(key)
        );
    }

    @DeleteMapping
    public Map<String, Object> delete(
            @RequestParam String key) {

        boolean deleted =
                service.deleteSortedSet(key);

        return Map.of(
                "operation", "DEL",
                "key", key,
                "deleted", deleted
        );
    }
}