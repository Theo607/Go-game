package com.example;

public enum InterSec {
    None, White, Black
}

public class Board {
    private int size = 19;
    private InterSec[][] field;

    public Board(int lines) {
        this.size = lines;
        field = new InterSec[size][size];

        // Initialize all squares to None
        for (int i = 0; i < size; i++) {
            for (int j = 0; j < size; j++) {
                field[i][j] = InterSec.None;
            }
        }
    }

    private boolean inBound(int n) {
        return n >= 1 && n <= size;
    }

    // Assuming 1 <= x, y <= size
    public InterSec getInterSec(int x, int y) {
        if (inBound(x) && inBound(y)) {
            return field[x - 1][y - 1];
        }
        throw new IndexOutOfBoundsException("Coordinates out of bounds: x=" + x + ", y=" + y);
    }

    public void setInterSec(int x, int y, InterSec state) {
        if (inBound(x) && inBound(y)) {
            field[x - 1][y - 1] = state;
        } else {
            throw new IndexOutOfBoundsException("Coordinates out of bounds: x=" + x + ", y=" + y);
        }
    }
}
