package com.ratelimiter.server.pipeline;

import com.ratelimiter.server.analytics.AnalyticsService;
import com.ratelimiter.server.config.ServerConfig;
import com.ratelimiter.server.store.RequestLog;
import com.ratelimiter.server.pipeline.RequestProcessor;
import com.ratelimiter.server.throttle.FixedWindowRateLimiter;

/** Wires the request-handling pipeline, keeping package-private stages hidden from callers. */
public final class Pipeline {

    private Pipeline() {}

    public static RequestProcessor build(ServerConfig config, RequestLog store) {
        return new RequestHandler(
            new FixedWindowRateLimiter(config.maxRequestsPerWindow, config.windowMs),
            new AnalyticsService(store),
            store);
    }
}
