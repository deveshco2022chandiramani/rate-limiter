package com.ratelimiter.server.networking;

import com.ratelimiter.server.pipeline.RequestProcessor;
import com.ratelimiter.shared.model.Request;
import com.ratelimiter.shared.model.Response;

import java.io.*;
import java.net.Socket;
import java.net.SocketTimeoutException;

/** Handles one client connection: reads Requests, delegates to {@link RequestProcessor}, writes Responses. */
final class ClientHandler {

    private final Socket socket;
    private final int socketTimeoutMs;
    private final RequestProcessor handler;

    ClientHandler(Socket socket, int socketTimeoutMs, RequestProcessor handler) {
        this.socket = socket;
        this.socketTimeoutMs = socketTimeoutMs;
        this.handler = handler;
    }

    void run() {
        try (Socket s = socket) {
            s.setSoTimeout(socketTimeoutMs);
            // ObjectOutputStream must be created and flushed before ObjectInputStream to avoid deadlock
            ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
            out.flush();
            ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
            serve(in, out);
        } catch (IOException ignored) {
        } catch (Exception e) {
            System.err.println("[ClientHandler] Unexpected error: " + e.getMessage());
        }
    }

    private void serve(ObjectInputStream in, ObjectOutputStream out) throws IOException {
        while (!socket.isClosed()) {
            try {
                Request request = readRequest(in);
                out.writeObject(handler.handle(request));
                out.flush();
            } catch (SocketTimeoutException | EOFException e) {
                break;
            }
        }
    }

    private Request readRequest(ObjectInputStream in) throws IOException {
        try {
            return (Request) in.readObject();
        } catch (ClassNotFoundException | ClassCastException e) {
            System.err.println("[Server] Protocol violation from " + socket.getRemoteSocketAddress() + ": " + e.getMessage());
            throw new IOException("Protocol violation", e);
        }
    }
}
