package com.redislab.model;

public record LockResponse(
        String key,
        boolean acquired,
        String token,
        long ttlMs) {
}
