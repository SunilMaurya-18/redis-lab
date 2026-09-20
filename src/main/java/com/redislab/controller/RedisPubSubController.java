package com.redislab.controller;

import com.redislab.service.RedisPubSubService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/redis/pubsub")
public class RedisPubSubController {
    private final RedisPubSubService pubSubService;
    public RedisPubSubController(
            RedisPubSubService pubSubService) {

        this.pubSubService = pubSubService;
    }
    @PostMapping("/publish")
    public Long publish(
            @RequestParam String channel,
            @RequestParam String message) {

        return pubSubService.publish(
                channel,
                message
        );
    }
    @PostMapping("/publish-event")
    public Long publishEvent(
            @RequestParam String message) {

        return pubSubService.publishEvent(
                message
        );
    }
}