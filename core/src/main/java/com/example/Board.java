package com.example;
import java.io.Serializable;

public class Board implements Serializable{
    private int size = 19;
    private final InterSec[][] field;

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

    public String boardToString() {
        StringBuilder sb = new StringBuilder();
        sb.append("   ");
        
        for (int i = 0; i < size; i++) {
            if (i < 9) sb.append("  ");
            else sb.append(" ");
            sb.append(i+1);
        }
        sb.append("\n");
        
        for (int x = 0; x < size; x++) {
            for (int y = 0; y < size; y++) {
                
                InterSec field = getInterSec(x+1, y+1);
                if (x < 9) sb.append("  ");
                else sb.append(" ");
                sb.append(x+1);
                
                switch(field) {
                    case Black:
                        sb.append(" O ");
                        break;
                    case White:
                        sb.append(" X ");
                        break;
                    default:
                        sb.append(" . ");
                        break;
                }
            }
            sb.append("\n");
        }
        return sb.toString();
    }
}