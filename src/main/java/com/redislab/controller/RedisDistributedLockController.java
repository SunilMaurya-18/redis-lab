package com.redislab.controller;

import com.redislab.model.LockResponse;
import com.redislab.service.RedisDistributedLockService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/redis/locks")
public class RedisDistributedLockController {

    private final RedisDistributedLockService service;

    public RedisDistributedLockController(RedisDistributedLockService service) {
        this.service = service;
    }

    @PostMapping("/try")
    public LockResponse tryLock(
            @RequestParam String key,
            @RequestParam(defaultValue = "redis-lab") String owner,
            @RequestParam(defaultValue = "10000") long ttlMs) {

        return service.tryLock(key, owner, ttlMs);
    }

    @PostMapping("/release")
    public Map<String, Object> release(
            @RequestParam String key,
            @RequestParam String token) {

        return Map.of("key", key, "released", service.release(key, token));
    }

    @PostMapping("/renew")
    public Map<String, Object> renew(
            @RequestParam String key,
            @RequestParam String token,
            @RequestParam(defaultValue = "10000") long ttlMs) {

        return Map.of("key", key, "renewed", service.renew(key, token, ttlMs));
    }

    @GetMapping("/ttl")
    public Map<String, Object> ttl(@RequestParam String key) {
        return Map.of("key", key, "ttlMs", service.ttl(key));
    }
}
