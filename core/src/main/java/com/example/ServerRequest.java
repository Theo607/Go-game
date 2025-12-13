package com.example;

public class ServerRequest implements NetworkMessage {
    private final String requestType;

    public ServerRequest(String requestType) {
        this.requestType = requestType;
    }

    public String getType() {
        return requestType;
    }
}
