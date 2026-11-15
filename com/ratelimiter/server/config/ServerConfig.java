package com.ratelimiter.server.config;

import com.ratelimiter.shared.util.ConfigGuard;

/** Immutable server configuration; validated at build() time so mis-config is caught before start. */
public final class ServerConfig {

    public final int port;
    public final int socketTimeoutMs;
    public final int maxRequestsPerWindow;
    public final long windowMs;
    public final int backlogSize;
    public final long serverRunDurationMs;

    private ServerConfig(Builder b) {
        this.port = b.port;
        this.socketTimeoutMs = b.socketTimeoutMs;
        this.maxRequestsPerWindow = b.maxRequestsPerWindow;
        this.windowMs = b.windowMs;
        this.backlogSize = b.backlogSize;
        this.serverRunDurationMs = b.serverRunDurationMs;
    }

    public static Builder builder() { return new Builder(); }

    public static final class Builder {
        private int port = 9090;
        private int socketTimeoutMs = 30_000;
        private int maxRequestsPerWindow = 10;
        private long windowMs = 1_000;
        private int backlogSize = 50;
        private long serverRunDurationMs = 60_000;

        public Builder port(int v)                { this.port = v; return this; }
        public Builder socketTimeoutMs(int v)      { this.socketTimeoutMs = v; return this; }
        public Builder maxRequestsPerWindow(int v) { this.maxRequestsPerWindow = v; return this; }
        public Builder windowMs(long v)            { this.windowMs = v; return this; }
        public Builder backlogSize(int v)          { this.backlogSize = v; return this; }
        public Builder serverRunDurationMs(long v) { this.serverRunDurationMs = v; return this; }

        public ServerConfig build() {
            ConfigGuard.require(port >= 1 && port <= 65535, "port must be 1-65535, got: " + port);
            ConfigGuard.require(socketTimeoutMs > 0, "socketTimeoutMs must be > 0");
            ConfigGuard.require(maxRequestsPerWindow > 0, "maxRequestsPerWindow must be > 0");
            ConfigGuard.require(windowMs > 0, "windowMs must be > 0");
            ConfigGuard.require(backlogSize > 0, "backlogSize must be > 0");
            ConfigGuard.require(serverRunDurationMs == -1 || (serverRunDurationMs >= 1 && serverRunDurationMs <= 60_000), "serverRunDurationMs must be -1 (infinite) or 1-60000, got: " + serverRunDurationMs);
            return new ServerConfig(this);
        }
    }
}
