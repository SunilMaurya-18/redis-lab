package com.redislab.model;

public record RateLimitResponse(
        String key,
        boolean allowed,
        long limit,
        long current,
        long remaining,
        long resetAfterSeconds) {
}
