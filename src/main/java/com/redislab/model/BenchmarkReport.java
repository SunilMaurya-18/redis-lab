package com.redislab.model;

import java.util.List;

public record BenchmarkReport(
        String keyPrefix,
        int warmupIterations,
        int measuredIterations,
        int batchSize,
        List<BenchmarkResult> results,
        String interpretation) {
}
