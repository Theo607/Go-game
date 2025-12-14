package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room {
    private final String roomId;
    private final String roomName;
    private ClientHandler owner;
    private final List<ClientHandler> players;
    private boolean started = false;
    private final int MAX_PLAYERS = 2;
    private final Map<ClientHandler, Color> colors = new HashMap<>();
    private ClientHandler colorChangeRequester = null;

    public Room(String roomId, String roomName, ClientHandler owner) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.owner = owner;
        this.players = new ArrayList<>();
    }

    public synchronized Color getColor(ClientHandler player) {
        return colors.getOrDefault(player, Color.NONE);
    }

    public synchronized boolean pickColor(ClientHandler player, Color color) {
        if (started)
            return false;
        if (colors.get(player) != Color.NONE)
            return false;

        if (colors.containsValue(color))
            return false; // already taken

        colors.put(player, color);

        // Assign remaining color to other player
        for (ClientHandler p : players) {
            if (p != player) {
                colors.put(p, color == Color.BLACK ? Color.WHITE : Color.BLACK);
            }
        }
        return true;
    }

    public synchronized boolean colorsChosen() {
        return colors.values().stream().noneMatch(c -> c == Color.NONE);
    }

    public synchronized boolean join(ClientHandler client) {
        colors.put(client, Color.NONE);
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

    public synchronized List<String> getPlayerNames() {
        return players.stream()
                .map(ClientHandler::getUsername)
                .toList();
    }

    public synchronized boolean canRespondToColorChange(ClientHandler player) {
        return colorChangeRequester != null && player != colorChangeRequester;
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

    public boolean isOwner(ClientHandler client) {
        return owner == client;
    }

    public List<ClientHandler> getPlayers() {
        return players;
    }

    public synchronized void swapColors(ClientHandler a, ClientHandler b) {
        Color temp = colors.get(a);
        colors.put(a, colors.get(b));
        colors.put(b, temp);
    }

    public synchronized void setColorChangeRequester(ClientHandler client) {
        colorChangeRequester = client;
    }

    public synchronized ClientHandler getColorChangeRequester() {
        return colorChangeRequester;
    }

    public synchronized void clearColorChangeRequest() {
        colorChangeRequester = null;
    }

    public synchronized void setOwner(ClientHandler newOwner) {
        this.owner = newOwner;
    }

}
