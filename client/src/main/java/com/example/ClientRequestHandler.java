package com.example;

public class ClientRequestHandler {
    private Client client;
    public ClientRequestHandler(Client client) {
        this.client = client;
    }
    public void handleRequest(ServerRequest request) {
        client.printStr("Handling request: " + request.getType());
    }
    
}
