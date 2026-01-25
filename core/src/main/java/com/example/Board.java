package com.example;

import java.io.Serializable;

public class Board implements Serializable{
    private final int size;
    private final StoneColor[][] field;

    public Board(int lines) {
        this.size = lines;
        this.field = new StoneColor[size][size];

        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                field[i][j] = StoneColor.EMPTY_STONE;
            }
        }
    }

    public boolean inBounds(int row, int col) {
        return row >= 1 && row <= size && col >= 1 && col <= size;
    }

    // Assuming 1 <= x, y <= size
    public StoneColor getInterSec(int row, int column) {
        if (inBounds(row, column)) {
            return field[row - 1][column - 1];
        }
        throw new IndexOutOfBoundsException("Coordinates out of bounds: row = " + row + ", column = " + column);
    }

    public void setInterSec(int row, int column, StoneColor state) {
        if (inBounds(row,column)) {
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

    public Board copy() {
        Board copy = new Board(size);
        for (int r = 1; r <= size; r++) {
            for (int c = 1; c <= size; c++) {
                copy.setInterSec(r, c, getInterSec(r, c));
            }
        }
        return copy;
    }

    public StoneColor[][] getStateCopy() {
        StoneColor[][] copy = new StoneColor[size][size];
        for (int r = 1; r <= size; r++) {
            for (int c = 1; c <= size; c++) {
                copy[r-1][c-1] = getInterSec(r, c);
            }
        }
        return copy;
    }

    public void restoreFrom(Board other) {
        for (int r = 1; r <= size; r++) {
            for (int c = 1; c <= size; c++) {
                setInterSec(r, c, other.getInterSec(r, c));
            }
        }
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (!(o instanceof Board b))
            return false;
        if (size != b.size)
            return false;

        for (int i = 1; i <= size; i++) {
            for (int j = 1; j <= size; j++) {
                if (getInterSec(i, j) != b.getInterSec(i, j))
                    return false;
            }
        }
        return true;
    }

    /*@Override
    public int hashCode() {
        return Objects.hash(size);
    }*/
}
