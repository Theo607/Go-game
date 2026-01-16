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
    public List<int[]> tryMoveWithCaptures(Move move) {
        int x = move.getX();
        int y = move.getY();
        StoneColor color = move.getState();

        if (x < 1 || x > board.getSize() || y < 1 || y > board.getSize()) return null;
        if (board.getInterSec(x, y) != StoneColor.EMPTY_STONE) return null;

        board.setInterSec(x, y, color);

        // Track removed stones
        List<int[]> removed = removeCapturedStones(opposite(color));

        if (!hasLiberty(x, y)) {
            // Undo
            board.setInterSec(x, y, StoneColor.EMPTY_STONE);
            // Undo captured stones
            for (int[] pos : removed) {
                board.setInterSec(pos[0], pos[1], opposite(color));
            }
            return null; // illegal
        }

        return removed;
    }

    /** Remove all opponent stones that have no liberties */
    private List<int[]> removeCapturedStones(StoneColor opponent) {
        List<int[]> removed = new ArrayList<>();
        boolean[][] visited = new boolean[board.getSize()][board.getSize()];

        for (int i = 1; i <= board.getSize(); i++) {
            for (int j = 1; j <= board.getSize(); j++) {
                if (!visited[i - 1][j - 1] && board.getInterSec(i, j) == opponent) {
                    List<int[]> group = new ArrayList<>();
                    if (!groupHasLiberty(i, j, opponent, visited, group)) {
                        for (int[] pos : group) {
                            board.setInterSec(pos[0], pos[1], StoneColor.EMPTY_STONE);
                            removed.add(pos);
                        }
                    }
                }
            }
        }

        return removed;
    }


    /**
     * Check if a group has at least one liberty (DFS).
     */
    private boolean groupHasLiberty(int x, int y, StoneColor color, boolean[][] visited, List<int[]> group) {
        int size = board.getSize();
        if (x < 1 || x > size || y < 1 || y > size) return false;
        if (visited[x - 1][y - 1]) return false;

        visited[x - 1][y - 1] = true;
        StoneColor current = board.getInterSec(x, y);
        if (current == StoneColor.EMPTY_STONE) return true;
        if (current != color) return false;

        group.add(new int[]{x, y});

        boolean hasLiberty = false;
        int[][] dirs = {{-1, 0}, {1, 0}, {0, -1}, {0, 1}};
        for (int[] d : dirs) {
            int nx = x + d[0];
            int ny = y + d[1];
            if (nx >= 1 && nx <= size && ny >= 1 && ny <= size && !visited[nx - 1][ny - 1]) {
                if (board.getInterSec(nx, ny) == StoneColor.EMPTY_STONE) hasLiberty = true;
                else if (board.getInterSec(nx, ny) == color)
                    hasLiberty |= groupHasLiberty(nx, ny, color, visited, group);
            }
        }
        return hasLiberty;
    }

    /** Check if the stone at x,y has any liberties */
    private boolean hasLiberty(int x, int y) {
        boolean[][] visited = new boolean[board.getSize()][board.getSize()];
        List<int[]> group = new ArrayList<>();
        return groupHasLiberty(x, y, board.getInterSec(x, y), visited, group);
    }

    /** Get the opposite stone color */
    private StoneColor opposite(StoneColor color) {
        return switch (color) {
            case BLACK_STONE -> StoneColor.WHITE_STONE;
            case WHITE_STONE -> StoneColor.BLACK_STONE;
            default -> StoneColor.EMPTY_STONE;
        };
    }

    public Board getBoard() {
        return board;
    }
}

