package com.redislab.model;

import java.util.List;

public record RedisKeyAnalysis(
        String pattern,
        long scannedKeys,
        long totalMemoryBytes,
        List<RedisKeyInspection> largestKeys) {
}
