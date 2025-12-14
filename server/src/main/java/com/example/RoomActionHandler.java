package com.example;

import java.util.List;

public class RoomActionHandler {
    private final ClientHandler client;
    private final RoomManager roomManager;

    public RoomActionHandler(ClientHandler client) {
        this.client = client;
        this.roomManager = client.getRoomManager();
    }

    private boolean checkUsernameSet() {
        if ("Guest".equals(client.getUsername())) {
            client.sendRequest(
                    new ServerRequest("ERROR", "You must set your username before joining or creating a room."));
            return false;
        }
        return true;
    }

    public void handleCommand(ClientCommand command) {
        // Normalize: trim and uppercase
        String cmdType = command.getCommandType().trim().toUpperCase();

        switch (cmdType) {
            case "BEGIN" -> handleBeginGame();
            case "CREATE_ROOM" -> handleCreateRoom(command);
            case "JOIN_ROOM" -> handleJoinRoom(command);
            case "LEAVE_ROOM" -> handleLeaveRoom();
            case "LIST_ROOMS" -> handleListRooms();
            case "PICK_COLOR" -> handlePickColor(command);
            case "REQUEST_COLOR_CHANGE" -> handleRequestColorChange();
            case "ACCEPT_COLOR_CHANGE" -> handleAcceptColorChange();
            case "DECLINE_COLOR_CHANGE" -> handleDeclineColorChange();
            default -> client.sendRequest(new ServerRequest("UNKNOWN_COMMAND", command.getCommandType()));
        }
    }

    private void handleBeginGame() {
        Room room = client.getCurrentRoom();
        if (room == null) {
            client.sendRequest(new ServerRequest("ERROR", "You are not in a room."));
            return;
        }

        if (!room.isOwner(client)) {
            client.sendRequest(new ServerRequest("ERROR", "Only the room owner can start the game."));
            return;
        }

        if (!room.colorsChosen()) {
            client.sendRequest(new ServerRequest("ERROR", "Both players must pick colors before starting."));
            return;
        }

        if (room.isStarted()) {
            client.sendRequest(new ServerRequest("ERROR", "Game has already started."));
            return;
        }

        // Mark the room as started
        room.start();// you might need to add a setter for this in Room

        room.broadcast(new ServerRequest("GAME_STARTED"));

        // Start a new game session thread
        GameSession session = new GameSession(room);
        new Thread(session).start();
    }

    private void handleCreateRoom(ClientCommand command) {
        if (!checkUsernameSet())
            return;
        if (command.getParameters().length >= 1) {
            String roomName = command.getParameters()[0];
            Room room = client.getCurrentRoom() != null ? client.getCurrentRoom()
                    : client.getCurrentRoom() == null ? roomManager.createRoom(roomName, client) : null;
            roomManager.joinRoom(room.getRoomId(), client);
            client.sendRequest(new ServerRequest("ROOM_CREATED", room.getRoomName(), room.getRoomId()));
        } else {
            client.sendRequest(new ServerRequest("USAGE", "CREATE_ROOM <roomName>"));
        }
    }

    private void handleJoinRoom(ClientCommand command) {
        if (!checkUsernameSet())
            return;
        if (command.getParameters().length == 0) {
            client.sendRequest(new ServerRequest("USAGE", "JOIN_ROOM <roomId>"));
            return;
        }

        String roomId = command.getParameters()[0];
        boolean success = roomManager.joinRoom(roomId, client);

        if (success) {
            client.sendRequest(new ServerRequest("JOINED_ROOM", roomId));
        } else {
            client.sendRequest(new ServerRequest("JOIN_ROOM_FAILED", roomId));
        }
    }

    private void handleLeaveRoom() {
        Room room = client.getCurrentRoom();
        if (room == null) {
            client.sendRequest(new ServerRequest("NOT_IN_ROOM"));
            return;
        }

        roomManager.leaveRoom(client);
        client.sendRequest(new ServerRequest("LEFT_ROOM", room.getRoomName()));
    }

    private void handleListRooms() {
        Room room = client.getCurrentRoom();
        if (room != null) {
            // Player is in a room → list players with roles and colors
            StringBuilder sb = new StringBuilder();
            for (ClientHandler p : room.getPlayers()) {
                String role = room.isOwner(p) ? "Owner" : "Player";
                Color color = room.getColor(p);
                String colorStr = switch (color) {
                    case BLACK -> "(B)";
                    case WHITE -> "(W)";
                    default -> "(N)";
                };
                sb.append(role).append(" ").append(p.getUsername()).append(" ").append(colorStr).append("\n");
            }
            client.sendRequest(new ServerRequest("ROOM_PLAYERS", sb.toString().trim()));
        } else {
            // Player not in a room → list rooms
            List<String> rooms = roomManager.listRooms();
            if (rooms.isEmpty()) {
                client.sendRequest(new ServerRequest("NO_ROOMS_AVAILABLE"));
            } else {
                StringBuilder sb = new StringBuilder();
                for (String r : rooms) {
                    sb.append(r).append("\n");
                }
                client.sendRequest(new ServerRequest("AVAILABLE_ROOMS", sb.toString().trim()));
            }
        }
    }

    private void handlePickColor(ClientCommand command) {
        Room room = client.getCurrentRoom();
        if (room == null) {
            client.sendRequest(new ServerRequest("NOT_IN_ROOM"));
            return;
        }

        if (room.isStarted()) {
            client.sendRequest(new ServerRequest("GAME_ALREADY_STARTED"));
            return;
        }

        if (command.getParameters().length == 0) {
            client.sendRequest(new ServerRequest("USAGE", "PICK_COLOR <BLACK|WHITE>"));
            return;
        }

        Color color;
        try {
            color = Color.valueOf(command.getParameters()[0]);
        } catch (IllegalArgumentException e) {
            client.sendRequest(new ServerRequest("INVALID_COLOR"));
            return;
        }

        boolean success = room.pickColor(client, color);
        if (success) {
            room.broadcast(new ServerRequest("COLOR_PICKED", client.getUsername(), color.name()));
        } else {
            client.sendRequest(new ServerRequest("COLOR_UNAVAILABLE"));
        }
    }

    private void handleRequestColorChange() {
        Room room = client.getCurrentRoom();
        if (room == null || room.isStarted()) {
            client.sendRequest(new ServerRequest("CANNOT_CHANGE_COLOR_NOW"));
            return;
        }

        room.setColorChangeRequester(client);
        room.broadcast(new ServerRequest("COLOR_CHANGE_REQUEST", client.getUsername()));
    }

    private void handleAcceptColorChange() {
        Room room = client.getCurrentRoom();
        if (room == null || !room.canRespondToColorChange(client)) {
            client.sendRequest(new ServerRequest("CANNOT_ACCEPT_COLOR_CHANGE"));
            return;
        }

        ClientHandler requester = room.getColorChangeRequester();
        room.swapColors(requester, client);
        room.clearColorChangeRequest();
        room.broadcast(new ServerRequest("COLORS_SWAPPED"));
    }

    private void handleDeclineColorChange() {
        Room room = client.getCurrentRoom();
        if (room == null || room.getColorChangeRequester() == null) {
            client.sendRequest(new ServerRequest("NO_COLOR_CHANGE_REQUEST_PENDING"));
            return;
        }

        room.clearColorChangeRequest();
        room.broadcast(new ServerRequest("COLOR_CHANGE_DECLINED"));
    }
}
