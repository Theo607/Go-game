package com.example;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {
    private static final int PORT = 1664;
    private ServerSocket serverSocket;
    private static boolean running;

    private final ClientManager clientManager = new ClientManager();
    private final RoomManager roomManager = new RoomManager();

    public void start() {
        try {
            running = true;
            serverSocket = new ServerSocket(PORT);
            Logger.info("Server started on port: " + PORT);

            while (running) {
                Socket clientSocket = serverSocket.accept();
                Logger.info("New client connected: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, clientManager, roomManager);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            Logger.error("Server error: ",  e);
        } finally {
            Logger.info("Server stopped.");
            stop();
        }
    }

    public void stop() {
        try {
            if (serverSocket != null) serverSocket.close();
            Logger.info("Server stopped.");
        } catch (IOException e) {
            Logger.error("Error closing server: ", e);
        }
    }

    public void kill() { running = false; }

    public static void main(String[] args) {
        new Server().start();
    }
}
