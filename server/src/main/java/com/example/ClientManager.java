package com.example;

import java.util.ArrayList;
import java.util.List;
/**
 * Managing clients - adding them to the list, removing them after they disconnect
 */
public class ClientManager {
    private final List<ClientHandler> clients;

    public ClientManager() {
        clients = new ArrayList<>();
    }

    public synchronized void addClient(ClientHandler client) {
        clients.add(client);
        System.out.println("Client added. Total clients: " + clients.size());
    }

    public synchronized void removeClient(ClientHandler client) {
        clients.remove(client);
        System.out.println("Client removed. Total clients: " + clients.size());
    }

    public synchronized int getClientCount() {
        return clients.size();
    }
}
