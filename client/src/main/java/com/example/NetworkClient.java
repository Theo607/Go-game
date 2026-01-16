package com.example;

import java.io.*;
import java.net.Socket;
import java.util.function.Consumer;

public class NetworkClient {

    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Thread listenerThread;
    private volatile boolean running = false;

    private final String host;
    private final int port;
    private Consumer<Message> onMessageReceived;

    public NetworkClient(String host, int port, Consumer<Message> onMessageReceived) {
        this.host = host;
        this.port = port;
        this.onMessageReceived = onMessageReceived;
    }

    public synchronized void connect() {
        if (running) return;

        try {
            socket = new Socket(host, port);

            out = new ObjectOutputStream(socket.getOutputStream());
            out.flush(); // important
            in = new ObjectInputStream(socket.getInputStream());

            running = true;
            Logger.info("Connected to server: " + host + ":" + port);

            listenerThread = new Thread(this::listenLoop, "NetworkClient-Listener");
            listenerThread.start();

        } catch (IOException e) {
            Logger.error("Failed to connect to server: " + host + ":" + port, e);
            disconnect();
        }
    }

    private void listenLoop() {
        try {
            while (running) {
                Object obj = in.readObject();
                if (obj instanceof Message msg) {
                    onMessageReceived.accept(msg);
                } else {
                    Logger.warn("Received unknown object: " + obj);
                }
            }
        } catch (EOFException e) {
            Logger.info("Server closed the connection.");
        } catch (Exception e) {
            Logger.error("Connection lost.", e);
        } finally {
            disconnect();
        }
    }

    public synchronized void sendMessage(Message msg) {
        if (!running || out == null) {
            Logger.warn("Cannot send message, not connected: " + msg.type);
            return;
        }

        try {
            out.writeObject(msg);
            out.flush();
            Logger.debug("Sent message: " + msg.type);
        } catch (IOException e) {
            Logger.error("Failed to send message: " + msg.type, e);
            disconnect();
        }
    }

    public synchronized void disconnect() {
        if (!running) return;
        running = false;

        try { if (in != null) in.close(); } catch (IOException ignored) {}
        try { if (out != null) out.close(); } catch (IOException ignored) {}
        try { if (socket != null) socket.close(); } catch (IOException ignored) {}

        Logger.info("Disconnected from server.");
    }

    public boolean isRunning() {
        return running;
    }
}

