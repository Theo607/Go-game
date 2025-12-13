package com.example;
import java.io.ObjectInputStream;

public class ClientListener implements Runnable {
    private final ObjectInputStream in;
    private final Client client;

    public ClientListener(ObjectInputStream in, Client client) {
        this.in = in;
        this.client = client;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();

                if (obj instanceof ServerRequest request) {
                    client.handleServerRequest(request);
                }
            }
        } catch (Exception e) {
            System.out.println("Server connection lost.");
        }
    }
}
