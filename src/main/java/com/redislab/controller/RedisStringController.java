package com.redislab.controller;

import com.redislab.service.RedisStringService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/redis/strings")
public class RedisStringController {

    private final RedisStringService redisStringService;

    public RedisStringController(
            RedisStringService redisStringService) {

        this.redisStringService = redisStringService;
    }

    // --------------------------------------------------
    // SET
    // --------------------------------------------------

    @PostMapping
    public Map<String, Object> set(
            @RequestParam String key,
            @RequestParam String value) {

        redisStringService.set(key, value);

        return Map.of(
                "operation", "SET",
                "key", key,
                "message", "Value stored successfully"
        );
    }

    // --------------------------------------------------
    // GET
    // --------------------------------------------------

    @GetMapping
    public Map<String, Object> get(
            @RequestParam String key) {

        String value = redisStringService.get(key);

        return Map.of(
                "operation", "GET",
                "key", key,
                "value", value
        );
    }

    // --------------------------------------------------
    // SET NX
    // --------------------------------------------------

    @PostMapping("/nx")
    public Map<String, Object> setIfAbsent(
            @RequestParam String key,
            @RequestParam String value) {

        boolean created =
                redisStringService.setIfAbsent(key, value);

        return Map.of(
                "operation", "SET NX",
                "key", key,
                "created", created
        );
    }

    // --------------------------------------------------
    // SET XX
    // --------------------------------------------------

    @PutMapping("/xx")
    public Map<String, Object> setIfPresent(
            @RequestParam String key,
            @RequestParam String value) {

        boolean updated =
                redisStringService.setIfPresent(key, value);

        return Map.of(
                "operation", "SET XX",
                "key", key,
                "updated", updated
        );
    }

    // --------------------------------------------------
    // MSET
    // --------------------------------------------------

    @PostMapping("/multiple")
    public Map<String, Object> setMultiple(
            @RequestParam List<String> keys,
            @RequestParam List<String> values) {

        redisStringService.setMultiple(keys, values);

        return Map.of(
                "operation", "MSET",
                "message", "Values stored successfully"
        );
    }

    // --------------------------------------------------
    // MGET
    // --------------------------------------------------

    @GetMapping("/multiple")
    public Map<String, Object> getMultiple(
            @RequestParam List<String> keys) {

        List<String> values =
                redisStringService.getMultiple(keys);

        return Map.of(
                "operation", "MGET",
                "keys", keys,
                "values", values
        );
    }

    // --------------------------------------------------
    // INCR
    // --------------------------------------------------

    @PostMapping("/increment")
    public Map<String, Object> increment(
            @RequestParam String key) {

        Long value =
                redisStringService.increment(key);

        return Map.of(
                "operation", "INCR",
                "key", key,
                "value", value
        );
    }

    // --------------------------------------------------
    // DECR
    // --------------------------------------------------

    @PostMapping("/decrement")
    public Map<String, Object> decrement(
            @RequestParam String key) {

        Long value =
                redisStringService.decrement(key);

        return Map.of(
                "operation", "DECR",
                "key", key,
                "value", value
        );
    }

    // --------------------------------------------------
    // INCRBY
    // --------------------------------------------------

    @PostMapping("/increment-by")
    public Map<String, Object> incrementBy(
            @RequestParam String key,
            @RequestParam long amount) {

        Long value =
                redisStringService.incrementBy(key, amount);

        return Map.of(
                "operation", "INCRBY",
                "key", key,
                "amount", amount,
                "value", value
        );
    }

    // --------------------------------------------------
    // DECRBY
    // --------------------------------------------------

    @PostMapping("/decrement-by")
    public Map<String, Object> decrementBy(
            @RequestParam String key,
            @RequestParam long amount) {

        Long value =
                redisStringService.decrementBy(key, amount);

        return Map.of(
                "operation", "DECRBY",
                "key", key,
                "amount", amount,
                "value", value
        );
    }

    // --------------------------------------------------
    // APPEND
    // --------------------------------------------------

    @PostMapping("/append")
    public Map<String, Object> append(
            @RequestParam String key,
            @RequestParam String value) {

        Integer length =
                redisStringService.append(key, value);

        return Map.of(
                "operation", "APPEND",
                "key", key,
                "newLength", length
        );
    }

    // --------------------------------------------------
    // STRLEN
    // --------------------------------------------------

    @GetMapping("/length")
    public Map<String, Object> length(
            @RequestParam String key) {

        Long length =
                redisStringService.length(key);

        return Map.of(
                "operation", "STRLEN",
                "key", key,
                "length", length
        );
    }

    // --------------------------------------------------
    // GETSET
    // --------------------------------------------------

    @PutMapping("/get-set")
    public Map<String, Object> getAndSet(
            @RequestParam String key,
            @RequestParam String value) {

        String oldValue =
                redisStringService.getAndSet(key, value);

        return Map.of(
                "operation", "GETSET",
                "key", key,
                "oldValue", oldValue
        );
    }

    // --------------------------------------------------
    // GETDEL
    // --------------------------------------------------

    @DeleteMapping("/get-delete")
    public Map<String, Object> getAndDelete(
            @RequestParam String key) {

        String value =
                redisStringService.getAndDelete(key);

        return Map.of(
                "operation", "GETDEL",
                "key", key,
                "value", value
        );
    }

    // --------------------------------------------------
    // EXISTS
    // --------------------------------------------------

    @GetMapping("/exists")
    public Map<String, Object> exists(
            @RequestParam String key) {

        boolean exists =
                redisStringService.exists(key);

        return Map.of(
                "operation", "EXISTS",
                "key", key,
                "exists", exists
        );
    }

    // --------------------------------------------------
    // DELETE
    // --------------------------------------------------

    @DeleteMapping
    public Map<String, Object> delete(
            @RequestParam String key) {

        boolean deleted =
                redisStringService.delete(key);

        return Map.of(
                "operation", "DEL",
                "key", key,
                "deleted", deleted
        );
    }

    // --------------------------------------------------
    // SET WITH TTL
    // --------------------------------------------------

    @PostMapping("/ttl")
    public Map<String, Object> setWithTtl(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam long seconds) {

        redisStringService.setWithTtl(
                key,
                value,
                seconds
        );

        return Map.of(
                "operation", "SET EX",
                "key", key,
                "ttlSeconds", seconds
        );
    }

    // --------------------------------------------------
    // TTL
    // --------------------------------------------------

    @GetMapping("/ttl")
    public Map<String, Object> ttl(
            @RequestParam String key) {

        Long ttl =
                redisStringService.getTtl(key);

        return Map.of(
                "operation", "TTL",
                "key", key,
                "ttlSeconds", ttl
        );
    }

    // --------------------------------------------------
    // EXPIRE
    // --------------------------------------------------

    @PutMapping("/expire")
    public Map<String, Object> expire(
            @RequestParam String key,
            @RequestParam long seconds) {

        boolean result =
                redisStringService.expire(key, seconds);

        return Map.of(
                "operation", "EXPIRE",
                "key", key,
                "success", result
        );
    }

    // --------------------------------------------------
    // PERSIST
    // --------------------------------------------------

    @PutMapping("/persist")
    public Map<String, Object> persist(
            @RequestParam String key) {

        boolean result =
                redisStringService.persist(key);

        return Map.of(
                "operation", "PERSIST",
                "key", key,
                "success", result
        );
    }
}