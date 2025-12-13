package com.example;

import com.example.exceptions.IncorrectBoardSize;

public class GameSession implements Runnable {

    private ClientHandler player1;
    private ClientHandler player2;
    private Game game;
    private boolean passed1 = false;
    private boolean passed2 = false;
    private boolean turn = false; // false - player1 (black), true - player2 (white)

    public GameSession(ClientHandler p1, ClientHandler p2) throws IncorrectBoardSize {
        this.player1 = p1;
        this.player2 = p2;
        this.game = new Game(9); // 9x9 board

        // send initial board to both players
        //sendBoardToPlayers();
    }

    /*private void sendBoardToPlayers() {
        Board board = game.getCurrPosition();
        player1.sendBoard(board);
        player2.sendBoard(board);
    }*/

    @Override
    public void run() {
        // TODO: implement game loop
        // - wait for moves from each player
        // - update game
        // - send updated board to both players
        boolean validMove = false;
        String command;
        if(!turn) {
            while (!validMove) {
                Board board = game.getCurrPosition();
                player1.sendBoard(board);
                command = player1.sendTurn();
                if(command.equals("pass")) {
                    passed1 = true;
                    if (passed2 == true) {
                        player1.sendWon();
                        player2.sendLost();
                    }
                    validMove = true;
                } else {
                    passed1 = false;
                    passed2 = false;
                }
                if (command.equals("resign")) {
                    player1.sendLost();
                    player2.sendWon();
                    validMove = true;
                } else {
                    String[] parts = command.split(" ");
                    int row = Integer.parseInt(parts[1]);
                    int column = Integer.parseInt(parts[2]);
                    if (validMove(row, column, InterSec.Black)) {
                        board.setInterSec(row, column, InterSec.Black);
                        validMove = true;
                    }
                    else continue;
                }
            }
        } else {
            while (!validMove) {
                Board board = game.getCurrPosition();
                player2.sendBoard(board);
                command = player2.sendTurn();
                if(command.equals("pass")) {
                    passed2 = true;
                    if (passed1 == true) {
                        player2.sendWon();
                        player1.sendLost();
                    }
                }
            }
        }
    }

    public void pass(ClientHandler player) {}

    public void resigned(ClientHandler player) {
        if (player.equals(player1)) {}
    }
}

