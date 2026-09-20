package com.redislab.model;

import java.util.Map;

public record StreamEntryResponse(
        String stream,
        String id,
        Map<String, String> fields) {
}
