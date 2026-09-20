package com.redislab.model;

public record ConsumerGroupResponse(
        String name,
        long consumerCount,
        long pendingCount,
        String lastDeliveredId) {
}
