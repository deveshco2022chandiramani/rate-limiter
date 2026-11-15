package com.ratelimiter.server.throttle;

/** Strategy interface for rate limiting; called before every request dispatch. */
public interface RateLimiter {

    ThrottleResult throttle(String clientId);
}
