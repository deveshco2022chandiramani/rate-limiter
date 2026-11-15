package com.ratelimiter.server.throttle;

import java.util.concurrent.atomic.AtomicReference;

/** Per-client fixed-window state: lock-free CAS loop over an immutable WindowSnapshot. */
final class WindowState {

    private record WindowSnapshot(int count, long windowStart) {}

    private final int maxRequests;
    private final long windowMs;
    private final AtomicReference<WindowSnapshot> state;

    WindowState(int maxRequests, long windowMs) {
        this.maxRequests = maxRequests;
        this.windowMs = windowMs;
        this.state = new AtomicReference<>(new WindowSnapshot(0, System.currentTimeMillis()));
    }

    ThrottleResult throttle() {
        while (true) {
            WindowSnapshot current = state.get();
            long now = System.currentTimeMillis();

            WindowSnapshot next;
            if (now - current.windowStart() >= windowMs) {
                // Window expired: claim the first slot of the new window
                next = new WindowSnapshot(1, now);
                if (state.compareAndSet(current, next)) return ThrottleResult.allow();
            } else if (current.count() < maxRequests) {
                // Window still active, slot available
                next = new WindowSnapshot(current.count() + 1, current.windowStart());
                if (state.compareAndSet(current, next)) return ThrottleResult.allow();
            } else {
                // Window full - no CAS needed, just reject
                return ThrottleResult.reject(windowMs - (now - current.windowStart()));
            }
            // CAS lost the race - another thread updated state first; retry with fresh snapshot
        }
    }
}
