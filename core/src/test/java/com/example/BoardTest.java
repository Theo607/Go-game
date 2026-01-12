package com.example;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Tag;

import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {

  @Test
  @Disabled
  @Tag("optional")
  void boardToStringTest() {
      Board board = new Board(3);
      // (row, col)
      board.setInterSec(2, 3, Color.BLACK); // row 2, col 3 -> X
      board.setInterSec(2, 1, Color.WHITE); // row 2, col 1 -> O

      String expected =
          "    1   2   3\n" +
          "  1 +---+---+\n" +
          "    |   |   |\n" +
          "  2 O---+---X\n" +
          "    |   |   |\n" +
          "  3 +---+---+";

      assertEquals(expected, board.boardToString().stripTrailing());
  }

  @Test
  void testOutOfBounds() {
    Board board = new Board(19);

    assertThrows(IndexOutOfBoundsException.class, () -> board.getInterSec(0, 5));
    assertThrows(IndexOutOfBoundsException.class, () -> board.getInterSec(20, 10));
  }
}
