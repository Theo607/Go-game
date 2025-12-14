package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room {
    private final String roomId;
    private final String roomName;
    private ClientHandler owner;
    private final List<ClientHandler> players = new ArrayList<>();
    private boolean started = false;
    private final Map<ClientHandler, Color> colors = new HashMap<>();
    private ClientHandler colorChangeRequester = null;

    private GameLogic gameLogic; // Holds the board and rules

    public Room(String roomId, String roomName, ClientHandler owner) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.owner = owner;
    }

    public synchronized void start() {
        if (!started && players.size() == 2) {
            started = true;
        }
    }

    public boolean isStarted() {
        return started;
    }

    public List<ClientHandler> getPlayers() {
        return players;
    }

    public ClientHandler getOwner() {
        return owner;
    }

    public boolean isOwner(ClientHandler client) {
        return owner == client;
    }

    public void setOwner(ClientHandler newOwner) {
        this.owner = newOwner;
    }

    public synchronized Color getColor(ClientHandler player) {
        return colors.getOrDefault(player, Color.NONE);
    }

    public synchronized boolean pickColor(ClientHandler player, Color color) {
        if (started)
            return false;
        if (colors.get(player) != null && colors.get(player) != Color.NONE)
            return false;

        if (colors.containsValue(color))
            return false;

        colors.put(player, color);

        // assign remaining color to other player
        for (ClientHandler p : players) {
            if (p != player)
                colors.put(p, color == Color.BLACK ? Color.WHITE : Color.BLACK);
        }
        return true;
    }

    public synchronized boolean colorsChosen() {
        return colors.values().stream().noneMatch(c -> c == Color.NONE);
    }

    public synchronized void broadcast(ServerRequest request) {
        for (ClientHandler player : players)
            player.sendRequest(request);
    }

    public synchronized GameLogic getGameLogic() {
        return gameLogic;
    }

    public synchronized boolean join(ClientHandler client) {
        if (players.size() < 2) {
            players.add(client);
            colors.put(client, Color.NONE);
            return true;
        }
        return false;
    }

    public synchronized void leave(ClientHandler client) {
        players.remove(client);
        colors.remove(client);
        if (client == owner && !players.isEmpty()) {
            owner = players.get(0);
        }
    }

    public String getRoomId() {
        return roomId;
    }

    public String getRoomName() {
        return roomName;
    }

    // Color change methods
    public synchronized void setColorChangeRequester(ClientHandler client) {
        colorChangeRequester = client;
    }

    public synchronized ClientHandler getColorChangeRequester() {
        return colorChangeRequester;
    }

    public synchronized void clearColorChangeRequest() {
        colorChangeRequester = null;
    }

    public synchronized boolean canRespondToColorChange(ClientHandler player) {
        return colorChangeRequester != null && player != colorChangeRequester;
    }

    public synchronized void swapColors(ClientHandler a, ClientHandler b) {
        Color temp = colors.get(a);
        colors.put(a, colors.get(b));
        colors.put(b, temp);
    }

}
