package com.example;

import com.example.exceptions.EmptyMove;
import com.example.exceptions.IncorrectBoardSize;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class GameTest {

    private Game game;

    @BeforeEach
    void setup() throws IncorrectBoardSize {
        game = new Game(5); // initialize a 5x5 board
    }

    @Test
    void testConstructorValidSize() {
        assertDoesNotThrow(() -> new Game(5));
    }

    @Test
    void testConstructorInvalidSize() {
        IncorrectBoardSize ex = assertThrows(IncorrectBoardSize.class, () -> new Game(1));
        assertEquals("The board size cannot be smaller than 2.", ex.getMessage());
    }

    @Test
    void testAcceptMove() throws Exception {
        Move move = new Move(1, 1, Color.WHITE); // match your enum case
        game.setCurrentMove(move);
        assertDoesNotThrow(() -> game.acceptMove());
    }

    @Test
    void testAcceptMoveEmpty() {
        EmptyMove ex = assertThrows(EmptyMove.class, () -> game.acceptMove());
        assertEquals("The move you tried to accept was empty.", ex.getMessage());
    }
}
