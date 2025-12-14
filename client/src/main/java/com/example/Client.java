package com.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 1664;
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private Thread listenerThread;
    private final ClientRequestHandler handler = new ClientRequestHandler(this);

    public void start() {
        try {
            this.socket = new Socket(HOST, PORT);
            this.out = new ObjectOutputStream(socket.getOutputStream());
            this.in = new ObjectInputStream(socket.getInputStream());

            this.listenerThread = new Thread(new ClientListener(in, this));
            this.listenerThread.start();

            handleConsoleInput();

        } catch (IOException e) {
            System.out.println("Unable to connect to server.");
        }
    }

    public void sendMessage(NetworkMessage message) {
        try {
            out.writeObject(message);
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to send message to server.");
        }
    }

    public void sendCommand(String commandType, String... parameters) {
        sendMessage(new ClientCommand(commandType, parameters));
    }

    public void handleServerRequest(ServerRequest request) {
        handler.handleRequest(request);
    }

    public void printStr(String message) {
        System.out.println(message);
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

    private void handleConsoleInput() {
        Scanner scanner = new Scanner(System.in);
        printStr("Enter commands: setname <name>, create <roomName>, join <roomId>, list, quit");

        while (true) {
            String input = scanner.nextLine().trim();
            if (input.isEmpty())
                continue;

            String[] parts = input.split(" ", 3);
            String command = parts[0].toLowerCase();

            switch (command) {
                case "setname" -> {
                    if (parts.length >= 2)
                        sendCommand("SET_USERNAME", parts[1]);
                    else
                        printStr("Usage: setname <username>");
                }
                case "create" -> {
                    if (parts.length >= 2)
                        sendCommand("CREATE_ROOM", parts[1]);
                    else
                        printStr("Usage: create <roomName>");
                }
                case "join" -> {
                    if (parts.length >= 2)
                        sendCommand("JOIN_ROOM", parts[1]);
                    else
                        printStr("Usage: join <roomId>");
                }
                case "leave" -> sendCommand("LEAVE_ROOM");
                case "list" -> sendCommand("LIST_ROOMS");
                case "start" -> sendCommand("START");
                case "pick" -> {
                    if (parts.length >= 2)
                        sendCommand("PICK_COLOR", parts[1].toUpperCase());
                    else
                        printStr("Usage: pick BLACK|WHITE");
                }
                case "swap" -> sendCommand("REQUEST_COLOR_CHANGE");
                case "accept" -> sendCommand("ACCEPT_COLOR_CHANGE");
                case "decline" -> sendCommand("DECLINE_COLOR_CHANGE");
                case "quit" -> {
                    printStr("Exiting...");
                    kill();
                    return;
                }
                default -> printStr("Unknown command. Available: setname, create, join, list, quit");
            }
        }
    }

    public static void main(String[] args) {
        new Client().start();
    }
}
