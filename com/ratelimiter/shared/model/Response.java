package com.ratelimiter.shared.model;

import java.io.Serializable;

/** Immutable server response; compact constructor normalises null payload to "" to avoid null-checks. */
public record Response(int requestId, RequestStatus status, String payload) implements Serializable {

    private static final long serialVersionUID = 1L;

    public Response {
        if (payload == null) payload = "";
    }

    public static Response ok(int requestId, String payload) {
        return new Response(requestId, RequestStatus.SUCCESS, payload);
    }

    public static Response rateLimited(int requestId, String retryMsg) {
        return new Response(requestId, RequestStatus.RATE_LIMITED, retryMsg);
    }
}
