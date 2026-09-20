package com.redislab.service;

import com.redislab.model.RedisKeyAnalysis;
import com.redislab.model.RedisKeyInspection;
import com.redislab.model.RedisMemoryReport;
import org.springframework.data.redis.connection.DefaultStringRedisConnection;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.StringRedisConnection;
import org.springframework.data.redis.core.Cursor;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.ScanOptions;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RedisDiagnosticsService {

    private final StringRedisTemplate redisTemplate;

    public RedisDiagnosticsService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public RedisKeyInspection inspect(String key) {
        validateKey(key);
        Object type = redisTemplate.type(key);
        String typeName = type == null ? "none" : type.toString().toLowerCase();
        return new RedisKeyInspection(
                key,
                typeName,
                valueOrMinusOne(redisTemplate.getExpire(key, TimeUnit.SECONDS)),
                number(execute("OBJECT", "IDLETIME", key)),
                number(execute("MEMORY", "USAGE", key)),
                text(execute("OBJECT", "ENCODING", key)),
                preview(key, typeName)
        );
    }

    public RedisMemoryReport memoryReport() {
        Long databaseSize = redisTemplate.execute(RedisConnection::dbSize);
        return new RedisMemoryReport(
                databaseSize == null ? 0L : databaseSize,
                parseAlternatingMap(execute("MEMORY", "STATS")),
                text(execute("INFO", "MEMORY")),
                text(execute("MEMORY", "DOCTOR"))
        );
    }

    public List<String> scan(String pattern, long count) {
        String match = pattern == null || pattern.isBlank() ? "*" : pattern;
        validateCount(count);
        List<String> keys = new ArrayList<>();
        ScanOptions options = ScanOptions.scanOptions()
                .match(match)
                .count(count)
                .build();
        try (Cursor<String> cursor = redisTemplate.scan(options)) {
            cursor.forEachRemaining(keys::add);
        }
        return keys;
    }

    public RedisKeyAnalysis analyze(String pattern, long scanCount, int largestCount) {
        validateCount(scanCount);
        if (largestCount <= 0 || largestCount > 100) {
            throw new IllegalArgumentException("Largest-key count must be between 1 and 100");
        }
        List<RedisKeyInspection> inspections = scan(pattern, scanCount).stream()
                .map(this::inspect)
                .filter(it -> it.memoryBytes() >= 0)
                .toList();
        long total = inspections.stream().mapToLong(RedisKeyInspection::memoryBytes).sum();
        List<RedisKeyInspection> largest = inspections.stream()
                .sorted(Comparator.comparingLong(RedisKeyInspection::memoryBytes).reversed())
                .limit(largestCount)
                .toList();
        return new RedisKeyAnalysis(
                pattern == null || pattern.isBlank() ? "*" : pattern,
                inspections.size(),
                total,
                largest
        );
    }

    private String preview(String key, String type) {
        if (!"string".equals(type)) {
            return "<preview omitted for " + type + ">";
        }
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            return null;
        }
        return value.length() <= 256 ? value : value.substring(0, 256) + "...";
    }

    private Object execute(String command, String... args) {
        return redisTemplate.execute((RedisCallback<Object>) connection -> {
            StringRedisConnection stringConnection = new DefaultStringRedisConnection(connection);
            return stringConnection.execute(command, args);
        });
    }

    private Map<String, Object> parseAlternatingMap(Object raw) {
        Map<String, Object> result = new LinkedHashMap<>();
        if (!(raw instanceof List<?> values)) {
            return result;
        }
        for (int index = 0; index + 1 < values.size(); index += 2) {
            result.put(text(values.get(index)), values.get(index + 1));
        }
        return result;
    }

    private long number(Object value) {
        if (value instanceof Number number) {
            return number.longValue();
        }
        try {
            return value == null ? -1L : Long.parseLong(text(value));
        } catch (NumberFormatException ignored) {
            return -1L;
        }
    }

    private String text(Object value) {
        if (value instanceof byte[] bytes) {
            return new String(bytes, java.nio.charset.StandardCharsets.UTF_8);
        }
        return value == null ? null : value.toString();
    }

    private long valueOrMinusOne(Long value) {
        return value == null ? -1L : value;
    }

    private void validateKey(String key) {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Redis key must not be blank");
        }
    }

    private void validateCount(long count) {
        if (count <= 0 || count > 100_000) {
            throw new IllegalArgumentException("SCAN count must be between 1 and 100000");
        }
    }
}
