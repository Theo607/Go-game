package com.example;

import com.example.exceptions.IncorrectBoardSize;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Queue;

public class Server {

    private final int port;
    private final Queue<ClientHandler> waitingPlayers = new LinkedList<>();

    public Server(int port) {
        this.port = port;
    }

    public void start() {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Server started on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                System.out.println("New client connected: " + clientSocket.getInetAddress());

                ClientHandler handler = new ClientHandler(clientSocket, this);
                new Thread(handler).start(); // handle client in its own thread
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Adds a client to the waiting list and starts a game if two are available.
     */
    public synchronized void addWaitingPlayer(ClientHandler player) throws IncorrectBoardSize {
        waitingPlayers.add(player);

        if (waitingPlayers.size() >= 2) {
            ClientHandler p1 = waitingPlayers.poll();
            ClientHandler p2 = waitingPlayers.poll();

            System.out.println("Starting a new game session between two players.");
            GameSession session = new GameSession(p1, p2);
            new Thread(session).start();
        }
    }

    public static void main(String[] args) {
        Server server = new Server(5000);
        server.start();
    }
}

