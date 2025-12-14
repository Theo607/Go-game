package com.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.List;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ClientManager manager;
    private final RoomManager roomManager;
    private Room currentRoom = null;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username = "Guest";

    public ClientHandler(Socket socket, ClientManager manager, RoomManager roomManager) {
        this.socket = socket;
        this.manager = manager;
        this.roomManager = roomManager;
        manager.addClient(this);
        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Error initializing streams for client" + socket.getInetAddress());
        }
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof NetworkMessage message) {
                    handleMessage(message);
                }
            }
        } catch (Exception e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
            manager.removeClient(this);
            close();
        }
    }

    private void handleMessage(NetworkMessage message) {
        if (message instanceof ClientCommand command) {
            switch (command.getCommandType()) {
                case "SET_USERNAME" -> {
                    if (command.getParameters().length > 0) {
                        setUsername(command.getParameters()[0]);
                        sendRequest(new ServerRequest("Username set to " + getUsername()));
                    }
                }
                case "CREATE_ROOM" -> {
                    if (command.getParameters().length >= 1) {
                        String roomName = command.getParameters()[0];
                        Room room = roomManager.createRoom(roomName, this);
                        roomManager.joinRoom(room.getRoomId(), this);
                        sendRequest(new ServerRequest(
                                "Room created: " + room.getRoomName() + " (ID: " + room.getRoomId() + ")"));

                    } else {
                        sendRequest(new ServerRequest("Usage: CREATE_ROOM <roomName>"));
                    }
                }
                case "JOIN_ROOM" -> {
                    if (command.getParameters().length > 0) {
                        String roomId = command.getParameters()[0];
                        boolean success = roomManager.joinRoom(roomId, this);
                        if (success) {
                            sendRequest(new ServerRequest("Joined room: " + roomId));
                        } else {
                            sendRequest(new ServerRequest("Failed to join room: " + roomId));
                        }
                    }
                }
                case "LIST_ROOMS" -> {
                    if (currentRoom != null) {
                        // Player is in a room → list players
                        List<String> players = currentRoom.getPlayerNames();
                        sendRequest(new ServerRequest(
                                "Players in room '" + currentRoom.getRoomName() + "': " +
                                        String.join(", ", players)));
                    } else {
                        // Player not in a room → list rooms
                        List<String> rooms = roomManager.listRooms();
                        if (rooms.isEmpty()) {
                            sendRequest(new ServerRequest("No rooms available."));
                        } else {
                            sendRequest(new ServerRequest(
                                    "Available rooms:\n" + String.join("\n", rooms)));
                        }
                    }
                }
                case "START" -> {
                    if (currentRoom == null) {
                        sendRequest(new ServerRequest("You are not in a room."));
                        break;
                    }

                    if (!currentRoom.isOwner(this)) {
                        sendRequest(new ServerRequest("Only the owner can start the game."));
                        break;
                    }

                    if (!currentRoom.colorsChosen()) {
                        sendRequest(new ServerRequest("Both players must pick colors first."));
                        break;
                    }

                    // currentRoom.start(null); // GameSession created later
                    // currentRoom.broadcast(new ServerRequest("Game started."));
                }
                case "PICK_COLOR" -> {
                    if (currentRoom == null) {
                        sendRequest(new ServerRequest("You are not in a room."));
                        break;
                    }

                    if (currentRoom.isStarted()) {
                        sendRequest(new ServerRequest("Game already started."));
                        break;
                    }

                    Color color;
                    try {
                        color = Color.valueOf(command.getParameters()[0]);
                    } catch (Exception e) {
                        sendRequest(new ServerRequest("Invalid color."));
                        break;
                    }

                    boolean success = currentRoom.pickColor(this, color);
                    if (!success) {
                        sendRequest(new ServerRequest("Color unavailable."));
                        break;
                    }

                    currentRoom.broadcast(new ServerRequest(
                            getUsername() + " picked " + color));
                }
                case "REQUEST_COLOR_CHANGE" -> {
                    if (currentRoom == null || currentRoom.isStarted()) {
                        sendRequest(new ServerRequest("Cannot change colors now."));
                        break;
                    }

                    currentRoom.setColorChangeRequester(this);
                    currentRoom.broadcast(new ServerRequest(
                            getUsername()
                                    + " wants to swap colors. Type accept or decline."));
                }
                case "ACCEPT_COLOR_CHANGE" -> {
                    if (currentRoom == null || !currentRoom.canRespondToColorChange(this)) {
                        sendRequest(new ServerRequest("You cannot accept a color change request."));
                        break;
                    }

                    ClientHandler requester = currentRoom.getColorChangeRequester();
                    currentRoom.swapColors(requester, this);
                    currentRoom.clearColorChangeRequest();
                    currentRoom.broadcast(new ServerRequest("Colors swapped."));
                }
                case "DECLINE_COLOR_CHANGE" -> {
                    if (currentRoom != null) {
                        currentRoom.clearColorChangeRequest();
                        currentRoom.broadcast(new ServerRequest("Color change declined."));
                    }
                }
                case "LEAVE_ROOM" -> {
                    if (currentRoom == null) {
                        sendRequest(new ServerRequest("You are not in a room."));
                    } else {
                        Room room = currentRoom;
                        roomManager.leaveRoom(this); // removes client from room, handles owner logic
                        sendRequest(new ServerRequest("You have left the room: " + room.getRoomName()));
                    }
                }
                default -> sendRequest(new ServerRequest("Unknown command: " + command.getCommandType()));
            }
        }
    }

    public void sendRequest(ServerRequest request) {
        try {
            out.writeObject(request);
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to send request to client " + socket.getInetAddress());
        }
    }

    public void close() {
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException e) {
            System.out.println("Error closing client " + socket.getInetAddress());
        }
    }

    public Room getCurrentRoom() {
        return currentRoom;
    }

    public void setCurrentRoom(Room room) {
        this.currentRoom = room;
    }
}
