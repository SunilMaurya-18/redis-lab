package com.redislab.controller;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/redis")
public class RedisConnectionController {
    private final StringRedisTemplate redisTemplate;

    public RedisConnectionController(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @GetMapping("/ping")
    public Map<String, String> ping() {
        String response = redisTemplate
                .getConnectionFactory().getConnection().ping();
        return Map.of(
                "redis", "connected",
                "response", response
        );
    }
}
