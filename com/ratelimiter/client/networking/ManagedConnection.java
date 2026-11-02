package com.ratelimiter.client.networking;

import com.ratelimiter.shared.model.Request;
import com.ratelimiter.shared.model.Response;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

final class ManagedConnection {

    private final String host;
    private final int port;
    private final int connectTimeoutMs;
    private final int readTimeoutMs;

    private Socket persistentSocket;
    private ConnectionChannel persistentChannel;

    ManagedConnection(String host, int port, int connectTimeoutMs, int readTimeoutMs) {
        this.host = host;
        this.port = port;
        this.connectTimeoutMs = connectTimeoutMs;
        this.readTimeoutMs = readTimeoutMs;
    }

    void connect() throws IOException {
        Socket s = openSocket();
        try {
            persistentChannel = ConnectionChannel.open(s);
        } catch (IOException e) {
            try { s.close(); } catch (IOException ignored) {}
            throw e;
        }
        persistentSocket = s;
    }

    void close() {
        if (persistentSocket != null) {
            try { persistentSocket.close(); } catch (IOException ignored) {}
            persistentSocket = null;
            persistentChannel = null;
        }
    }

    // Socket.isClosed() cannot detect half-open connections (peer closed silently), so on IOException
    // we discard the stale channel, re-establish, and retry once - preserving persistent-mode intent.
    Response send(Request request) throws IOException {
        if (persistentChannel != null && !persistentSocket.isClosed()) {
            try {
                return persistentChannel.exchange(request);
            } catch (IOException stale) {
                close();
                connect();
                return persistentChannel.exchange(request);
            }
        }
        try (Socket s = openSocket()) {
            return ConnectionChannel.open(s).exchange(request);
        }
    }

    private Socket openSocket() throws IOException {
        Socket socket = new Socket();
        socket.connect(new InetSocketAddress(host, port), connectTimeoutMs);
        socket.setSoTimeout(readTimeoutMs);
        return socket;
    }
}
