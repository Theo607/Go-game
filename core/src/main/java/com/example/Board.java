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
    public InterSec getInterSec(int row, int column) {
        if (inBound(row) && inBound(column)) {
            return field[row - 1][column - 1];
        }
        throw new IndexOutOfBoundsException("Coordinates out of bounds: row=" + row + ", column=" + column);
    }

    public void setInterSec(int row, int column, InterSec state) {
        if (inBound(row) && inBound(column)) {
            field[row - 1][column - 1] = state;
        } else {
            throw new IndexOutOfBoundsException("Coordinates out of bounds: row=" + row + ", column=" + column);
        }
    }

    public String boardToString() {
      StringBuilder sb = new StringBuilder();

      sb.append("    ");
      for (int col = 1; col <= size; col++) {
          sb.append(col).append("   ");
      }
      sb.append("\n");

      for (int row = 1; row <= size; row++) {

          sb.append(String.format("%3d ", row));
          for (int col = 1; col <= size; col++) {
              InterSec field = getInterSec(row, col);

              char c;
              switch (field) {
                  case Black -> c = 'O';
                  case White -> c = 'X';
                  default -> c = '+';
              }

              sb.append(c);

              if (col < size) {
                  sb.append("---");
              }
          }
          sb.append("\n");

          if (row < size) {
              sb.append("    ");
              for (int col = 1; col <= size; col++) {
                  sb.append("|");
                  if (col < size) sb.append("   ");
              }
              sb.append("\n");
          }
      }

      return sb.toString();
  }
}
