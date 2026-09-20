package com.redislab.controller;

import com.redislab.service.RedisListService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/redis/lists")
public class RedisListController {

    private final RedisListService redisListService;

    public RedisListController(
            RedisListService redisListService) {

        this.redisListService = redisListService;
    }

    @PostMapping("/left")
    public Map<String, Object> pushLeft(
            @RequestParam String key,
            @RequestParam String value) {

        Long size =
                redisListService.pushLeft(
                        key,
                        value
                );

        return Map.of(
                "operation", "LPUSH",
                "key", key,
                "value", value,
                "size", size
        );
    }

    @PostMapping("/right")
    public Map<String, Object> pushRight(
            @RequestParam String key,
            @RequestParam String value) {

        Long size =
                redisListService.pushRight(
                        key,
                        value
                );

        return Map.of(
                "operation", "RPUSH",
                "key", key,
                "value", value,
                "size", size
        );
    }

    @PostMapping("/left/multiple")
    public Map<String, Object> pushLeftMultiple(
            @RequestParam String key,
            @RequestBody List<String> values) {

        Long size =
                redisListService.pushLeftMultiple(
                        key,
                        values
                );

        return Map.of(
                "operation", "LPUSH",
                "key", key,
                "values", values,
                "size", size
        );
    }

    @PostMapping("/right/multiple")
    public Map<String, Object> pushRightMultiple(
            @RequestParam String key,
            @RequestBody List<String> values) {

        Long size =
                redisListService.pushRightMultiple(
                        key,
                        values
                );

        return Map.of(
                "operation", "RPUSH",
                "key", key,
                "values", values,
                "size", size
        );
    }

    @PostMapping("/pop/left")
    public Map<String, Object> popLeft(
            @RequestParam String key) {

        String value =
                redisListService.popLeft(key);

        return Map.of(
                "operation", "LPOP",
                "key", key,
                "value", value == null ? "" : value
        );
    }

    @PostMapping("/pop/right")
    public Map<String, Object> popRight(
            @RequestParam String key) {

        String value =
                redisListService.popRight(key);

        return Map.of(
                "operation", "RPOP",
                "key", key,
                "value", value == null ? "" : value
        );
    }

    @GetMapping("/range")
    public Map<String, Object> range(
            @RequestParam String key,
            @RequestParam long start,
            @RequestParam long end) {

        List<String> values =
                redisListService.range(
                        key,
                        start,
                        end
                );

        return Map.of(
                "operation", "LRANGE",
                "key", key,
                "start", start,
                "end", end,
                "values", values
        );
    }

    @GetMapping("/length")
    public Map<String, Object> length(
            @RequestParam String key) {

        Long size =
                redisListService.length(key);

        return Map.of(
                "operation", "LLEN",
                "key", key,
                "size", size
        );
    }

    @GetMapping("/index")
    public Map<String, Object> index(
            @RequestParam String key,
            @RequestParam long index) {

        String value =
                redisListService.index(
                        key,
                        index
                );

        return Map.of(
                "operation", "LINDEX",
                "key", key,
                "index", index,
                "value", value == null ? "" : value
        );
    }

    @PutMapping("/index")
    public Map<String, Object> set(
            @RequestParam String key,
            @RequestParam long index,
            @RequestParam String value) {

        redisListService.set(
                key,
                index,
                value
        );

        return Map.of(
                "operation", "LSET",
                "key", key,
                "index", index,
                "value", value
        );
    }

    @DeleteMapping("/remove")
    public Map<String, Object> remove(
            @RequestParam String key,
            @RequestParam long count,
            @RequestParam String value) {

        Long removed =
                redisListService.remove(
                        key,
                        count,
                        value
                );

        return Map.of(
                "operation", "LREM",
                "key", key,
                "count", count,
                "value", value,
                "removed", removed
        );
    }

    @PostMapping("/trim")
    public Map<String, Object> trim(
            @RequestParam String key,
            @RequestParam long start,
            @RequestParam long end) {

        redisListService.trim(
                key,
                start,
                end
        );

        return Map.of(
                "operation", "LTRIM",
                "key", key,
                "start", start,
                "end", end
        );
    }

    @PostMapping("/pop/left/blocking")
    public Map<String, Object> blockingPopLeft(
            @RequestParam String key,
            @RequestParam long timeoutSeconds) {

        String result =
                redisListService.blockingPopLeft(
                        key,
                        timeoutSeconds
                );

        return Map.of(
                "operation", "BLPOP",
                "key", key,
                "timeoutSeconds", timeoutSeconds,
                "result", result == null ? List.of() : result
        );
    }

    @PostMapping("/pop/right/blocking")
    public Map<String, Object> blockingPopRight(
            @RequestParam String key,
            @RequestParam long timeoutSeconds) {

        String result =
                redisListService.blockingPopRight(
                        key,
                        timeoutSeconds
                );

        return Map.of(
                "operation", "BRPOP",
                "key", key,
                "timeoutSeconds", timeoutSeconds,
                "result", result == null ? List.of() : result
        );
    }

    @DeleteMapping
    public Map<String, Object> delete(
            @RequestParam String key) {

        boolean deleted =
                redisListService.deleteList(key);

        return Map.of(
                "operation", "DEL",
                "key", key,
                "deleted", deleted
        );
    }
}