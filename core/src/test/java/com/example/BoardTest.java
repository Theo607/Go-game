package com.example;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

public class BoardTest {

    @Test
    void boardToStringTest() {
        Board board = new Board(3);
        board.setInterSec(3, 1, InterSec.Black);
        board.setInterSec(1, 2, InterSec.White);
        assertEquals("", board.boardToString());
    }

    @Test
    void testOutOfBounds() {
        Board board = new Board(19);
        assertThrows(IndexOutOfBoundsException.class, () -> board.getInterSec(0, 5));
        assertThrows(IndexOutOfBoundsException.class, () -> board.getInterSec(20, 10));
    }
}