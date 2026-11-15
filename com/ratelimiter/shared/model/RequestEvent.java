package com.ratelimiter.shared.model;

/** Immutable record of one completed request stored in the RequestLog for analytics queries. */
public record RequestEvent(
        int requestId,
        String clientId,
        String endpoint,
        long timestampMs,
        long latencyMs,
        RequestStatus status) {}
