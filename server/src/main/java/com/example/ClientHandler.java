package com.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Server server;
    private boolean connected = true;
    private String USERNAME;
    private GameSession gameSession;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
            Object obj = in.readObject();
            if (obj instanceof String) {
                USERNAME = (String) obj;
                System.out.println("Player " + USERNAME + " connected");
            }
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    public void sendBoard(Board board) {
        try {
            out.writeObject(board);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public Board receiveBoard() {
        try {
            return (Board) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void run() {
        try {
          server.addWaitingPlayer(this);
        } catch (Exception e) {
          e.printStackTrace();
        }
        Object obj;
        while (connected) {
            try {
                obj = in.readObject();
                if (obj instanceof String) handleMessage((String) obj);
            } catch (IOException | ClassNotFoundException e) {}
        }
        // Further communication handled in GameSession
    }

    public void handleMessage(String command) {
        if (command.equals("pass")) gameSession.pass(this);
        else if (command.equals("resign")) {
            disconnectClient();
        } else {}
    }

    public void disconnectClient() {
        connected = false;
        server.removePlayer(this);
    }

    public void winner() {
        server.movePlayerBack(this);
        try {
            out.writeObject("won");
        } catch (IOException e) {}
    }

    public void setSession(GameSession session) {
        this.gameSession = session;
    }

    public String getUsername() {
        return USERNAME;
    }
}