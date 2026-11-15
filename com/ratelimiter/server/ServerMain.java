package com.ratelimiter.server;

import com.ratelimiter.server.config.ServerConfig;
import com.ratelimiter.server.networking.Server;

import java.io.IOException;

/** Composition root: builds config, creates and starts the server. */
public final class ServerMain {

    private ServerMain() {}

    public static void main(String[] args) throws IOException {
        ServerConfig config = ServerConfig.builder()
            .port(9090)
            .maxRequestsPerWindow(10)
            .windowMs(1_000)
            .socketTimeoutMs(30_000)
            .serverRunDurationMs(60_000L)
            .build();

        Server.create(config).start();
    }
}
