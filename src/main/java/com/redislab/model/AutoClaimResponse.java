package com.redislab.model;

import java.util.List;

public record AutoClaimResponse(
        String nextStartId,
        List<StreamEntryResponse> entries,
        List<String> deletedEntryIds) {
}
