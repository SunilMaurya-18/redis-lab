package com.redislab.service;

import com.redislab.model.BenchmarkReport;
import com.redislab.model.BenchmarkResult;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.LongSupplier;

@Service
public class RedisBenchmarkService {

    private static final RedisScript<Long> INCREMENT_SCRIPT = RedisScript.of(
            "return redis.call('incr', KEYS[1])",
            Long.class
    );

    private final StringRedisTemplate redisTemplate;

    public RedisBenchmarkService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public BenchmarkReport run(
            String keyPrefix,
            int warmupIterations,
            int measuredIterations,
            int batchSize) {

        validate(keyPrefix, warmupIterations, measuredIterations, batchSize);
        List<BenchmarkResult> results = new ArrayList<>();
        results.add(measure("individual-set-get", warmupIterations, measuredIterations, batchSize,
                () -> individualSetGet(keyPrefix, batchSize)));
        results.add(measure("pipeline-set-get", warmupIterations, measuredIterations, batchSize,
                () -> pipelineSetGet(keyPrefix, batchSize)));
        results.add(measure("mset-mget", warmupIterations, measuredIterations, batchSize,
                () -> msetMget(keyPrefix, batchSize)));
        results.add(measure("lua-increment", warmupIterations, measuredIterations, 1,
                () -> luaIncrement(keyPrefix)));
        return new BenchmarkReport(
                keyPrefix,
                warmupIterations,
                measuredIterations,
                batchSize,
                results,
                "Compare throughput and percentiles on the same Redis host; repeat with multiple batch sizes and do not treat one run as a universal benchmark."
        );
    }

    private BenchmarkResult measure(
            String operation,
            int warmupIterations,
            int measuredIterations,
            int batchSize,
            LongSupplier operationSupplier) {

        for (int index = 0; index < warmupIterations; index++) {
            operationSupplier.getAsLong();
        }
        double[] samples = new double[measuredIterations];
        long totalOperations = 0L;
        long totalNanos = 0L;
        for (int index = 0; index < measuredIterations; index++) {
            long start = System.nanoTime();
            totalOperations += operationSupplier.getAsLong();
            long elapsed = System.nanoTime() - start;
            totalNanos += elapsed;
            samples[index] = elapsed / 1_000_000.0;
        }
        Arrays.sort(samples);
        double elapsedMs = totalNanos / 1_000_000.0;
        return new BenchmarkResult(
                operation,
                warmupIterations,
                measuredIterations,
                batchSize,
                totalOperations,
                elapsedMs,
                elapsedMs == 0.0 ? 0.0 : totalOperations / (elapsedMs / 1000.0),
                percentile(samples, 0.50),
                percentile(samples, 0.95),
                percentile(samples, 0.99)
        );
    }

    private long individualSetGet(String prefix, int batchSize) {
        for (int index = 0; index < batchSize; index++) {
            String key = prefix + ":individual:" + index;
            redisTemplate.opsForValue().set(key, "value-" + index);
            redisTemplate.opsForValue().get(key);
        }
        return batchSize * 2L;
    }

    private long pipelineSetGet(String prefix, int batchSize) {
        redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
            StringRedisConnection redis = new DefaultStringRedisConnection(connection);
            for (int index = 0; index < batchSize; index++) {
                String key = prefix + ":pipeline:" + index;
                redis.set(key, "value-" + index);
                redis.get(key);
            }
            return null;
        });
        return batchSize * 2L;
    }

    private long msetMget(String prefix, int batchSize) {
        Map<String, String> values = new LinkedHashMap<>();
        List<String> keys = new ArrayList<>();
        for (int index = 0; index < batchSize; index++) {
            String key = prefix + ":mset:" + index;
            keys.add(key);
            values.put(key, "value-" + index);
        }
        redisTemplate.opsForValue().multiSet(values);
        redisTemplate.opsForValue().multiGet(keys);
        return batchSize * 2L;
    }

    private long luaIncrement(String prefix) {
        redisTemplate.execute(INCREMENT_SCRIPT, List.of(prefix + ":lua"));
        return 1L;
    }

    private double percentile(double[] samples, double percentile) {
        if (samples.length == 0) {
            return 0.0;
        }
        int index = (int) Math.ceil(percentile * samples.length) - 1;
        return samples[Math.max(0, Math.min(index, samples.length - 1))];
    }

    private void validate(String prefix, int warmup, int measured, int batchSize) {
        if (prefix == null || prefix.isBlank()) {
            throw new IllegalArgumentException("Benchmark key prefix must not be blank");
        }
        if (warmup < 0 || warmup > 100_000 || measured <= 0 || measured > 100_000) {
            throw new IllegalArgumentException("Warmup must be 0..100000 and measured iterations 1..100000");
        }
        if (batchSize <= 0 || batchSize > 10_000) {
            throw new IllegalArgumentException("Batch size must be between 1 and 10000");
        }
    }
}
