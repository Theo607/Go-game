package com.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 1664;
    private ServerSocket serverSocket;
    private final ClientManager clientManager = new ClientManager();
    private final RoomManager roomManager = new RoomManager();

    public void start() {
        try {
            serverSocket = new ServerSocket(PORT);
            System.out.println("Server started on port " + PORT);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, clientManager, roomManager);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            System.out.println("Server error: " + e.getMessage());
        } finally {
            stop();
        }
    }

    public void stop() {
        try {
            if (serverSocket != null) serverSocket.close();
            System.out.println("Server stopped.");
        } catch (IOException e) {
            System.out.println("Error closing server: " + e.getMessage());
        }
    }

    public static void main(String[] args) {
        new Server().start();
    }
}
