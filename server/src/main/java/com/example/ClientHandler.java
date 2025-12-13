package com.example;

import com.example.Board;

import java.io.*;
import java.net.Socket;

public class ClientHandler implements Runnable {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Server server;

    public ClientHandler(Socket socket, Server server) {
        this.socket = socket;
        this.server = server;

        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());
        } catch (IOException e) {
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
        // Further communication handled in GameSession
    }
}

