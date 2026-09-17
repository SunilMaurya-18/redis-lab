package com.redislab.controller;

import com.redislab.service.RedisHashService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/redis/hashes")
public class RedisHashController {

    private final RedisHashService redisHashService;

    public RedisHashController(
            RedisHashService redisHashService) {

        this.redisHashService = redisHashService;
    }

    // --------------------------------------------------
    // HSET
    // --------------------------------------------------

    @PostMapping("/field")
    public Map<String, Object> set(
            @RequestParam String key,
            @RequestParam String field,
            @RequestParam String value) {

        redisHashService.set(key, field, value);

        return Map.of(
                "operation", "HSET",
                "key", key,
                "field", field,
                "message", "Field stored successfully"
        );
    }

    // --------------------------------------------------
    // HSETNX
    // --------------------------------------------------

    @PostMapping("/field/nx")
    public Map<String, Object> setIfAbsent(
            @RequestParam String key,
            @RequestParam String field,
            @RequestParam String value) {

        boolean created =
                redisHashService.setIfAbsent(
                        key,
                        field,
                        value
                );

        return Map.of(
                "operation", "HSETNX",
                "key", key,
                "field", field,
                "created", created
        );
    }

    // --------------------------------------------------
    // HSET MULTIPLE
    // --------------------------------------------------

    @PostMapping
    public Map<String, Object> setMultiple(
            @RequestParam String key,
            @RequestBody Map<String, String> fields) {

        redisHashService.setMultiple(key, fields);

        return Map.of(
                "operation", "HSET",
                "key", key,
                "fieldCount", fields.size()
        );
    }

    // --------------------------------------------------
    // HGET
    // --------------------------------------------------

    @GetMapping("/field")
    public Map<String, Object> get(
            @RequestParam String key,
            @RequestParam String field) {

        String value =
                redisHashService.get(key, field);

        return Map.of(
                "operation", "HGET",
                "key", key,
                "field", field,
                "value", value
        );
    }

    // --------------------------------------------------
    // HMGET
    // --------------------------------------------------

    @GetMapping("/fields")
    public Map<String, Object> getMultiple(
            @RequestParam String key,
            @RequestParam List<String> fields) {

        List<String> values =
                redisHashService.getMultiple(
                        key,
                        fields
                );

        return Map.of(
                "operation", "HMGET",
                "key", key,
                "fields", fields,
                "values", values
        );
    }

    // --------------------------------------------------
    // HGETALL
    // --------------------------------------------------

    @GetMapping
    public Map<String, Object> getAll(
            @RequestParam String key) {

        Map<String, String> values =
                redisHashService.getAll(key);

        return Map.of(
                "operation", "HGETALL",
                "key", key,
                "values", values
        );
    }

    // --------------------------------------------------
    // HEXISTS
    // --------------------------------------------------

    @GetMapping("/exists")
    public Map<String, Object> fieldExists(
            @RequestParam String key,
            @RequestParam String field) {

        boolean exists =
                redisHashService.fieldExists(
                        key,
                        field
                );

        return Map.of(
                "operation", "HEXISTS",
                "key", key,
                "field", field,
                "exists", exists
        );
    }

    // --------------------------------------------------
    // HDEL
    // --------------------------------------------------

    @DeleteMapping("/fields")
    public Map<String, Object> deleteFields(
            @RequestParam String key,
            @RequestParam List<String> fields) {

        long deleted =
                redisHashService.deleteFields(
                        key,
                        fields.toArray(new String[0])
                );

        return Map.of(
                "operation", "HDEL",
                "key", key,
                "deletedFields", deleted
        );
    }

    // --------------------------------------------------
    // HLEN
    // --------------------------------------------------

    @GetMapping("/length")
    public Map<String, Object> length(
            @RequestParam String key) {

        long length =
                redisHashService.length(key);

        return Map.of(
                "operation", "HLEN",
                "key", key,
                "fieldCount", length
        );
    }

    // --------------------------------------------------
    // HKEYS
    // --------------------------------------------------

    @GetMapping("/keys")
    public Map<String, Object> keys(
            @RequestParam String key) {

        return Map.of(
                "operation", "HKEYS",
                "key", key,
                "fields", redisHashService.keys(key)
        );
    }

    // --------------------------------------------------
    // HVALS
    // --------------------------------------------------

    @GetMapping("/values")
    public Map<String, Object> values(
            @RequestParam String key) {

        return Map.of(
                "operation", "HVALS",
                "key", key,
                "values", redisHashService.values(key)
        );
    }

    // --------------------------------------------------
    // HINCRBY
    // --------------------------------------------------

    @PostMapping("/increment")
    public Map<String, Object> increment(
            @RequestParam String key,
            @RequestParam String field,
            @RequestParam long amount) {

        Long value =
                redisHashService.incrementBy(
                        key,
                        field,
                        amount
                );

        return Map.of(
                "operation", "HINCRBY",
                "key", key,
                "field", field,
                "amount", amount,
                "value", value
        );
    }

    // --------------------------------------------------
    // HINCRBYFLOAT
    // --------------------------------------------------

    @PostMapping("/increment-float")
    public Map<String, Object> incrementFloat(
            @RequestParam String key,
            @RequestParam String field,
            @RequestParam double amount) {

        Double value =
                redisHashService.incrementByFloat(
                        key,
                        field,
                        amount
                );

        return Map.of(
                "operation", "HINCRBYFLOAT",
                "key", key,
                "field", field,
                "amount", amount,
                "value", value
        );
    }

    // --------------------------------------------------
    // DELETE ENTIRE HASH
    // --------------------------------------------------

    @DeleteMapping
    public Map<String, Object> deleteHash(
            @RequestParam String key) {

        boolean deleted =
                redisHashService.deleteHash(key);

        return Map.of(
                "operation", "DEL",
                "key", key,
                "deleted", deleted
        );
    }
}