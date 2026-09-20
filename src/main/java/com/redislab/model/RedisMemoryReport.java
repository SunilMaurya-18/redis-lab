package com.redislab.model;

import java.util.Map;

public record RedisMemoryReport(
        long databaseSize,
        Map<String, Object> memoryUsage,
        String memoryInfo,
        String memoryDoctor) {
}
