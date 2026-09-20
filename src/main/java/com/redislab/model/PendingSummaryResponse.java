package com.redislab.model;

import java.util.Map;

public record PendingSummaryResponse(
        String group,
        long totalPending,
        String minId,
        String maxId,
        Map<String, Long> pendingByConsumer) {
}
