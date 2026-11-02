package com.ratelimiter.server.throttle;

/** Immutable result of a throttle check: allowed, or rejected with retry delay. */
public record ThrottleResult(boolean allowed, long retryAfterMs) {

    public static ThrottleResult allow() {
        return new ThrottleResult(true, 0);
    }

    public static ThrottleResult reject(long retryAfterMs) {
        return new ThrottleResult(false, retryAfterMs);
    }
}
