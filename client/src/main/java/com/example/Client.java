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
    private boolean running = true;

    private ClientCommandSender commandSender;
    private ClientRequestHandler requestHandler;
    private ConsoleInputHandler consoleHandler;

    public void start() {
        try {
            socket = new Socket(HOST, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            commandSender = new ClientCommandSender(out);
            requestHandler = new ClientRequestHandler(this);
            consoleHandler = new ConsoleInputHandler(commandSender, this);

            Thread listenerThread = new Thread(new ClientListener(in, requestHandler));
            listenerThread.start();

            consoleHandler.runInputLoop(); // main thread handles console input

        } catch (IOException e) {
            System.out.println("Unable to connect to server.");
        } finally {
            kill();
        }
    }

    public void printStr(String message) {
        System.out.println(message);
    }

    public void kill() {
        running = false;
        try {
            if (in != null) in.close();
            if (out != null) out.close();
            if (socket != null) socket.close();
        } catch (IOException ignored) {}
    }

    public boolean isRunning() {
        return running;
    }

    public static void main(String[] args) {
        new Client().start();
    }
}
