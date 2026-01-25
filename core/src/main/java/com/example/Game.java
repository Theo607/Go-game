package com.example;

import java.util.Stack;

import com.example.exceptions.EmptyMove;
import com.example.exceptions.IncorrectBoardSize;

/**
 * This class contains stack of all moves made
 */
public class Game {
    private Board currentPosition;
    private Stack<Move> pastMoves;
    private Move currentMove;

    public Game(int boardSize) throws IncorrectBoardSize {
        if (boardSize < 2) throw new IncorrectBoardSize("The board size cannot be smaller than 2.");
        currentPosition = new Board(boardSize);
        pastMoves = new Stack<>();
        currentMove = null;
    }

    public Board getCurrPosition() {
        return currentPosition;
    }

    public void setCurrentMove(Move candidateMove) {
        currentMove = candidateMove;
    }

    public void acceptMove() throws EmptyMove {
        if (currentMove == null) throw new EmptyMove("The move you tried to accept was empty.");
        pastMoves.push(currentMove);
    }

    public void declineMove() {
        currentMove = null;
    }
}
