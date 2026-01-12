package com.example;

public abstract class InputHandler {
    protected final ClientCommandSender commandSender;
    protected final Client client;

    public InputHandler(ClientCommandSender commandSender, Client client) {
        this.commandSender = commandSender;
        this.client = client;
    }

    public abstract void runInputLoop();
    
    // Metoda do przekazywania wiadomości z serwera do UI
    public abstract void handleServerMessage(String message);
}
