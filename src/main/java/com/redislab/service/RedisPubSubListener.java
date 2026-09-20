package com.redislab.service;

import org.springframework.stereotype.Component;

@Component
public class RedisPubSubListener {

    public void onMessage(
            String message,
            String pattern) {

        System.out.println(
                "[REDIS PUB/SUB] " +
                        "pattern=" + pattern +
                        ", message=" + message
        );
    }
}