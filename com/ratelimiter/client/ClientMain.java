package com.ratelimiter.client;

import com.ratelimiter.client.networking.Client;
import com.ratelimiter.shared.model.Request;
import com.ratelimiter.shared.model.Response;

import java.io.IOException;
import java.net.ConnectException;
import java.net.SocketTimeoutException;

/** Entry point: sends a burst of requests and prints each response status. */
public final class ClientMain {

    private ClientMain() {}

    public static void main(String[] args) {
        String host = "localhost";
        int port = 9090;
        String clientId = args.length > 0 ? args[0] : "demo-client";
        int count = args.length > 1 ? Integer.parseInt(args[1]) : 5;

        System.out.printf("=== Sending %d GET_TOTAL_REQUESTS as '%s' to %s:%d ===%n", count, clientId, host, port);
        try (Client client = new Client(host, port, clientId).connect()) {
            for (int i = 1; i <= count; i++) {
                try {
                    Response resp = client.send(Request.Type.GET_TOTAL_REQUESTS);
                    System.out.printf("%-14s req#%d -> %s%n", "[" + resp.status() + "]", i, resp.payload());
                } catch (SocketTimeoutException e) {
                    System.err.println("[Error] req#" + i + " timed out - server may be busy.");
                } catch (IOException e) {
                    System.err.println("[Error] req#" + i + " failed: " + e.getMessage());
                }
            }
        } catch (ConnectException e) {
            System.err.println("[Error] Cannot connect to " + host + ":" + port + " - is the server running?");
        } catch (IOException e) {
            System.err.println("[Error] Connection failed: " + e.getMessage());
        }
    }
}
