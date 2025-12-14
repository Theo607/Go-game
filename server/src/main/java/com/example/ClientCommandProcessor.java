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

    public void processCommand(NetworkMessage message) {
        if (!(message instanceof ClientCommand command)) return;

        switch (command.getCommandType()) {
            case "SET_USERNAME" -> {
                if (command.getParameters().length > 0) {
                    client.setUsername(command.getParameters()[0]);
                    client.sendRequest(new ServerRequest("USERNAME_SET", client.getUsername()));
                }
            }
            case "CREATE_ROOM", "JOIN_ROOM", "LEAVE_ROOM", "LIST_ROOMS",
                 "PICK_COLOR", "REQUEST_COLOR_CHANGE", "ACCEPT_COLOR_CHANGE", "DECLINE_COLOR_CHANGE" ->
                    roomHandler.handleCommand(command);

            case "MOVE", "PASS", "RESIGN" ->
                    gameHandler.handleCommand(command);

            default -> client.sendRequest(new ServerRequest("UNKNOWN_COMMAND", command.getCommandType()));
        }
    }
}
