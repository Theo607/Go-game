package com.example;

public class ClientCommand implements NetworkMessage {
    private final String commandType;
    private final String[] parameters;

    public ClientCommand(String commandType, String... parameters) {
        this.commandType = commandType;
        this.parameters = parameters;
    }

    public String getCommandType() {
        return commandType;
    }

    public String[] getParameters() {
        return parameters;
    }
}
