package com.example;
import com.example.exceptions.InvalidMove;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

class MoveTest {

    @Test
    void testValidMove() throws InvalidMove {
        Move move = new Move(3, 4, Color.BLACK);
        assertEquals(3, move.getX());
        assertEquals(4, move.getY());
        assertEquals(Color.BLACK, move.getState());
    }

    @Test
    void testInvalidMove() {
        assertThrows(InvalidMove.class, () -> new Move(0, 0, Color.BLACK));
    }
}
