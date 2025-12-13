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
                        if (room != null) {
                            sendRequest(new ServerRequest(
                                    "Room created: " + room.getRoomName() + " (ID: " + room.getRoomId() + ")"));
                        } else {
                            sendRequest(new ServerRequest("Room creation failed."));
                        }
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
                    List<String> rooms = roomManager.listRooms();
                    sendRequest(new ServerRequest("Available rooms: " + String.join(", ", rooms)));
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
}
