package com.redislab.model;

public record RedisKeyInspection(
        String key,
        String type,
        long ttlSeconds,
        long idleSeconds,
        long memoryBytes,
        String encoding,
        String valuePreview) {
}
