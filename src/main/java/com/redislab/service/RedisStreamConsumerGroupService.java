package com.redislab.service;

import com.redislab.model.AutoClaimResponse;
import com.redislab.model.ConsumerGroupResponse;
import com.redislab.model.ConsumerInfoResponse;
import com.redislab.model.PendingMessageResponse;
import com.redislab.model.PendingSummaryResponse;
import com.redislab.model.StreamEntryResponse;
import org.springframework.data.domain.Range;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.connection.stream.Consumer;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.data.redis.connection.stream.PendingMessage;
import org.springframework.data.redis.connection.stream.PendingMessages;
import org.springframework.data.redis.connection.stream.PendingMessagesSummary;
import org.springframework.data.redis.connection.stream.ReadOffset;
import org.springframework.data.redis.connection.stream.RecordId;
import org.springframework.data.redis.connection.stream.StreamInfo;
import org.springframework.data.redis.connection.stream.StreamOffset;
import org.springframework.data.redis.connection.stream.StreamReadOptions;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.StreamOperations;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Phase 13 implementation of Redis Streams consumer groups.
 *
 * <p>The normal group operations use the typed Spring Data Redis
 * {@link StreamOperations} API. XAUTOCLAIM is intentionally executed through
 * the lower-level Redis connection because Spring Data Redis 4.1.1 exposes
 * XCLAIM but has no synchronous StreamOperations method for XAUTOCLAIM.</p>
 */
@Service
public class RedisStreamConsumerGroupService {

    private static final int MAX_COUNT = 1_000;
    private static final long MAX_BLOCK_MS = 60_000;

    private final StringRedisTemplate redisTemplate;

    public RedisStreamConsumerGroupService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    /**
     * Creates a group. Spring Data Redis sends XGROUP CREATE with MKSTREAM,
     * so the stream may not exist yet.
     */
    public String createGroup(String stream, String group, String startId) {
        validateStream(stream);
        validateName(group, "Group");
        validateStartId(startId);

        String result = operations().createGroup(
                stream,
                toReadOffset(startId),
                group
        );

        return result == null ? "OK" : result;
    }

    public String addEvent(String stream, String eventType, String payload) {
        validateStream(stream);
        validateValue(eventType, "Event type");
        validateValue(payload, "Payload");

        Map<String, String> fields = new LinkedHashMap<>();
        fields.put("eventType", eventType);
        fields.put("payload", payload);

        RecordId id = operations().add(stream, fields);
        if (id == null) {
            throw new IllegalStateException("Redis did not return a stream entry ID");
        }
        return id.getValue();
    }

    /**
     * Reads new messages with XREADGROUP ... STREAMS stream > by default.
     * Passing 0-0 reads this consumer's pending history instead.
     */
    public List<StreamEntryResponse> read(
            String stream,
            String group,
            String consumer,
            String offset,
            long count,
            long blockMs) {

        validateStream(stream);
        validateName(group, "Group");
        validateName(consumer, "Consumer");
        validateStartId(offset);
        validateCount(count);
        validateBlockMs(blockMs);

        StreamReadOptions options = StreamReadOptions.empty().count(count);
        if (blockMs > 0) {
            options = options.block(Duration.ofMillis(blockMs));
        }

        List<MapRecord<String, String, String>> records = operations().read(
                Consumer.from(group, consumer),
                options,
                StreamOffset.create(stream, toReadOffset(offset))
        );

        return records == null
                ? List.of()
                : records.stream().map(this::toEntryResponse).toList();
    }

    /**
     * Acknowledges entries using XACK. The return value is the Long count of
     * entries newly removed from the Pending Entries List.
     */
    public long acknowledge(String stream, String group, List<String> recordIds) {
        validateStream(stream);
        validateName(group, "Group");
        List<String> ids = validateRecordIds(recordIds);

        Long acknowledged = operations().acknowledge(
                stream,
                group,
                ids.toArray(String[]::new)
        );
        return acknowledged == null ? 0L : acknowledged;
    }

    public PendingSummaryResponse pendingSummary(String stream, String group) {
        validateStream(stream);
        validateName(group, "Group");

        PendingMessagesSummary summary = operations().pending(stream, group);
        return new PendingSummaryResponse(
                summary.getGroupName(),
                summary.getTotalPendingMessages(),
                summary.minMessageId(),
                summary.maxMessageId(),
                Map.copyOf(summary.getPendingMessagesPerConsumer())
        );
    }

    public List<PendingMessageResponse> pending(
            String stream,
            String group,
            String consumer,
            String startId,
            String endId,
            long count,
            long minIdleMs) {

        validateStream(stream);
        validateName(group, "Group");
        validateStartId(startId);
        validateStartId(endId);
        validateCount(count);
        if (minIdleMs < 0) {
            throw new IllegalArgumentException("Minimum idle time must not be negative");
        }

        Range<String> range = Range.closed(startId, endId);
        PendingMessages pending;
        if (consumer == null || consumer.isBlank()) {
            pending = minIdleMs == 0
                    ? operations().pending(stream, group, range, count)
                    : operations().pending(
                            stream,
                            group,
                            range,
                            count,
                            Duration.ofMillis(minIdleMs)
                    );
        } else {
            validateName(consumer, "Consumer");
            Consumer streamConsumer = Consumer.from(group, consumer);
            pending = minIdleMs == 0
                    ? operations().pending(stream, streamConsumer, range, count)
                    : operations().pending(
                            stream,
                            streamConsumer,
                            range,
                            count,
                            Duration.ofMillis(minIdleMs)
                    );
        }

        return pending == null
                ? List.of()
                : pending.stream().map(this::toPendingResponse).toList();
    }

    /**
     * Claims specific pending IDs with XCLAIM. Redis does not need the old
     * consumer name; ownership moves by group, ID, and new consumer.
     */
    public List<StreamEntryResponse> claim(
            String stream,
            String group,
            String newConsumer,
            long minIdleMs,
            List<String> recordIds) {

        validateStream(stream);
        validateName(group, "Group");
        validateName(newConsumer, "New consumer");
        validateIdleMs(minIdleMs);
        List<String> ids = validateRecordIds(recordIds);

        List<RecordId> redisIds = ids.stream().map(RecordId::of).toList();
        List<MapRecord<String, String, String>> claimed = operations().claim(
                stream,
                group,
                newConsumer,
                Duration.ofMillis(minIdleMs),
                redisIds.toArray(RecordId[]::new)
        );

        return claimed == null
                ? List.of()
                : claimed.stream().map(this::toEntryResponse).toList();
    }

    /**
     * Runs XAUTOCLAIM and returns the next cursor plus claimed entries. The
     * cursor must be sent back as startId on the next call to scan the rest of
     * the Pending Entries List.
     */
    public AutoClaimResponse autoClaim(
            String stream,
            String group,
            String newConsumer,
            long minIdleMs,
            String startId,
            long count) {

        validateStream(stream);
        validateName(group, "Group");
        validateName(newConsumer, "New consumer");
        validateIdleMs(minIdleMs);
        validateStartId(startId);
        validateCount(count);

        Object raw = redisTemplate.execute((RedisCallback<Object>) connection ->
                executeAutoClaim(
                        connection,
                        stream,
                        group,
                        newConsumer,
                        minIdleMs,
                        startId,
                        count
                )
        );

        return parseAutoClaim(raw, stream);
    }

    public List<ConsumerGroupResponse> groups(String stream) {
        validateStream(stream);
        StreamInfo.XInfoGroups groups = operations().groups(stream);
        if (groups == null) {
            return List.of();
        }
        return groups.stream()
                .map(group -> new ConsumerGroupResponse(
                        group.groupName(),
                        valueOrZero(group.consumerCount()),
                        valueOrZero(group.pendingCount()),
                        group.lastDeliveredId()
                ))
                .toList();
    }

    public List<ConsumerInfoResponse> consumers(String stream, String group) {
        validateStream(stream);
        validateName(group, "Group");
        StreamInfo.XInfoConsumers consumers = operations().consumers(stream, group);
        if (consumers == null) {
            return List.of();
        }
        return consumers.stream()
                .map(consumer -> new ConsumerInfoResponse(
                        consumer.groupName(),
                        consumer.consumerName(),
                        consumer.idleTimeMs(),
                        consumer.pendingCount()
                ))
                .toList();
    }

    public boolean deleteConsumer(String stream, String group, String consumer) {
        validateStream(stream);
        validateName(group, "Group");
        validateName(consumer, "Consumer");

        Boolean deleted = operations().deleteConsumer(
                stream,
                Consumer.from(group, consumer)
        );
        return Boolean.TRUE.equals(deleted);
    }

    public boolean destroyGroup(String stream, String group) {
        validateStream(stream);
        validateName(group, "Group");
        return Boolean.TRUE.equals(operations().destroyGroup(stream, group));
    }

    private Object executeAutoClaim(
            RedisConnection connection,
            String stream,
            String group,
            String newConsumer,
            long minIdleMs,
            String startId,
            long count) {

        List<String> arguments = new ArrayList<>(8);
        arguments.add(stream);
        arguments.add(group);
        arguments.add(newConsumer);
        arguments.add(Long.toString(minIdleMs));
        arguments.add(startId);
        arguments.add("COUNT");
        arguments.add(Long.toString(count));

        byte[][] rawArguments = arguments.stream()
                .map(value -> value.getBytes(StandardCharsets.UTF_8))
                .toArray(byte[][]::new);
        return connection.execute("XAUTOCLAIM", rawArguments);
    }

    private AutoClaimResponse parseAutoClaim(Object raw, String stream) {
        if (raw == null) {
            return new AutoClaimResponse("0-0", List.of(), List.of());
        }

        List<?> response = asList(raw, "XAUTOCLAIM response");
        if (response.size() < 2) {
            throw new IllegalStateException("Redis returned an invalid XAUTOCLAIM response");
        }

        String nextStartId = text(response.get(0));
        List<?> rawEntries = asList(response.get(1), "XAUTOCLAIM entries");
        List<StreamEntryResponse> entries = new ArrayList<>();
        for (Object rawEntry : rawEntries) {
            List<?> entry = asList(rawEntry, "XAUTOCLAIM entry");
            if (entry.size() < 2) {
                throw new IllegalStateException("Redis returned an invalid stream entry");
            }
            entries.add(new StreamEntryResponse(
                    stream,
                    text(entry.get(0)),
                    parseFields(entry.get(1))
            ));
        }

        List<String> deletedIds = response.size() >= 3
                ? asList(response.get(2), "XAUTOCLAIM deleted IDs").stream()
                .map(this::text)
                .toList()
                : List.of();

        return new AutoClaimResponse(
                nextStartId,
                List.copyOf(entries),
                deletedIds
        );
    }

    private Map<String, String> parseFields(Object rawFields) {
        List<?> fields = asList(rawFields, "stream fields");
        if (fields.size() % 2 != 0) {
            throw new IllegalStateException("Redis returned an odd number of stream field values");
        }

        Map<String, String> result = new LinkedHashMap<>();
        for (int index = 0; index < fields.size(); index += 2) {
            result.put(text(fields.get(index)), text(fields.get(index + 1)));
        }
        return result;
    }

    private StreamEntryResponse toEntryResponse(MapRecord<String, String, String> record) {
        return new StreamEntryResponse(
                record.getStream(),
                record.getId().getValue(),
                Map.copyOf(record.getValue())
        );
    }

    private PendingMessageResponse toPendingResponse(PendingMessage message) {
        return new PendingMessageResponse(
                message.getIdAsString(),
                message.getGroupName(),
                message.getConsumerName(),
                message.getElapsedTimeSinceLastDelivery().toMillis(),
                message.getTotalDeliveryCount()
        );
    }

    private StreamOperations<String, String, String> operations() {
        return redisTemplate.opsForStream();
    }

    private ReadOffset toReadOffset(String offset) {
        return ">".equals(offset)
                ? ReadOffset.lastConsumed()
                : ReadOffset.from(offset);
    }

    private List<?> asList(Object value, String description) {
        if (value instanceof List<?> list) {
            return list;
        }
        throw new IllegalStateException(
                "Redis returned an invalid " + description + ": " + value
        );
    }

    private String text(Object value) {
        if (value instanceof byte[] bytes) {
            return new String(bytes, StandardCharsets.UTF_8);
        }
        if (value instanceof String string) {
            return string;
        }
        if (value instanceof Number number) {
            return number.toString();
        }
        throw new IllegalStateException("Redis returned an unsupported value: " + value);
    }

    private List<String> validateRecordIds(List<String> recordIds) {
        if (recordIds == null || recordIds.isEmpty()) {
            throw new IllegalArgumentException("At least one record ID is required");
        }
        if (recordIds.size() > MAX_COUNT) {
            throw new IllegalArgumentException("A maximum of " + MAX_COUNT + " record IDs is supported");
        }
        if (recordIds.stream().anyMatch(id -> id == null || id.isBlank())) {
            throw new IllegalArgumentException("Record IDs must not be blank");
        }
        return recordIds;
    }

    private void validateStream(String stream) {
        validateName(stream, "Stream");
    }

    private void validateName(String value, String label) {
        validateValue(value, label);
    }

    private void validateValue(String value, String label) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(label + " must not be blank");
        }
    }

    private void validateStartId(String id) {
        validateValue(id, "Stream ID");
    }

    private void validateCount(long count) {
        if (count <= 0 || count > MAX_COUNT) {
            throw new IllegalArgumentException("Count must be between 1 and " + MAX_COUNT);
        }
    }

    private void validateBlockMs(long blockMs) {
        if (blockMs < 0 || blockMs > MAX_BLOCK_MS) {
            throw new IllegalArgumentException("Block timeout must be between 0 and " + MAX_BLOCK_MS + " ms");
        }
    }

    private void validateIdleMs(long idleMs) {
        if (idleMs < 0) {
            throw new IllegalArgumentException("Minimum idle time must not be negative");
        }
    }

    private long valueOrZero(Long value) {
        return value == null ? 0L : value;
    }
}
