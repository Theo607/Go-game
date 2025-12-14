package com.example;

import com.example.exceptions.EmptyMove;
import com.example.exceptions.IncorrectBoardSize;

public class GameSession extends Thread {
    private final Room room;
    private final ClientHandler blackPlayer;
    private final ClientHandler whitePlayer;
    private final Game game;

    private boolean blackPassedLastTurn = false;
    private boolean whitePassedLastTurn = false;
    private boolean gameEnded = false;

    public GameSession(Room room) throws IncorrectBoardSize {
        this.room = room;

        // Determine players based on colors
        ClientHandler p1 = room.getPlayers().get(0);
        ClientHandler p2 = room.getPlayers().get(1);
        if (room.getColor(p1) == Color.BLACK) {
            blackPlayer = p1;
            whitePlayer = p2;
        } else {
            blackPlayer = p2;
            whitePlayer = p1;
        }

        this.game = new Game(19); // Standard 19x19 Go board
        room.start();
        broadcastToRoom("GAME_STARTED", blackPlayer.getUsername() + " (BLACK) goes first.");
    }

    @Override
    public void run() {
        ClientHandler currentPlayer = blackPlayer;

        while (!gameEnded) {
            sendBoardToPlayers();
            requestMove(currentPlayer);

            PlayerAction action = waitForAction(currentPlayer);

            if (action.isResign()) {
                handleResignation(currentPlayer);
            } else if (action.isPass()) {
                handlePass(currentPlayer);
            } else {
                handleMove(currentPlayer, action.getMove());
            }

            // Switch turns
            currentPlayer = (currentPlayer == blackPlayer) ? whitePlayer : blackPlayer;
        }

        broadcastToRoom("GAME_ENDED", "Thank you for playing!");
    }

    private void sendBoardToPlayers() {
        String boardStr = game.getCurrPosition().boardToString();
        blackPlayer.sendRequest(new ServerRequest("BOARD_UPDATE", boardStr));
        whitePlayer.sendRequest(new ServerRequest("BOARD_UPDATE", boardStr));
    }

    private void requestMove(ClientHandler player) {
        player.sendRequest(new ServerRequest("YOUR_TURN"));
        ClientHandler opponent = (player == blackPlayer) ? whitePlayer : blackPlayer;
        opponent.sendRequest(new ServerRequest("OPPONENT_TURN", player.getUsername()));
    }

    private PlayerAction waitForAction(ClientHandler player) {
        synchronized (player) {
            while (true) {
                PlayerAction action = player.consumeLatestAction();
                if (action != null) return action;
                try {
                    player.wait();
                } catch (InterruptedException e) {
                    return PlayerAction.resign(); // Treat interrupt as resignation
                }
            }
        }
    }

    private void handlePass(ClientHandler player) {
        if (player == blackPlayer) blackPassedLastTurn = true;
        else whitePassedLastTurn = true;

        broadcastToRoom("PLAYER_PASSED", player.getUsername());

        if (blackPassedLastTurn && whitePassedLastTurn) {
            gameEnded = true;
            broadcastToRoom("GAME_OVER", "Both players passed. Game ended.");
        }
    }

    private void handleResignation(ClientHandler player) {
        gameEnded = true;
        String winner = (player == blackPlayer) ? whitePlayer.getUsername() : blackPlayer.getUsername();
        broadcastToRoom("GAME_OVER", player.getUsername() + " resigned. " + winner + " wins!");
    }

    private void handleMove(ClientHandler player, Move move) {
        try {
            game.setCurrentMove(move);
            game.acceptMove();

            // Reset consecutive pass flags
            blackPassedLastTurn = false;
            whitePassedLastTurn = false;

            broadcastToRoom("PLAYER_MOVED", player.getUsername(),
                    String.valueOf(move.getX()), String.valueOf(move.getY()));
        } catch (EmptyMove e) {
            player.sendRequest(new ServerRequest("INVALID_MOVE", e.getMessage()));
        }
    }

    private void broadcastToRoom(String requestType, String... params) {
        for (ClientHandler player : room.getPlayers()) {
            player.sendRequest(new ServerRequest(requestType, params));
        }
    }
}
