package com.ratelimiter.shared.model;

import java.io.Serializable;

/** Immutable decoded client request. */
public record Request(String clientId, int requestId, Type requestType, long timestampMs) implements Serializable {

    private static final long serialVersionUID = 1L;

    public enum Type {
        GET_TOTAL_REQUESTS,
        GET_TOP_CLIENTS,
        GET_AVERAGE_LATENCY
    }
}
