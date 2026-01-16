package com.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class ClientHandler implements Runnable {

    private final Socket socket;
    private final ClientManager clientManager;
    private final RoomManager roomManager;
    private ObjectInputStream in;
    private ObjectOutputStream out;
    private boolean running;

    public String username;
    private Room currentRoom;
    private final BlockingQueue<PlayerAction> actionQueue = new LinkedBlockingQueue<>();
    private final ClientCommandProcessor processor;

    public ClientHandler(Socket socket, ClientManager cm, RoomManager rm) {
        this.socket = socket;
        this.clientManager = cm;
        this.roomManager = rm;
        this.processor = new ClientCommandProcessor(this);
        this.running = true;

        try {
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
            Logger.error("Error initializing client streams", e);
        }

        this.username = null;
        this.currentRoom = null;
        clientManager.addClient(this);
    }

    @Override
    public void run() {
        try {
            while (running) {
                Object obj = in.readObject();
                if (obj instanceof Message msg) {
                    processor.processMessage(msg);
                }
            }
        } catch (Exception e) {
            Logger.error("Client disconnected: " + socket.getInetAddress(), e);
        } finally {
            cleanup();
        }
    }

    public void sendMessage(Message msg) {
        try {
            out.writeObject(msg);
            out.flush();
        } catch (IOException e) {
            Logger.error("Failed to send message to client", e);
        }
    }

    public void submitAction(PlayerAction action) { actionQueue.offer(action); }
    public PlayerAction waitForAction() throws InterruptedException { return actionQueue.take(); }

    public Room getCurrentRoom() { return currentRoom; }
    public void setCurrentRoom(Room room) { this.currentRoom = room; }
    public RoomManager getRoomManager() {
        return this.roomManager;
    }


    private void cleanup() {
        running = false;
        clientManager.removeClient(this);
        if (currentRoom != null) currentRoom.leave(this);
        try {
            in.close();
            out.close();
            socket.close();
        } catch (IOException ignored) {}
    }
}

