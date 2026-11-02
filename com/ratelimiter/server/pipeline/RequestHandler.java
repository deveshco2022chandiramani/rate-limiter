package com.ratelimiter.server.pipeline;

import com.ratelimiter.server.analytics.AnalyticsService;
import com.ratelimiter.server.store.RequestLog;
import com.ratelimiter.server.throttle.ThrottleResult;
import com.ratelimiter.server.throttle.RateLimiter;
import com.ratelimiter.shared.model.Request;
import com.ratelimiter.shared.model.RequestEvent;
import com.ratelimiter.shared.model.Response;

/** Pipeline: rate-limit → dispatch → record. */
final class RequestHandler implements RequestProcessor {

    private static final long nsPerMs = 1_000_000L;

    private final RateLimiter rateLimiter;
    private final AnalyticsService analytics;
    private final RequestLog store;

    RequestHandler(RateLimiter rateLimiter, AnalyticsService analytics, RequestLog store) {
        this.rateLimiter = rateLimiter;
        this.analytics = analytics;
        this.store = store;
    }

    @Override
    public Response handle(Request request) {
        long startNs = System.nanoTime();
        Response response = process(request);
        long latencyMs = (System.nanoTime() - startNs) / nsPerMs;
        store.record(new RequestEvent(
            request.requestId(),
            request.clientId(),
            request.requestType().name(),
            System.currentTimeMillis(),
            latencyMs,
            response.status()));
        return response;
    }

    private Response process(Request request) {
        ThrottleResult rl = rateLimiter.throttle(request.clientId());
        if (!rl.allowed())
            return Response.rateLimited(request.requestId(), "Rate limited. Retry after " + rl.retryAfterMs() + "ms");

        return dispatch(request);
    }

    private Response dispatch(Request request) {
        return switch (request.requestType()) {
            case GET_TOTAL_REQUESTS  -> Response.ok(request.requestId(), String.valueOf(analytics.getTotalRequests()));
            case GET_TOP_CLIENTS     -> Response.ok(request.requestId(), analytics.getTopClients().toString());
            case GET_AVERAGE_LATENCY -> Response.ok(request.requestId(), String.format("%.2f ms", analytics.getAverageLatency()));
        };
    }
}
