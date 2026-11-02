package com.ratelimiter.server.analytics;

import com.ratelimiter.server.store.RequestLog;
import com.ratelimiter.shared.model.RequestEvent;
import com.ratelimiter.shared.model.RequestStatus;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/** Analytics queries: O(1) counter reads and O(n) stream scans over the event snapshot. */
public final class AnalyticsService {

    private static final int topN = 5;

    private final RequestLog store;

    public AnalyticsService(RequestLog store) {
        this.store = store;
    }

    // O(1) - reads the pre-computed LongAdder counter, no event scan needed
    public long getTotalRequests() {
        return store.counters().totalRequests();
    }

    // O(n) - only SUCCESS events count toward top-clients ranking
    public Map<String, Long> getTopClients() {
        return store.snapshot().stream()
            .filter(e -> e.status() == RequestStatus.SUCCESS)
            .collect(Collectors.groupingBy(RequestEvent::clientId, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<String, Long>comparingByValue().reversed())
            .limit(topN)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (a, b) -> a,
                LinkedHashMap::new));
    }

    // O(n) - mapToLong then average; returns 0 when the store is empty
    public double getAverageLatency() {
        return store.snapshot().stream()
            .mapToLong(RequestEvent::latencyMs)
            .average()
            .orElse(0.0);
    }

    private <K> Map<K, Long> rankByCount(int n, Function<RequestEvent, K> keyExtractor) {
        return store.snapshot().stream()
            .collect(Collectors.groupingBy(keyExtractor, Collectors.counting()))
            .entrySet().stream()
            .sorted(Map.Entry.<K, Long>comparingByValue().reversed())
            .limit(n)
            .collect(Collectors.toMap(
                Map.Entry::getKey,
                Map.Entry::getValue,
                (a, b) -> a,   // unreachable - keys are unique after groupingBy
                LinkedHashMap::new));
    }
}
