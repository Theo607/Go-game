package com.example;

import java.util.List;

/**
 * MoveResult is a class that tells whether the move is legal or not
 * It can contain a list of stones captured by that move
 */
public class MoveResult {

    private final boolean legal;
    private final List<Point> captured;
    private final MoveError error;

    private MoveResult(boolean legal, List<Point> captured, MoveError error) {
        this.legal = legal;
        this.captured = captured;
        this.error = error;
    }

    public static MoveResult legal(List<Point> captured) {
        return new MoveResult(true, List.copyOf(captured), null);
    }

    public static MoveResult illegal(MoveError error) {
        return new MoveResult(false, List.of(), error);
    }

    public boolean isLegal() {
        return legal;
    }

    public List<Point> getCaptured() {
        return captured;
    }

    public MoveError getError() {
        return error;
    }
}