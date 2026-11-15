package com.ratelimiter.server.throttle;


import com.ratelimiter.shared.exception.ConfigException;

import java.util.concurrent.ConcurrentHashMap;

/** Per-client fixed-window rate limiter; each client gets an isolated {@link WindowState}. */
public final class FixedWindowRateLimiter implements RateLimiter {

    private final int maxRequests;
    private final long windowMs;

    private final ConcurrentHashMap<String, WindowState> windows = new ConcurrentHashMap<>();

    public FixedWindowRateLimiter(int maxRequests, long windowMs) {
        if (maxRequests <= 0)
            throw new ConfigException("maxRequests must be > 0, got: " + maxRequests);
        if (windowMs <= 0)
            throw new ConfigException("windowMs must be > 0, got: " + windowMs);
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
    }

    @Override
    public ThrottleResult throttle(String clientId) {
        return windows.computeIfAbsent(clientId, id -> new WindowState(maxRequests, windowMs))
                      .throttle();
    }

    public int trackedClientCount() {
        return windows.size();
    }
}
