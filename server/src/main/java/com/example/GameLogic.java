package com.example;

import java.util.ArrayList;
import java.util.List;

public class GameLogic {

    private final Room room;
    private final Board board;

    public GameLogic(Room room) {
        this.room = room;
        this.board = new Board(19); // default 19x19 board
    }

    /**
     * Attempt to play a move. Returns true if legal.
     */
    public boolean tryMove(Move move) {
        int x = move.getX();
        int y = move.getY();
        Color color = move.getState();

        // Check if coordinates are in bounds
        if (x < 1 || x > board.getSize() || y < 1 || y > board.getSize())
            return false;

        // Check if intersection is empty
        if (board.getInterSec(x, y) != Color.NONE)
            return false;

        // Tentatively place stone
        board.setInterSec(x, y, color);

        // Check for captures
        removeCapturedStones(opposite(color));

        // Check for suicide (if the stone has no liberties after placement)
        if (!hasLiberty(x, y)) {
            board.setInterSec(x, y, Color.NONE); // undo
            return false;
        }

        return true;
    }

    /**
     * Remove all opponent stones that have no liberties.
     */
    private void removeCapturedStones(Color opponent) {
        boolean[][] visited = new boolean[board.getSize()][board.getSize()];

        for (int i = 1; i <= board.getSize(); i++) {
            for (int j = 1; j <= board.getSize(); j++) {
                if (!visited[i - 1][j - 1] && board.getInterSec(i, j) == opponent) {
                    List<int[]> group = new ArrayList<>();
                    if (!groupHasLiberty(i, j, opponent, visited, group)) {
                        // remove stones
                        for (int[] pos : group) {
                            board.setInterSec(pos[0], pos[1], Color.NONE);
                        }
                    }
                }
            }
        }
    }

    /**
     * Check if a group has at least one liberty, DFS style.
     */
    private boolean groupHasLiberty(int x, int y, Color color, boolean[][] visited, List<int[]> group) {
        int size = board.getSize();
        if (x < 1 || x > size || y < 1 || y > size) return false;
        if (visited[x - 1][y - 1]) return false;

        visited[x - 1][y - 1] = true;
        Color current = board.getInterSec(x, y);
        if (current == Color.NONE) return true;
        if (current != color) return false;

        group.add(new int[]{x, y});

        // check 4 neighbors
        boolean hasLiberty = false;
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (nx >= 1 && nx <= size && ny >= 1 && ny <= size) {
                if (!visited[nx - 1][ny - 1]) {
                    if (board.getInterSec(nx, ny) == Color.NONE) hasLiberty = true;
                    else if (board.getInterSec(nx, ny) == color) {
                        hasLiberty |= groupHasLiberty(nx, ny, color, visited, group);
                    }
                }
            }
        }
        return hasLiberty;
    }

    private boolean hasLiberty(int x, int y) {
        boolean[][] visited = new boolean[board.getSize()][board.getSize()];
        List<int[]> group = new ArrayList<>();
        return groupHasLiberty(x, y, board.getInterSec(x, y), visited, group);
    }

    private Color opposite(Color color) {
        return color == Color.BLACK ? Color.WHITE : Color.BLACK;
    }

    public Board getBoard() {
        return board;
    }

}
