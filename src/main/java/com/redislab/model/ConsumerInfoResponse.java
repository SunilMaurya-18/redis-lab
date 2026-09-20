package com.redislab.model;

public record ConsumerInfoResponse(
        String group,
        String name,
        long idleMs,
        long pendingCount) {
}
