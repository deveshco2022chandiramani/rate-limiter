package com.ratelimiter.server.networking;

import com.ratelimiter.server.config.ServerConfig;
import com.ratelimiter.server.pipeline.RequestProcessor;
import com.ratelimiter.shared.model.Response;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;

/** Accept loop: spawns one virtual thread per connection. */
public final class Server implements AutoCloseable {

    private final ServerConfig config;
    private final RequestProcessor handler;
    private final ServerSocket serverSocket;

    private final AtomicBoolean running = new AtomicBoolean(false);
    private final AtomicInteger activeConnections = new AtomicInteger(0);
    private final AtomicLong totalConnectionCount = new AtomicLong(0);

    private Server(ServerConfig config, RequestProcessor handler) throws IOException {
        this.config = config;
        this.handler = handler;
        this.serverSocket = new ServerSocket(config.port, config.backlogSize);
        this.serverSocket.setSoTimeout(1_000);
    }

    public static Server create(ServerConfig config) throws IOException {
        return new Server(config, req -> Response.ok(req.requestId(), "<stub for " + req.requestType() + ">"));
    }

    public void start() {
        running.set(true);
        System.out.println("[Server] Listening on port " + config.port);
        acceptLoop();
        System.out.println("[Server] Stopped.");
    }

    public void stop() {
        running.set(false);
        try {
            if (!serverSocket.isClosed()) serverSocket.close();
        } catch (IOException e) {
            System.err.println("[Server] Error closing server socket: " + e.getMessage());
        }
    }

    @Override
    public void close() { stop(); }

    public int activeConnections() { return activeConnections.get(); }

    private void acceptLoop() {
        long deadline = config.serverRunDurationMs == -1
            ? Long.MAX_VALUE
            : System.currentTimeMillis() + config.serverRunDurationMs;
        while (running.get() && System.currentTimeMillis() < deadline) {
            Socket socket = tryAccept();
            if (socket != null) {
                long id = totalConnectionCount.incrementAndGet();
                activeConnections.incrementAndGet();
                ClientHandler ch = new ClientHandler(socket, config.socketTimeoutMs, handler);
                Thread.ofVirtual().name("client-" + id).start(() -> {
                    try { ch.run(); }
                    finally { activeConnections.decrementAndGet(); }
                });
            }
        }
        if (running.get()) stop();
    }

    private Socket tryAccept() {
        try {
            return serverSocket.accept();
        } catch (java.net.SocketTimeoutException e) {
            return null;
        } catch (IOException e) {
            if (!serverSocket.isClosed()) System.err.println("[Server] Accept error: " + e.getMessage());
            return null;
        }
    }
}
