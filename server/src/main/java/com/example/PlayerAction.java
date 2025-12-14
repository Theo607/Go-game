package com.example;

public class PlayerAction {
    private final Move move;    // null if pass
    private final boolean resign;

    private PlayerAction(Move move, boolean resign) {
        this.move = move;
        this.resign = resign;
    }

    public static PlayerAction move(Move move) {
        return new PlayerAction(move, false);
    }

    public static PlayerAction pass() {
        return new PlayerAction(null, false);
    }

    public static PlayerAction resign() {
        return new PlayerAction(null, true);
    }

    public Move getMove() { return move; }
    public boolean isPass() { return move == null && !resign; }
    public boolean isResign() { return resign; }
}
