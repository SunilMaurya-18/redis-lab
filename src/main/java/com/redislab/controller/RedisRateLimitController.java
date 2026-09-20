package com.redislab.controller;

import com.redislab.model.RateLimitResponse;
import com.redislab.service.RedisRateLimitService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/redis/rate-limits")
public class RedisRateLimitController {

    private final RedisRateLimitService service;

    public RedisRateLimitController(RedisRateLimitService service) {
        this.service = service;
    }

    @PostMapping("/simple")
    public RateLimitResponse simple(
            @RequestParam String scope,
            @RequestParam(defaultValue = "10") long limit,
            @RequestParam(defaultValue = "60000") long windowMs) {

        return service.simpleFixedWindow(scope, limit, windowMs);
    }

    @PostMapping("/atomic")
    public RateLimitResponse atomic(
            @RequestParam String scope,
            @RequestParam(defaultValue = "10") long limit,
            @RequestParam(defaultValue = "60000") long windowMs) {

        return service.atomicFixedWindow(scope, limit, windowMs);
    }
}
