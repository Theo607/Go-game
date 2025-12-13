package com.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;


public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 1664;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Thread listenerThread;
    public void start() {
        try {
            this.socket = new Socket(HOST, PORT);
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());

            this.listenerThread = new Thread(new ClientListener(in, this));
            this.listenerThread.start();
        } catch (IOException e) {
            System.out.println("Unable to connect to server.");
        }
    }

    public void handleServerRequest(ServerRequest request) {
        // Handle different types of server requests here
        System.out.println("Received request: " + request.getType());
    }

    public void sendMessage(NetworkMessage message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to send message to server.");
        }
    }

    public void kill() {
        try {
            in.close();
            out.close();
            listenerThread.interrupt();
            socket.close();
        } catch (IOException e) {
            System.out.println("Error closing client streams.");
        }
    }

    public void printStr(String message) {
        System.out.println(message);
    }

    public static void main(String[] args) {
        new Client().start();
    }
}
