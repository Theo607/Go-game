package com.example;

import java.io.Serializable;

public class Board implements Serializable{
    private int size = 19;
    private final StoneColor[][] field;

    public Board(int lines) {
        this.size = lines;
        field = new StoneColor[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                field[i][j] = StoneColor.EMPTY_STONE;
            }
        }
    }

    private boolean inBound(int n) {
        return n >= 1 && n <= size;
    }

    // Assuming 1 <= x, y <= size
    public StoneColor getInterSec(int row, int column) {
        if (inBound(row) && inBound(column)) {
            return field[row - 1][column - 1];
        }
        throw new IndexOutOfBoundsException("Coordinates out of bounds: row = " + row + ", column = " + column);
    }

    public void setInterSec(int row, int column, StoneColor state) {
        if (inBound(row) && inBound(column)) {
            field[row - 1][column - 1] = state;
        } else {
            throw new IndexOutOfBoundsException("Coordinates out of bounds: row = " + row + ", column = " + column);
        }
    }

    public String boardToString() {
        StringBuilder sb = new StringBuilder();

        // Column headers
        sb.append("   ");
        for (int col = 1; col <= size; col++) {
            sb.append(String.format("%2d ", col));
        }
        sb.append("\n");

        for (int row = 1; row <= size; row++) {
            sb.append(String.format("%2d ", row)); // row number
            for (int col = 1; col <= size; col++) {
                StoneColor field = getInterSec(row, col);
                char c = switch (field) {
                    case BLACK_STONE -> 'X';
                    case WHITE_STONE -> 'O';
                    default -> '.';
                };
                sb.append(" ").append(c).append(" ");
            }
            sb.append("\n");
        }

        return sb.toString();
    }

    public int getSize() {
        return size;
    }
}
