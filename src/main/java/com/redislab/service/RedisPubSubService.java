package com.redislab.service;

import com.redislab.config.RedisPubSubConfig;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

@Service
public class RedisPubSubService {
    private final StringRedisTemplate redisTemplate;
    public RedisPubSubService(
            StringRedisTemplate redisTemplate) {

        this.redisTemplate = redisTemplate;
    }
    public Long publish(
            String channel,
            String message) {

        validateChannel(channel);
        validateMessage(message);

        return redisTemplate.convertAndSend(
                channel,
                message
        );
    }
    public Long publishEvent(
            String message) {

        validateMessage(message);

        return redisTemplate.convertAndSend(
                RedisPubSubConfig.EVENT_CHANNEL,
                message
        );
    }
    private void validateChannel(String channel) {

        if (channel == null || channel.isBlank()) {

            throw new IllegalArgumentException(
                    "Channel must not be blank"
            );
        }
    }
    private void validateMessage(String message) {

        if (message == null || message.isBlank()) {

            throw new IllegalArgumentException(
                    "Message must not be blank"
            );
        }
    }
}
