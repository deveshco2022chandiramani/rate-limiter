package com.ratelimiter.server.pipeline;

import com.ratelimiter.shared.model.Request;
import com.ratelimiter.shared.model.Response;

/** Boundary between the I/O layer and the business logic layer: receives a Request, returns a Response. */
public interface RequestProcessor {

    Response handle(Request request);
}
