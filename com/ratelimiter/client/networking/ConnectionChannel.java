package com.ratelimiter.client.networking;

import com.ratelimiter.shared.model.Request;
import com.ratelimiter.shared.model.Response;

import java.io.*;
import java.net.Socket;

final class ConnectionChannel {

    private final ObjectInputStream in;
    private final ObjectOutputStream out;

    private ConnectionChannel(ObjectInputStream in, ObjectOutputStream out) {
        this.in = in;
        this.out = out;
    }

    // Create ObjectOutputStream first (and flush it), then ObjectInputStream, to avoid deadlock
    static ConnectionChannel open(Socket s) throws IOException {
        ObjectOutputStream out = new ObjectOutputStream(new BufferedOutputStream(s.getOutputStream()));
        out.flush();
        ObjectInputStream in = new ObjectInputStream(new BufferedInputStream(s.getInputStream()));
        return new ConnectionChannel(in, out);
    }

    Response exchange(Request request) throws IOException {
        try {
            out.writeObject(request);
            out.flush();
            return (Response) in.readObject();
        } catch (ClassNotFoundException | ClassCastException e) {
            throw new IOException("Unexpected response type from server: " + e.getMessage(), e);
        }
    }
}
