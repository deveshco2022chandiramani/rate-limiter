package com.ratelimiter.server.pipeline;

import com.ratelimiter.server.throttle.RateLimiter;
import com.ratelimiter.server.throttle.ThrottleResult;
import com.ratelimiter.shared.model.Request;
import com.ratelimiter.shared.model.Response;

/** Rate-limit then dispatch. Analytics and logging added in step 10. */
final class RequestHandler implements RequestProcessor {

    private final RateLimiter rateLimiter;

    RequestHandler() { this.rateLimiter = null; }
    RequestHandler(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public Response handle(Request request) {
        ThrottleResult rl = rateLimiter.throttle(request.clientId());
        if (!rl.allowed())
            return Response.rateLimited(request.requestId(), "Rate limited. Retry after " + rl.retryAfterMs() + "ms");
        return dispatch(request);
    }

    private Response dispatch(Request request) {
        return switch (request.requestType()) {
            case GET_TOTAL_REQUESTS  -> Response.ok(request.requestId(), "<stub for GET_TOTAL_REQUESTS>");
            case GET_TOP_CLIENTS     -> Response.ok(request.requestId(), "<stub for GET_TOP_CLIENTS>");
            case GET_AVERAGE_LATENCY -> Response.ok(request.requestId(), "<stub for GET_AVERAGE_LATENCY>");
        };
    }
}
