package com.redislab.model;

public record BenchmarkResult(
        String operation,
        int warmupIterations,
        int measuredIterations,
        int batchSize,
        long totalOperations,
        double elapsedMs,
        double operationsPerSecond,
        double p50Ms,
        double p95Ms,
        double p99Ms) {
}
