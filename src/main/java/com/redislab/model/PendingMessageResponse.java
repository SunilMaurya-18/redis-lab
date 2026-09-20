package com.redislab.model;

public record PendingMessageResponse(
        String id,
        String group,
        String consumer,
        long idleMs,
        long deliveryCount) {
}
