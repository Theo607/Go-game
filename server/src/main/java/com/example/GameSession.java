package com.example;

public class GameSession implements Runnable {

    private final Room room;
    private final GameLogic logic;

    public GameSession(Room room) {
        this.room = room;
        this.logic = new GameLogic(room);
    }

    @Override
    public void run() {
        ClientHandler blackPlayer = null;
        ClientHandler whitePlayer = null;

        // Determine colors
        for (ClientHandler p : room.getPlayers()) {
            if (room.getColor(p) == Color.BLACK) {
                blackPlayer = p;
            } else if (room.getColor(p) == Color.WHITE) {
                whitePlayer = p;
            }
        }

        if (blackPlayer == null || whitePlayer == null) {
            room.broadcast(new ServerRequest(
                    "ERROR", "Both players must have chosen colors."));
            return;
        }

        ClientHandler currentPlayer = blackPlayer;
        ClientHandler otherPlayer = whitePlayer;

        int consecutivePasses = 0;

        // Initial board
        broadcastBoard();

        while (true) {
            try {
                currentPlayer.sendRequest(
                        new ServerRequest("YOUR_TURN", currentPlayer.getUsername()));

                PlayerAction action = currentPlayer.waitForAction();

                // Resign
                if (action.isResign()) {
                    room.broadcast(new ServerRequest(
                            "GAME_OVER",
                            currentPlayer.getUsername() + " resigned."));
                    break;
                }

                // Pass
                if (action.isPass()) {
                    room.broadcast(new ServerRequest(
                            "PLAYER_PASSED",
                            currentPlayer.getUsername()));
                    consecutivePasses++;
                }
                // Move
                else {
                    Move move = action.getMove();
                    boolean legal = logic.tryMove(move);

                    if (!legal) {
                        currentPlayer.sendRequest(new ServerRequest(
                                "ILLEGAL_MOVE",
                                move.getX() + "," + move.getY()));
                        continue; // retry same player
                    }

                    room.broadcast(new ServerRequest(
                            "PLAYER_MOVED",
                            currentPlayer.getUsername(),
                            move.getX() + "," + move.getY()));

                    consecutivePasses = 0;
                }

                broadcastBoard();

                // End condition: double pass
                if (consecutivePasses >= 2) {
                    room.broadcast(new ServerRequest(
                            "GAME_OVER",
                            "Both players passed consecutively."));
                    break;
                }

                // Swap turns
                ClientHandler temp = currentPlayer;
                currentPlayer = otherPlayer;
                otherPlayer = temp;

            } catch (InterruptedException e) {
                room.broadcast(new ServerRequest(
                        "ERROR", "Game session interrupted."));
                break;
            }
        }
    }

    private void broadcastBoard() {
        String boardStr = logic.getBoard().boardToString();
        room.broadcast(new ServerRequest("BOARD_UPDATE", boardStr));
    }
}
