package com.example;

import java.util.List;

public class RoomActionHandler {

    private final ClientHandler client;
    private final RoomManager roomManager;

    public RoomActionHandler(ClientHandler client) {
        this.client = client;
        this.roomManager = client.getRoomManager();
    }

    public void handleMessage(Message msg) {
        switch (msg.type) {
            case CREATE_ROOM -> handleCreateRoom(msg.roomName);
            case LIST_ROOMS -> handleListRooms();
            case LIST_PLAYERS -> handleListPlayers();
            case JOIN -> handleJoinRoom(msg.roomName); // reuse roomName as roomId
            case LEAVE_ROOM -> handleLeaveRoom();
            case PICK_COLOR -> handlePickColor(msg.color);
            case SWAP -> handleSwapRequest();
            case ACCEPT_SWAP -> handleSwapAccept();
            case DECLINE_SWAP -> handleSwapDecline();
            case BEGIN -> handleBeginGame();
            default -> sendUnknown(msg);
        }
    }

    private void handleCreateRoom(String roomName) {
        if (client.username == null || client.username.isBlank()) {
            sendError("You must set a username before creating a room.");
            return;
        }
        Room room = roomManager.createRoom(roomName, client);
        roomManager.joinRoom(room.getRoomId(), client);

        Message response = new Message();
        response.type = MessageType.ROOM_CREATED;
        response.roomName = room.getRoomName();
        client.sendMessage(response);
    }

    private void handleJoinRoom(String roomId) {
        if (roomManager.joinRoom(roomId, client)) {
            Message response = new Message();
            response.type = MessageType.JOIN;
            response.roomName = roomId;
            client.sendMessage(response);
        } else sendError("Failed to join room.");
    }

    private void handleLeaveRoom() {
        Room room = client.getCurrentRoom();
        if (room != null) {
            roomManager.leaveRoom(client);
            Message response = new Message();
            response.type = MessageType.LEAVE_ROOM;
            response.roomName = room.getRoomName();
            client.sendMessage(response);
        } else sendError("You are not in a room.");
    }

    private void handleListRooms() {
        List<String> rooms = roomManager.listRooms();
        Message response = new Message();
        response.type = MessageType.ROOM_LIST;
        response.roomList = rooms.toArray(new String[0]);
        client.sendMessage(response);
    }

    private void handleListPlayers() {
        Room current = client.getCurrentRoom();
        if (current == null) {
            sendError("You are not in a room.");
            return;
        }

        Message response = new Message();
        response.type = MessageType.PLAYER_LIST;
        response.playerNames = current.getPlayerNicks(); 
        client.sendMessage(response);
    }


    // UPDATED: use StoneColor instead of Color
    private void handlePickColor(StoneColor color) {
        Room room = client.getCurrentRoom();
        if (room == null) { 
            sendError("You are not in a room."); 
            return; 
        }

        if (room.pickColor(client, color)) {
            Message msg = new Message();
            msg.type = MessageType.PICK_COLOR;
            msg.color = color;
            msg.nick = client.username;
            room.broadcast(msg);
        } else sendError("Color not available.");
    }

    private void handleSwapRequest() {
        Room room = client.getCurrentRoom();
        if (room == null || room.getPlayer() == null) {
            sendError("Cannot swap colors: you are not in a full room.");
            return;
        }

        // Check if a swap is already requested
        if (room.getColorChangeRequester() != null) {
            sendError("There is already a swap request pending.");
            return;
        }

        room.setColorChangeRequester(client);

        // Notify the other player
        ClientHandler other = (client == room.getOwner()) ? room.getPlayer() : room.getOwner();
        Message m = new Message();
        m.type = MessageType.SWAP;
        m.nick = client.username; // who requested swap
        other.sendMessage(m);

        Logger.info(client.username + " requested a color swap.");
    }

    private void handleSwapAccept() {
        Room room = client.getCurrentRoom();
        ClientHandler requester = room.getColorChangeRequester();
        if (requester == null) {
            sendError("No swap request to accept.");
            return;
        }

        // Only the other player can accept
        if (requester == client) {
            sendError("You cannot accept your own swap request.");
            return;
        }

        // Perform the swap
        room.swapColors(requester, client);
        room.clearColorChangeRequest();

        Message m = new Message();
        m.type = MessageType.SWAP_ACCEPTED;
        room.broadcast(m);

        Logger.info("Color swap accepted between " + requester.username + " and " + client.username);
    }

    private void handleSwapDecline() {
        Room room = client.getCurrentRoom();
        ClientHandler requester = room.getColorChangeRequester();
        if (requester == null) {
            sendError("No swap request to decline.");
            return;
        }

        room.clearColorChangeRequest();

        Message m = new Message();
        m.type = MessageType.SWAP_DECLINED;
        requester.sendMessage(m); // notify requester

        Logger.info(client.username + " declined color swap from " + requester.username);
    }

    private void handleBeginGame() {
        Room room = client.getCurrentRoom();
        if (room == null || !room.isOwner(client) || !room.colorsChosen()) {
            sendError("Cannot start game yet.");
            return;
        }

        room.start();
        room.broadcast(buildMessage(MessageType.BEGIN, "Game started!"));
        new Thread(new GameSession(room)).start();
    }

    private void sendUnknown(Message msg) {
        sendError("Unknown room command: " + msg.type);
    }

    private void sendError(String text) {
        Message m = new Message();
        m.type = MessageType.ERROR;
        m.nick = text; // reuse nick/message field for errors
        client.sendMessage(m);
    }

    private Message buildMessage(MessageType type, String... params) {
        Message m = new Message();
        m.type = type;
        if (params.length > 0) m.nick = params[0];
        return m;
    }
}

