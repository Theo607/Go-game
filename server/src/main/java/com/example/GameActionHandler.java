package com.example;

public class GameActionHandler {

    private final ClientHandler client;

    public GameActionHandler(ClientHandler client) {
        this.client = client;
    }

    public void handleMessage(Message msg) {
        Room room = client.getCurrentRoom();

        if (room == null || !room.isStarted()) {
            sendError("Game has not started.");
            return;
        }

        StoneColor stoneColor = room.getColor(client);

        try {
            switch (msg.type) {
                case MOVE -> handleMove(msg, stoneColor);
                case PASS -> client.submitAction(PlayerAction.pass());
                case RESIGN -> client.submitAction(PlayerAction.resign());
                default -> sendError("Unknown game command: " + msg.type);
            }
        } catch (Exception e) {
            sendError("Invalid move: " + e.getMessage());
        }
    }

    private void handleMove(Message msg, StoneColor stoneColor) throws Exception {
        Move move = msg.move;

        if (move == null) {
            sendError("Move is missing in the message.");
            return;
        }

        // Ensure the move has the correct color assigned from the player
        Move coloredMove = new Move(move.getX(), move.getY(), stoneColor);
        client.submitAction(PlayerAction.move(coloredMove));
    }

    private void sendError(String text) {
        Message m = new Message();
        m.type = MessageType.ERROR;
        m.error = text; // reuse nick field for errors
        client.sendMessage(m);
    }
}

