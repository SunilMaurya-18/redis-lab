package com.redislab.controller;

import com.redislab.service.RedisExpirationService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/redis/expiration")
public class RedisExpirationController {

    private final RedisExpirationService service;

    public RedisExpirationController(
            RedisExpirationService service) {

        this.service = service;
    }

    // ---------------------------------------------------------
    // EXPIRE
    // ---------------------------------------------------------

    @PostMapping("/expire")
    public Map<String, Object> expire(
            @RequestParam String key,
            @RequestParam long seconds) {

        boolean result =
                service.expire(key, seconds);

        return Map.of(
                "operation", "EXPIRE",
                "key", key,
                "seconds", seconds,
                "success", result
        );
    }

    // ---------------------------------------------------------
    // PEXPIRE
    // ---------------------------------------------------------

    @PostMapping("/pexpire")
    public Map<String, Object> pExpire(
            @RequestParam String key,
            @RequestParam long milliseconds) {

        boolean result =
                service.pExpire(
                        key,
                        milliseconds
                );

        return Map.of(
                "operation", "PEXPIRE",
                "key", key,
                "milliseconds", milliseconds,
                "success", result
        );
    }

    // ---------------------------------------------------------
    // TTL
    // ---------------------------------------------------------

    @GetMapping("/ttl")
    public Map<String, Object> ttl(
            @RequestParam String key) {

        Long ttl =
                service.ttl(key);

        return Map.of(
                "operation", "TTL",
                "key", key,
                "ttlSeconds", ttl
        );
    }

    // ---------------------------------------------------------
    // PTTL
    // ---------------------------------------------------------

    @GetMapping("/pttl")
    public Map<String, Object> pTtl(
            @RequestParam String key) {

        Long ttl =
                service.pTtl(key);

        return Map.of(
                "operation", "PTTL",
                "key", key,
                "ttlMilliseconds", ttl
        );
    }

    // ---------------------------------------------------------
    // EXPIREAT
    // ---------------------------------------------------------

    @PostMapping("/expire-at")
    public Map<String, Object> expireAt(
            @RequestParam String key,
            @RequestParam long unixSeconds) {

        boolean result =
                service.expireAt(
                        key,
                        unixSeconds
                );

        return Map.of(
                "operation", "EXPIREAT",
                "key", key,
                "unixSeconds", unixSeconds,
                "success", result
        );
    }

    // ---------------------------------------------------------
    // PEXPIREAT
    // ---------------------------------------------------------

    @PostMapping("/pexpire-at")
    public Map<String, Object> pExpireAt(
            @RequestParam String key,
            @RequestParam long unixMilliseconds) {

        boolean result =
                service.pExpireAt(
                        key,
                        unixMilliseconds
                );

        return Map.of(
                "operation", "PEXPIREAT",
                "key", key,
                "unixMilliseconds", unixMilliseconds,
                "success", result
        );
    }

    // ---------------------------------------------------------
    // PERSIST
    // ---------------------------------------------------------

    @PostMapping("/persist")
    public Map<String, Object> persist(
            @RequestParam String key) {

        boolean result =
                service.persist(key);

        return Map.of(
                "operation", "PERSIST",
                "key", key,
                "success", result
        );
    }

    // ---------------------------------------------------------
    // SET EX
    // ---------------------------------------------------------

    @PostMapping("/set-ex")
    public Map<String, Object> setEx(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam long seconds) {

        service.setWithExpiration(
                key,
                value,
                seconds
        );

        return Map.of(
                "operation", "SET EX",
                "key", key,
                "seconds", seconds,
                "success", true
        );
    }

    // ---------------------------------------------------------
    // SET PX
    // ---------------------------------------------------------

    @PostMapping("/set-px")
    public Map<String, Object> setPx(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam long milliseconds) {

        service.setWithMillisecondExpiration(
                key,
                value,
                milliseconds
        );

        return Map.of(
                "operation", "SET PX",
                "key", key,
                "milliseconds", milliseconds,
                "success", true
        );
    }

    // ---------------------------------------------------------
    // SET NX EX
    // ---------------------------------------------------------

    @PostMapping("/set-nx-ex")
    public Map<String, Object> setNxEx(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam long seconds) {

        boolean created =
                service.setIfAbsentWithExpiration(
                        key,
                        value,
                        seconds
                );

        return Map.of(
                "operation", "SET NX EX",
                "key", key,
                "seconds", seconds,
                "created", created
        );
    }

    // ---------------------------------------------------------
    // SET NX PX
    // ---------------------------------------------------------

    @PostMapping("/set-nx-px")
    public Map<String, Object> setNxPx(
            @RequestParam String key,
            @RequestParam String value,
            @RequestParam long milliseconds) {

        boolean created =
                service.setIfAbsentWithMillisecondExpiration(
                        key,
                        value,
                        milliseconds
                );

        return Map.of(
                "operation", "SET NX PX",
                "key", key,
                "milliseconds", milliseconds,
                "created", created
        );
    }
}