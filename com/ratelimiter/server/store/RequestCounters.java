package com.ratelimiter.server.store;

import java.util.concurrent.atomic.LongAdder;

/**
 * Thread-safe O(1) counters backed by LongAdder.
 * Two separate adders (allowed + rejected) let analytics break down outcomes without scanning events.
 */
public final class RequestCounters {

    private final LongAdder allowedRequests = new LongAdder();
    private final LongAdder rejectedRequests = new LongAdder();

    public void incrementAllowed()  { allowedRequests.increment(); }
    public void incrementRejected() { rejectedRequests.increment(); }

    public long allowedRequests()   { return allowedRequests.sum(); }
    public long rejectedRequests()  { return rejectedRequests.sum(); }
    public long totalRequests()     { return allowedRequests.sum() + rejectedRequests.sum(); }
}
