package com.example;

import java.util.ArrayList;
import java.util.List;

public class Room {
    private final String roomId;
    private final String roomName;
    private final ClientHandler owner;
    private final List<ClientHandler> players;
    private boolean started = false;
    private final int MAX_PLAYERS = 2;

    public Room(String roomId, String roomName, ClientHandler owner) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.owner = owner;
        this.players = new ArrayList<>();
        this.players.add(owner);
    }

    public synchronized boolean join(ClientHandler client) {
        if (players.size() >= MAX_PLAYERS || started)
            return false;
        players.add(client);
        return true;
    }

    public synchronized void leave(ClientHandler client) {
        players.remove(client);
        if (client.getCurrentRoom() == this) {
            client.setCurrentRoom(null);
        }
    }

    public synchronized void start() {
        if (!started && players.size() == MAX_PLAYERS) {
            started = true;
        }
    }

    public synchronized void broadcast(ServerRequest request) {
        for (ClientHandler player : players) {
            player.sendRequest(request);
        }
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    public ClientHandler getOwner() {
        return owner;
    }

    public boolean isStarted() {
        return started;
    }

    public List<ClientHandler> getPlayers() {
        return players;
    }
}
