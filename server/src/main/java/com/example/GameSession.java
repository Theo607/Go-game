package com.example;

import java.util.List;
import java.util.Map;
import java.util.HashMap;

public class GameSession implements Runnable {

    private final Room room;
    private final GameLogic logic;

    private final Map<StoneColor, Integer> prisoners = new HashMap<>();

    public GameSession(Room room) {
        this.room = room;
        this.logic = room.getGameLogic();
        prisoners.put(StoneColor.BLACK_STONE, 0);
        prisoners.put(StoneColor.WHITE_STONE, 0);
    }

    @Override
    public void run() {
        ClientHandler blackPlayer = null;
        ClientHandler whitePlayer = null;

        // Assign players by color
        for (ClientHandler p : room.getPlayers()) {
            StoneColor color = room.getColor(p);
            if (color == StoneColor.BLACK_STONE) blackPlayer = p;
            else if (color == StoneColor.WHITE_STONE) whitePlayer = p;
        }

        if (blackPlayer == null || whitePlayer == null) {
            broadcastError("Both players must pick colors.");
            return;
        }

        ClientHandler currentPlayer = blackPlayer;
        ClientHandler otherPlayer = whitePlayer;
        int consecutivePasses = 0;

        // Initial board
        broadcastBoard();

        while (true) {
            try {
                // Notify current player it's their turn
                Message yourTurnMsg = new Message();
                yourTurnMsg.type = MessageType.YOUR_TURN;
                yourTurnMsg.nick = currentPlayer.username;
                currentPlayer.sendMessage(yourTurnMsg);

                // Wait for player action
                PlayerAction action = currentPlayer.waitForAction();

                // Resign
                if (action.isResign()) {
                    Message gameLost = new Message();
                    Message gameWon = new Message();
                    gameLost.type = MessageType.GAME_LOST;
                    gameWon.type = MessageType.GAME_WON;
                    currentPlayer.sendMessage(gameLost);
                    otherPlayer.sendMessage(gameWon);
                    break;
                }

                // Pass
                if (action.isPass()) {
                    broadcastInfo(currentPlayer.username + " passed.");
                    consecutivePasses++;

                    if (consecutivePasses >= 2) {
                        Message tie = new Message();
                        tie.type = MessageType.GAME_TIED;
                        room.broadcast(tie);
                        break; // exit loop immediately
                    }

                    // skip move logic and swap turns; next iteration handled in loop
                    ClientHandler temp = currentPlayer;
                    currentPlayer = otherPlayer;
                    otherPlayer = temp;
                    continue;
                }
                // Move
                else {
                    Move move = action.getMove();
                    move.setState(room.getColor(currentPlayer));

                    List<int[]> removedStones = logic.tryMoveWithCaptures(move);

                    if (removedStones == null) {
                        Message illegalMsg = new Message();
                        illegalMsg.type = MessageType.INVALID_MOVE;
                        currentPlayer.sendMessage(illegalMsg);
                        continue; // retry
                    }

                    // Broadcast move + removed stones
                    Message moveMsg = new Message();
                    moveMsg.type = MessageType.MOVE;
                    moveMsg.nick = currentPlayer.username;
                    moveMsg.move = move;
                    moveMsg.removedStones = removedStones;
                    room.broadcast(moveMsg);

                    consecutivePasses = 0;
                }

                // Swap turns
                ClientHandler temp = currentPlayer;
                currentPlayer = otherPlayer;
                otherPlayer = temp;

            } catch (InterruptedException e) {
                broadcastError("Game session interrupted.");
                break;
            }
        }
    }

    /** Broadcast the current board state to all players */
    private void broadcastBoard() {
        Message boardMsg = new Message();
        boardMsg.type = MessageType.BOARD_UPDATE;
        boardMsg.board = logic.getBoard();
        room.broadcast(boardMsg);
    }

    /** Helper to broadcast a simple text info message */
    private void broadcastInfo(String text) {
        Message msg = new Message();
        msg.type = MessageType.INFO; // add INFO to MessageType
        msg.nick = text;
        room.broadcast(msg);
    }

    /** Helper to broadcast errors */
    private void broadcastError(String text) {
        Message msg = new Message();
        msg.type = MessageType.ERROR;
        msg.nick = text;
        room.broadcast(msg);
    }

    private void addPrisoners(List<Point> captured, StoneColor capturedColor) {
        int current = prisoners.getOrDefault(capturedColor,0);
        prisoners.put(capturedColor, current + captured.size());
    }

    public Map<StoneColor, Integer> getPrisoners() {
        return Map.copyOf(prisoners);
    }

    private void endGameByPass() {

        broadcastInfo("Game ended by consecutive passes");

        Map<StoneColor, Integer> finalScore = logic.countTerritory(getPrisoners());
        int blackPoints = finalScore.get(StoneColor.BLACK_STONE);
        int whitePoints = finalScore.get(StoneColor.WHITE_STONE);

        Message result = new Message();
        result.type = MessageType.GAME_RESULT;
        result.blackScore = blackPoints;
        result.whiteScore = whitePoints;
        room.broadcast(result);
    }
}
