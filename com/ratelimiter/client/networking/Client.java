package com.ratelimiter.client.networking;

import com.ratelimiter.shared.model.Request;
import com.ratelimiter.shared.model.Response;

import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

/** Owns client identity and constructs typed requests; delegates connection management to ManagedConnection. */
public final class Client implements AutoCloseable {

    private static final int defaultConnectTimeoutMs = 5_000;
    private static final int defaultReadTimeoutMs = 10_000;

    private final String clientId;
    private final AtomicInteger requestCounter = new AtomicInteger(0);
    private final ManagedConnection conn;

    public Client(String host, int port, String clientId) {
        this(host, port, clientId, defaultConnectTimeoutMs, defaultReadTimeoutMs);
    }

    public Client(String host, int port, String clientId, int connectTimeoutMs, int readTimeoutMs) {
        this.clientId = clientId;
        this.conn = new ManagedConnection(host, port, connectTimeoutMs, readTimeoutMs);
    }

    public Client connect() throws IOException {
        conn.connect();
        return this;
    }

    @Override
    public void close() {
        conn.close();
    }

    public Response send(Request.Type type) throws IOException {
        return conn.send(buildRequest(type));
    }

    private Request buildRequest(Request.Type type) {
        return new Request(clientId, requestCounter.incrementAndGet(), type, System.currentTimeMillis());
    }
}
