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
    private InputHandler inputHandler;

    public InputHandler getInputHandler() {
        return inputHandler;
    }

    public ClientCommandSender getCommandSender() {
        return commandSender;
    }

    public void start(String inputHandlerFlag) {
        try {
            socket = new Socket(HOST, PORT);
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            commandSender = new ClientCommandSender(out);
            requestHandler = new ClientRequestHandler(this);

            // choose input handler
            switch (inputHandlerFlag) {
                case "gui" -> inputHandler = new GuiInputHandler(commandSender, this);
                case "console" -> inputHandler = new ConsoleInputHandler(commandSender, this);
                default -> inputHandler = new ConsoleInputHandler(commandSender, this);
            }

            // start listener thread
            Thread listenerThread = new Thread(new ClientListener(in, requestHandler));
            listenerThread.start();

            // run input handler
            inputHandler.runInputLoop();

            // main loop keeps client alive
            while (running) {
                try {
                    Thread.sleep(100);
                } catch (InterruptedException e) {
                    break;
                }
            }

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
        String mode = args.length > 0 ? args[0] : "console";
        new Client().start(mode);
    }
}

