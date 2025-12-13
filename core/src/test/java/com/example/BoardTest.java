package com.example;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Test;

public class BoardTest {

    @Test
    void boardToStringTest() {
        Board board = new Board(3);
        board.setInterSec(3, 1, InterSec.Black);
        board.setInterSec(1, 2, InterSec.White);
        String expected = "    1  2  3 \n"
                         +" 1  .  X  . \n"
                         +" 2  .  .  . \n"
                         +" 3  O  .  . \n";
        assertEquals(expected, board.boardToString());
    }

    @Test
    void testOutOfBounds() {
        Board board = new Board(19);
        assertThrows(IndexOutOfBoundsException.class, () -> board.getInterSec(0, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> board.getInterSec(20, 10));
    }
}