package com.example;

import com.example.Board;
import com.example.Game;
import com.example.exceptions.IncorrectBoardSize;

public class GameSession implements Runnable {

    private ClientHandler player1;
    private ClientHandler player2;
    private Game game;

    public GameSession(ClientHandler p1, ClientHandler p2) throws IncorrectBoardSize {
        this.player1 = p1;
        this.player2 = p2;
        this.game = new Game(9); // 9x9 board

        // send initial board to both players
        sendBoardToPlayers();
    }

    private void sendBoardToPlayers() {
        Board board = game.getCurrPosition();
        player1.sendBoard(board);
        player2.sendBoard(board);
    }

    @Override
    public void run() {
        // TODO: implement game loop
        // - wait for moves from each player
        // - update game
        // - send updated board to both players
    }
}

