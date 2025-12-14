package com.example;

public class GameActionHandler {
    private final ClientHandler client;

    public GameActionHandler(ClientHandler client) {
        this.client = client;
    }

    public void handleCommand(ClientCommand command) {
        if (client.getCurrentRoom() == null || !client.getCurrentRoom().isStarted()) {
            client.sendRequest(new ServerRequest("GAME_NOT_STARTED"));
            return;
        }

        switch (command.getCommandType()) {
            case "MOVE" -> {
                try {
                    int x = Integer.parseInt(command.getParameters()[0]);
                    int y = Integer.parseInt(command.getParameters()[1]);
                    Move move = new Move(x, y, client.getCurrentRoom().getColor(client));
                    client.setLatestAction(PlayerAction.move(move));
                } catch (Exception e) {
                    client.sendRequest(new ServerRequest("INVALID_MOVE", "Coordinates invalid"));
                }
            }
            case "PASS" -> client.setLatestAction(PlayerAction.pass());
            case "RESIGN" -> client.setLatestAction(PlayerAction.resign());
        }
    }
}
