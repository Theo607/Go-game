package com.example;

public class GameActionHandler {
    private final ClientHandler client;

    public GameActionHandler(ClientHandler client) {
        this.client = client;
    }

    public void handleCommand(ClientCommand command) {
        Room room = client.getCurrentRoom();

        if (room == null || !room.isStarted()) {
            client.sendRequest(new ServerRequest("GAME_NOT_STARTED"));
            return;
        }

        try {
            switch (command.getCommandType()) {
                case "MOVE" -> {
                    if (command.getParameters().length < 2) {
                        client.sendRequest(new ServerRequest("INVALID_MOVE", "Coordinates missing"));
                        return;
                    }
                    int x = Integer.parseInt(command.getParameters()[0]);
                    int y = Integer.parseInt(command.getParameters()[1]);
                    Move move = new Move(x, y, room.getColor(client));
                    client.submitAction(PlayerAction.move(move));
                }
                case "PASS" -> client.submitAction(PlayerAction.pass());
                case "RESIGN" -> client.submitAction(PlayerAction.resign());
                default -> client.sendRequest(new ServerRequest("UNKNOWN_GAME_COMMAND", command.getCommandType()));
            }
        } catch (NumberFormatException e) {
            client.sendRequest(new ServerRequest("INVALID_MOVE", "Coordinates must be integers"));
        } catch (Exception e) {
            client.sendRequest(new ServerRequest("INVALID_MOVE", e.getMessage()));
        }
    }
}
