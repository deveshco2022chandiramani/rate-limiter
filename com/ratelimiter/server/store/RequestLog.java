package com.ratelimiter.server.store;

import com.ratelimiter.shared.model.RequestEvent;
import com.ratelimiter.shared.model.RequestStatus;

import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

/** Append-only event log (ConcurrentLinkedQueue) plus O(1) counters delegated to {@link RequestCounters}. */
public final class RequestLog {

    private final ConcurrentLinkedQueue<RequestEvent> events = new ConcurrentLinkedQueue<>();
    private final RequestCounters counters = new RequestCounters();

    // O(1) lock-free append - ConcurrentLinkedQueue is safe for concurrent writes
    public void record(RequestEvent e) {
        events.add(e);
        if (e.status() == RequestStatus.SUCCESS) {
            counters.incrementAllowed();
        } else {
            counters.incrementRejected();
        }
    }

    // Snapshot isolates stream analytics from concurrent writes
    public List<RequestEvent> snapshot() {
        return List.copyOf(events);
    }

    public RequestCounters counters() {
        return counters;
    }
}
