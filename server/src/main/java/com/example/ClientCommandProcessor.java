package com.example;

public class ClientCommandProcessor {

    private final ClientHandler client;
    private final RoomActionHandler roomHandler;
    private final GameActionHandler gameHandler;

    public ClientCommandProcessor(ClientHandler client) {
        this.client = client;
        this.roomHandler = new RoomActionHandler(client);
        this.gameHandler = new GameActionHandler(client);
    }

    public void processMessage(Message msg) {
        switch (msg.type) {
            case SET_NAME -> handleSetName(msg);
            //TODO: Add swap thingys
           case CREATE_ROOM, LIST_ROOMS, LIST_PLAYERS, JOIN, LEAVE_ROOM, PICK_COLOR, BEGIN,
             SWAP, ACCEPT_SWAP, DECLINE_SWAP, SWAP_ACCEPTED, SWAP_DECLINED ->
                roomHandler.handleMessage(msg);
            case MOVE, PASS, RESIGN ->
                gameHandler.handleMessage(msg);
            default -> sendUnknownCommand(msg);
        }
    }

    private void handleSetName(Message msg) {
        if (msg.nick == null || msg.nick.isBlank()) {
            sendError("Invalid username");
            return;
        }

        client.username = msg.nick;

        Message response = new Message();
        response.type = MessageType.NICK_SET;
        response.nick = client.username;
        client.sendMessage(response);
    }

    private void sendUnknownCommand(Message msg) {
        Message response = new Message();
        response.type = MessageType.UNKNOWN;
        client.sendMessage(response);
    }

    private void sendError(String text) {
        Message response = new Message();
        response.type = MessageType.ERROR;
        response.nick = text;
        client.sendMessage(response);
    }
}

