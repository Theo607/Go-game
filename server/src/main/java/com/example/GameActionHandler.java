package com.example;

/**
 * Handling game-related commands received from client
 */
public class GameActionHandler {

    private final ClientHandler client;

    public GameActionHandler(ClientHandler client) {
        this.client = client;
    }

    /**
     * Informing the client about actions performed
     * Showing current board, notifying about turns, game results
     * Adding actions to the queue of actions
     * @param msg Message to handle
     */
    public void handleMessage(Message msg) {

        switch (msg.type) {
            case BOARD_UPDATE -> {
                System.out.println(msg.board.boardToString());
                return;
            }
            case INFO -> {
                System.out.println("[INFO] " + msg.nick);
                return;
            }
            case YOUR_TURN -> {
                System.out.println("Your turn!");
                return;
            }
            case GAME_RESULT -> {
                System.out.println("Game ended");
                System.out.println("Black: " + msg.blackScore);
                System.out.println("White: " + msg.whiteScore);
                return;
            }
            case GAME_WON, GAME_LOST, GAME_TIED -> {
                System.out.println(msg.type);
                return;
            }
        }

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
        // Ensure the move has the correct color assigned from the player
        Move coloredMove = new Move(msg.x, msg.y, stoneColor);
        client.submitAction(PlayerAction.move(coloredMove));
    }

    private void sendError(String text) {
        Message m = new Message();
        m.type = MessageType.ERROR;
        m.error = text; // reuse nick field for errors
        client.sendMessage(m);
    }
}

