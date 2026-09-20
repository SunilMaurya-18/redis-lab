package com.redislab.controller;

import com.redislab.service.RedisTransactionService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/redis/transactions")
public class RedisTransactionController {

    private final RedisTransactionService service;

    public RedisTransactionController(
            RedisTransactionService service) {

        this.service = service;
    }

    @PostMapping("/execute")
    public Map<String, Object> execute(
            @RequestParam String key1,
            @RequestParam String value1,
            @RequestParam String key2,
            @RequestParam String value2) {

        List<Object> results =
                service.executeTransaction(
                        key1,
                        value1,
                        key2,
                        value2
                );

        return Map.of(
                "operation", "MULTI + EXEC",
                "results", results
        );
    }
    @PostMapping("/discard")
    public Map<String, Object> discard(
            @RequestParam String key,
            @RequestParam String value) {

        service.discardTransaction(
                key,
                value
        );

        return Map.of(
                "operation", "MULTI + DISCARD",
                "key", key,
                "discarded", true
        );
    }

    @PostMapping("/watch-set")
    public Map<String, Object> watchSet(
            @RequestParam String key,
            @RequestParam String expectedValue,
            @RequestParam String newValue) {

        List<Object> results =
                service.watchAndSet(
                        key,
                        expectedValue,
                        newValue
                );

        return Map.of(
                "operation", "WATCH + MULTI + EXEC",
                "key", key,
                "results", results
        );
    }

    @PostMapping("/watch-increment")
    public Map<String, Object> watchIncrement(
            @RequestParam String key,
            @RequestParam long amount) {

        List<Object> results =
                service.watchAndIncrement(
                        key,
                        amount
                );

        return Map.of(
                "operation", "WATCH + MULTI + EXEC",
                "key", key,
                "amount", amount,
                "results", results
        );
    }
}