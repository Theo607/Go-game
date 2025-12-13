package com.example;
import com.example.exceptions.InvalidMove;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MoveTest {

    @Test
    void testValidMove() throws InvalidMove {
        Move move = new Move(3, 4, InterSec.Black);
        assertEquals(3, move.getX());
        assertEquals(4, move.getY());
        assertEquals(InterSec.Black, move.getState());
    }

    @Test
    void testInvalidMove() {
        assertThrows(InvalidMove.class, () -> new Move(0, 0, InterSec.Black));
    }
}
