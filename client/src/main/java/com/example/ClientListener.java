package com.example;

import java.io.ObjectInputStream;

public class ClientListener implements Runnable {
    private final ObjectInputStream in;
    private final ClientRequestHandler requestHandler;

    public ClientListener(ObjectInputStream in, ClientRequestHandler requestHandler) {
        this.in = in;
        this.requestHandler = requestHandler;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof ServerRequest request) {
                    requestHandler.handleRequest(request);
                }
            }
        } catch (Exception e) {
            System.out.println("Server connection lost.");
        }
    }
}
