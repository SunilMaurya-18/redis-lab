package com.redislab.controller;

import com.redislab.service.RedisStreamService;
import org.springframework.data.redis.connection.stream.MapRecord;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/redis/streams")
public class RedisStreamController {

    private final RedisStreamService streamService;

    public RedisStreamController(
            RedisStreamService streamService) {

        this.streamService = streamService;
    }

    @PostMapping("/add")
    public String addEvent(
            @RequestParam String stream,
            @RequestParam String eventType,
            @RequestParam String payload) {

        return streamService.addEvent(
                stream,
                eventType,
                payload
        );
    }

    @GetMapping
    public List<MapRecord<String, Object, Object>> readAll(
            @RequestParam String stream) {

        return streamService.readAll(stream);
    }

    @GetMapping("/latest")
    public List<MapRecord<String, Object, Object>> readLatest(
            @RequestParam String stream,
            @RequestParam long count) {

        return streamService.readLatest(
                stream,
                count
        );
    }

    @GetMapping("/from")
    public List<MapRecord<String, Object, Object>> readFromId(
            @RequestParam String stream,
            @RequestParam String startId) {

        return streamService.readFromId(
                stream,
                startId
        );
    }

    @GetMapping("/size")
    public Long size(
            @RequestParam String stream) {

        return streamService.size(stream);
    }

    @DeleteMapping("/entry")
    public Long delete(
            @RequestParam String stream,
            @RequestParam String recordId) {

        return streamService.delete(
                stream,
                recordId
        );
    }

    @PostMapping("/trim")
    public Long trim(
            @RequestParam String stream,
            @RequestParam long maxEntries) {

        return streamService.trim(
                stream,
                maxEntries
        );
    }
}