package com.redislab.service;

import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamRecords;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
public class RedisStreamService {

    private final StringRedisTemplate redisTemplate;

    public RedisStreamService(
            StringRedisTemplate redisTemplate) {

        this.redisTemplate = redisTemplate;
    }

    public String addEvent(
            String stream,
            String eventType,
            String payload) {

        validateStream(stream);
        validateValue(eventType, "Event type");
        validateValue(payload, "Payload");

        Map<String, String> fields = new HashMap<>();

        fields.put("eventType", eventType);
        fields.put("payload", payload);

        MapRecord<String, String, String> record =
                StreamRecords.newRecord()
                        .in(stream)
                        .ofMap(fields);

        RecordId recordId =
                redisTemplate.opsForStream()
                        .add(record);

        if (recordId == null) {
            throw new IllegalStateException(
                    "Redis did not return a stream entry ID"
            );
        }

        return recordId.getValue();
    }

    public List<MapRecord<String, Object, Object>> readAll(
            String stream) {

        validateStream(stream);

        return redisTemplate.opsForStream()
                .range(
                        stream,
                        Range.unbounded()
                );
    }

    public List<MapRecord<String, Object, Object>> readLatest(
            String stream,
            long count) {

        validateStream(stream);
        validateCount(count);

        return redisTemplate.opsForStream()
                .reverseRange(
                        stream,
                        Range.unbounded()
                )
                .stream()
                .limit(count)
                .toList();
    }

    public List<MapRecord<String, Object, Object>> readFromId(
            String stream,
            String startId) {

        validateStream(stream);
        validateValue(startId, "Start ID");

        Range<String> range =
                Range.rightOpen(
                        startId,
                        "+"
                );

        return redisTemplate.opsForStream()
                .range(
                        stream,
                        range
                );
    }

    public Long size(String stream) {

        validateStream(stream);

        return redisTemplate.opsForStream()
                .size(stream);
    }

    public Long delete(
            String stream,
            String recordId) {

        validateStream(stream);
        validateValue(recordId, "Record ID");

        return redisTemplate.opsForStream()
                .delete(
                        stream,
                        RecordId.of(recordId)
                );
    }

    public Long trim(
            String stream,
            long maxEntries) {

        validateStream(stream);

        if (maxEntries <= 0) {
            throw new IllegalArgumentException(
                    "Max entries must be greater than zero"
            );
        }

        return redisTemplate.opsForStream()
                .trim(
                        stream,
                        maxEntries,
                        true
                );
    }

    private void validateStream(String stream) {

        if (stream == null || stream.isBlank()) {
            throw new IllegalArgumentException(
                    "Stream must not be blank"
            );
        }
    }

    private void validateValue(
            String value,
            String fieldName) {

        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(
                    fieldName + " must not be blank"
            );
        }
    }

    private void validateCount(long count) {

        if (count <= 0) {
            throw new IllegalArgumentException(
                    "Count must be greater than zero"
            );
        }

        if (count > 10_000) {
            throw new IllegalArgumentException(
                    "Count must not exceed 10000"
            );
        }
    }
}