package com.redislab.controller;

import com.redislab.service.RedisSetService;
import org.springframework.web.bind.annotation.*;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

@RestController
@RequestMapping("/api/redis/sets")
public class RedisSetController {

    private final RedisSetService redisSetService;

    public RedisSetController(
            RedisSetService redisSetService) {

        this.redisSetService = redisSetService;
    }

    @PostMapping("/add")
    public Map<String, Object> add(
            @RequestParam String key,
            @RequestParam String value) {

        Long added =
                redisSetService.add(
                        key,
                        value
                );

        return Map.of(
                "operation", "SADD",
                "key", key,
                "value", value,
                "added", added
        );
    }

    @PostMapping("/add/multiple")
    public Map<String, Object> addMultiple(
            @RequestParam String key,
            @RequestBody String[] values) {

        Long added =
                redisSetService.addMultiple(
                        key,
                        values
                );

        return Map.of(
                "operation", "SADD",
                "key", key,
                "values", Arrays.asList(values),
                "added", added
        );
    }

    @DeleteMapping("/remove")
    public Map<String, Object> remove(
            @RequestParam String key,
            @RequestParam String[] values) {

        Long removed =
                redisSetService.remove(
                        key,
                        values
                );

        return Map.of(
                "operation", "SREM",
                "key", key,
                "values", Arrays.asList(values),
                "removed", removed
        );
    }

    @GetMapping("/member")
    public Map<String, Object> isMember(
            @RequestParam String key,
            @RequestParam String value) {

        boolean exists =
                redisSetService.isMember(
                        key,
                        value
                );

        return Map.of(
                "operation", "SISMEMBER",
                "key", key,
                "value", value,
                "member", exists
        );
    }

    @GetMapping
    public Map<String, Object> members(
            @RequestParam String key) {

        Set<String> members =
                redisSetService.members(key);

        return Map.of(
                "operation", "SMEMBERS",
                "key", key,
                "members", members
        );
    }

    @GetMapping("/size")
    public Map<String, Object> size(
            @RequestParam String key) {

        Long size =
                redisSetService.size(key);

        return Map.of(
                "operation", "SCARD",
                "key", key,
                "size", size
        );
    }

    @PostMapping("/pop")
    public Map<String, Object> pop(
            @RequestParam String key) {

        String value =
                redisSetService.pop(key);

        return Map.of(
                "operation", "SPOP",
                "key", key,
                "value", value == null ? "" : value
        );
    }

    @PostMapping("/pop/multiple")
    public Map<String, Object> popMultiple(
            @RequestParam String key,
            @RequestParam long count) {

        List<String> values =
                redisSetService.pop(
                        key,
                        count
                );

        return Map.of(
                "operation", "SPOP",
                "key", key,
                "count", count,
                "values", values
        );
    }

    @GetMapping("/random")
    public Map<String, Object> randomMember(
            @RequestParam String key) {

        String value =
                redisSetService.randomMember(key);

        return Map.of(
                "operation", "SRANDMEMBER",
                "key", key,
                "value", value == null ? "" : value
        );
    }

    @GetMapping("/random/multiple")
    public Map<String, Object> randomMembers(
            @RequestParam String key,
            @RequestParam long count) {

        List<String> values =
                redisSetService.randomMembers(
                        key,
                        count
                );

        return Map.of(
                "operation", "SRANDMEMBER",
                "key", key,
                "count", count,
                "values", values
        );
    }

    @PostMapping("/move")
    public Map<String, Object> move(
            @RequestParam String source,
            @RequestParam String destination,
            @RequestParam String value) {

        boolean moved =
                redisSetService.move(
                        source,
                        destination,
                        value
                );

        return Map.of(
                "operation", "SMOVE",
                "source", source,
                "destination", destination,
                "value", value,
                "moved", moved
        );
    }

    @GetMapping("/intersect")
    public Map<String, Object> intersect(
            @RequestParam String key1,
            @RequestParam String key2) {

        Set<String> result =
                redisSetService.intersect(
                        key1,
                        key2
                );

        return Map.of(
                "operation", "SINTER",
                "key1", key1,
                "key2", key2,
                "members", result
        );
    }

    @GetMapping("/union")
    public Map<String, Object> union(
            @RequestParam String key1,
            @RequestParam String key2) {

        Set<String> result =
                redisSetService.union(
                        key1,
                        key2
                );

        return Map.of(
                "operation", "SUNION",
                "key1", key1,
                "key2", key2,
                "members", result
        );
    }

    @GetMapping("/difference")
    public Map<String, Object> difference(
            @RequestParam String key1,
            @RequestParam String key2) {

        Set<String> result =
                redisSetService.difference(
                        key1,
                        key2
                );

        return Map.of(
                "operation", "SDIFF",
                "key1", key1,
                "key2", key2,
                "members", result
        );
    }

    @DeleteMapping
    public Map<String, Object> delete(
            @RequestParam String key) {

        boolean deleted =
                redisSetService.deleteSet(key);

        return Map.of(
                "operation", "DEL",
                "key", key,
                "deleted", deleted
        );
    }
}