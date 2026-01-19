package com.example;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Room {
    private final String roomId;
    private final String roomName;
    private ClientHandler owner;
    private ClientHandler player; // second player
    private boolean started = false;
    private final Map<ClientHandler, StoneColor> colors = new HashMap<>();
    private ClientHandler colorChangeRequester = null;
    private Board board;
    private ClientHandler currentPlayer;

    private GameLogic gameLogic; // Holds the board and rules

    public Room(String roomId, String roomName, ClientHandler owner) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.owner = owner;
        this.player = null;
        owner.setCurrentRoom(this);
        colors.put(owner, StoneColor.EMPTY_STONE);
    }
  
    public synchronized boolean join(ClientHandler client) {
        if (client == owner) return false; // owner already in room
        if (player == null) {
            player = client;
            colors.put(client, StoneColor.EMPTY_STONE);
            return true;
        }
        return false; // room full
    }



    public synchronized void leave(ClientHandler client) {
        if (client == owner) {
            owner = null;
            if (player != null) {
                owner = player;
                player = null;
            }
        } else if (client == player) {
            player = null;
        }
        colors.remove(client);
        clearColorChangeRequestIfNeeded(client);
    }

    private void clearColorChangeRequestIfNeeded(ClientHandler client) {
        if (colorChangeRequester == client) colorChangeRequester = null;
    }

    public synchronized void start() {
        if (started)
            return;
        if (owner == null || player == null)
            return;
        if (!colorsChosen())
            return;
        this.gameLogic = new GameLogic(board);
        this.started = true;
    }

    public boolean isStarted() { return started; }

    public ClientHandler getOwner() { return owner; }

    public ClientHandler getPlayer() { return player; }

    public boolean isOwner(ClientHandler client) { return owner == client; }

    public void setOwner(ClientHandler newOwner) { this.owner = newOwner; }

    public synchronized ClientHandler getCurrentPlayer() { return currentPlayer; }

    public synchronized void setCurrentPlayer(ClientHandler player) { this.currentPlayer = player; }

    // UPDATED: use StoneColor
    public synchronized StoneColor getColor(ClientHandler client) {
        return colors.getOrDefault(client, StoneColor.EMPTY_STONE);
    }

    public synchronized boolean pickColor(ClientHandler client, StoneColor color) {
        if (started) return false;
        if (colors.get(client) != null && colors.get(client) != StoneColor.EMPTY_STONE) return false;
        if (colors.containsValue(color)) return false;

        colors.put(client, color);

        // assign remaining color to the other player
        ClientHandler other = (client == owner) ? player : owner;
        if (other != null) {
            colors.put(other, color == StoneColor.BLACK_STONE ? StoneColor.WHITE_STONE : StoneColor.BLACK_STONE);
        }

        return true;
    }

    public synchronized boolean colorsChosen() {
        if (owner == null || player == null) return false;
        return colors.get(owner) != StoneColor.EMPTY_STONE &&
               colors.get(player) != StoneColor.EMPTY_STONE;
    }

    public synchronized void broadcast(Message msg) {
        if (owner != null) owner.sendMessage(msg);
        if (player != null) player.sendMessage(msg);
    }

    public synchronized GameLogic getGameLogic() { return gameLogic; }

    public String getRoomId() { return roomId; }

    public String getRoomName() { return roomName; }

    // Color change methods
    public synchronized void setColorChangeRequester(ClientHandler client) { colorChangeRequester = client; }

    public synchronized ClientHandler getColorChangeRequester() { return colorChangeRequester; }

    public synchronized void clearColorChangeRequest() { colorChangeRequester = null; }

    public synchronized boolean canRespondToColorChange(ClientHandler client) {
        return colorChangeRequester != null && client != colorChangeRequester;
    }

    public synchronized void swapColors(ClientHandler a, ClientHandler b) {
        StoneColor temp = colors.get(a);
        colors.put(a, colors.get(b));
        colors.put(b, temp);
    }

    public synchronized List<ClientHandler> getPlayers() {
        List<ClientHandler> list = new ArrayList<>();
        if (owner != null) list.add(owner);
        if (player != null) list.add(player);
        return list;
    }

    // For convenience: get array of nicks with colors
    public String[] getPlayerNicks() {
        String ownerColor = colors.getOrDefault(owner, StoneColor.EMPTY_STONE).name();
        ownerColor = ownerColor.equals("EMPTY_STONE") ? "No color" : ownerColor;

        String playerColor = player != null 
                ? colors.getOrDefault(player, StoneColor.EMPTY_STONE).name() 
                : "No player";
        if (playerColor.equals("EMPTY_STONE")) playerColor = "No color";

        String ownerStr = "(Owner) " + owner.username + " [" + ownerColor + "]";
        String playerStr = player != null ? "(Player) " + player.username + " [" + playerColor + "]" 
                                          : "(Player) <none>";

        return new String[]{ownerStr, playerStr};
    }
}
