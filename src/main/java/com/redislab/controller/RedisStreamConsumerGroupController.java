package com.redislab.controller;

import com.redislab.model.AutoClaimResponse;
import com.redislab.model.ConsumerGroupResponse;
import com.redislab.model.ConsumerInfoResponse;
import com.redislab.model.PendingMessageResponse;
import com.redislab.model.PendingSummaryResponse;
import com.redislab.model.StreamEntryResponse;
import com.redislab.service.RedisStreamConsumerGroupService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/redis/stream-groups")
public class RedisStreamConsumerGroupController {

    private final RedisStreamConsumerGroupService service;

    public RedisStreamConsumerGroupController(RedisStreamConsumerGroupService service) {
        this.service = service;
    }

    @PostMapping("/create")
    public String createGroup(
            @RequestParam String stream,
            @RequestParam String group,
            @RequestParam(defaultValue = "0-0") String startId) {

        return service.createGroup(stream, group, startId);
    }

    @PostMapping("/add")
    public String addEvent(
            @RequestParam String stream,
            @RequestParam String eventType,
            @RequestParam String payload) {

        return service.addEvent(stream, eventType, payload);
    }

    @GetMapping("/read")
    public List<StreamEntryResponse> read(
            @RequestParam String stream,
            @RequestParam String group,
            @RequestParam String consumer,
            @RequestParam(defaultValue = ">") String offset,
            @RequestParam(defaultValue = "10") long count,
            @RequestParam(defaultValue = "0") long blockMs) {

        return service.read(stream, group, consumer, offset, count, blockMs);
    }

    @PostMapping("/ack")
    public long acknowledge(
            @RequestParam String stream,
            @RequestParam String group,
            @RequestParam List<String> recordIds) {

        return service.acknowledge(stream, group, recordIds);
    }

    @GetMapping("/pending/summary")
    public PendingSummaryResponse pendingSummary(
            @RequestParam String stream,
            @RequestParam String group) {

        return service.pendingSummary(stream, group);
    }

    @GetMapping("/pending")
    public List<PendingMessageResponse> pending(
            @RequestParam String stream,
            @RequestParam String group,
            @RequestParam(required = false) String consumer,
            @RequestParam(defaultValue = "-") String startId,
            @RequestParam(defaultValue = "+") String endId,
            @RequestParam(defaultValue = "100") long count,
            @RequestParam(defaultValue = "0") long minIdleMs) {

        return service.pending(
                stream,
                group,
                consumer,
                startId,
                endId,
                count,
                minIdleMs
        );
    }

    @PostMapping("/claim")
    public List<StreamEntryResponse> claim(
            @RequestParam String stream,
            @RequestParam String group,
            @RequestParam String newConsumer,
            @RequestParam(defaultValue = "60000") long minIdleMs,
            @RequestParam List<String> recordIds) {

        return service.claim(stream, group, newConsumer, minIdleMs, recordIds);
    }

    @PostMapping("/autoclaim")
    public AutoClaimResponse autoClaim(
            @RequestParam String stream,
            @RequestParam String group,
            @RequestParam String newConsumer,
            @RequestParam(defaultValue = "60000") long minIdleMs,
            @RequestParam(defaultValue = "0-0") String startId,
            @RequestParam(defaultValue = "100") long count) {

        return service.autoClaim(
                stream,
                group,
                newConsumer,
                minIdleMs,
                startId,
                count
        );
    }

    @GetMapping("/groups")
    public List<ConsumerGroupResponse> groups(@RequestParam String stream) {
        return service.groups(stream);
    }

    @GetMapping("/consumers")
    public List<ConsumerInfoResponse> consumers(
            @RequestParam String stream,
            @RequestParam String group) {

        return service.consumers(stream, group);
    }

    @DeleteMapping("/consumer")
    public boolean deleteConsumer(
            @RequestParam String stream,
            @RequestParam String group,
            @RequestParam String consumer) {

        return service.deleteConsumer(stream, group, consumer);
    }

    @DeleteMapping("/group")
    public boolean destroyGroup(
            @RequestParam String stream,
            @RequestParam String group) {

        return service.destroyGroup(stream, group);
    }
}
