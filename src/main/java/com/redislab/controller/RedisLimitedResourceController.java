package com.redislab.controller;

import com.redislab.model.RateLimitResponse;
import com.redislab.service.RedisRateLimitService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/limited")
public class RedisLimitedResourceController {

    private final RedisRateLimitService service;

    public RedisLimitedResourceController(RedisRateLimitService service) {
        this.service = service;
    }

    @GetMapping("/resource")
    public ResponseEntity<Map<String, Object>> resource(
            @RequestParam(required = false) String clientId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String forwardedFor) {

        String scope = clientId != null && !clientId.isBlank()
                ? "user:" + clientId
                : "ip:" + (forwardedFor == null || forwardedFor.isBlank() ? "unknown" : forwardedFor);

        RateLimitResponse result = service.atomicFixedWindow(scope, 5, 60_000);
        HttpHeaders headers = new HttpHeaders();
        headers.add("X-RateLimit-Limit", Long.toString(result.limit()));
        headers.add("X-RateLimit-Remaining", Long.toString(result.remaining()));
        headers.add("X-RateLimit-Reset-After", Long.toString(result.resetAfterSeconds()));

        Map<String, Object> body = Map.of(
                "allowed", result.allowed(),
                "scope", scope,
                "current", result.current(),
                "message", result.allowed() ? "request accepted" : "rate limit exceeded"
        );

        return result.allowed()
                ? ResponseEntity.ok().headers(headers).body(body)
                : ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).headers(headers).body(body);
    }
}
