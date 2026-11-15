package com.ratelimiter.server.pipeline;

import com.ratelimiter.shared.model.Request;
import com.ratelimiter.shared.model.Response;

/** Stub dispatcher - returns placeholder payloads. Extended in steps 08 and 10. */
final class RequestHandler implements RequestProcessor {

    RequestHandler() {}

    @Override
    public Response handle(Request request) {
        return switch (request.requestType()) {
            case GET_TOTAL_REQUESTS  -> Response.ok(request.requestId(), "<stub for GET_TOTAL_REQUESTS>");
            case GET_TOP_CLIENTS     -> Response.ok(request.requestId(), "<stub for GET_TOP_CLIENTS>");
            case GET_AVERAGE_LATENCY -> Response.ok(request.requestId(), "<stub for GET_AVERAGE_LATENCY>");
        };
    }
}
