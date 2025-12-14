package com.example;

public class ServerRequest implements NetworkMessage {
    private final String requestType;
    private final String[] parameters;

    public ServerRequest(String requestType, String... parameters) {
        this.requestType = requestType;
        this.parameters = parameters;
    }

    public String getType() {
        return requestType;
    }

    public String[] getParameters() {
        return parameters;
    }
}
