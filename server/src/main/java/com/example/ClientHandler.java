package com.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {
    private final Socket socket;
    private final ClientManager manager;
    private final RoomManager roomManager;

    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username = "Guest";
    private Room currentRoom = null;
    private PlayerAction latestAction = null;

    private final ClientCommandProcessor commandProcessor;

    public ClientHandler(Socket socket, ClientManager manager, RoomManager roomManager) {
        this.socket = socket;
        this.manager = manager;
        this.roomManager = roomManager;
        this.commandProcessor = new ClientCommandProcessor(this);

        manager.addClient(this);

        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            System.out.println("Error initializing streams for client " + socket.getInetAddress());
        }
    }

    @Override
    public void run() {
        try {
            while (true) {
                Object obj = in.readObject();
                if (obj instanceof NetworkMessage message) {
                    commandProcessor.processCommand(message);
                }
            }
        } catch (Exception e) {
            System.out.println("Client disconnected: " + socket.getInetAddress());
        } finally {
            manager.removeClient(this);
            roomManager.leaveRoom(this);
            close();
        }
    }

    public synchronized void setLatestAction(PlayerAction action) {
        this.latestAction = action;
        notifyAll();
    }

    public synchronized PlayerAction consumeLatestAction() {
        PlayerAction action = latestAction;
        latestAction = null;
        return action;
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

    // Getters and setters
    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }
    public Room getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(Room room) { this.currentRoom = room; }
    public RoomManager getRoomManager() { return roomManager; }
}
