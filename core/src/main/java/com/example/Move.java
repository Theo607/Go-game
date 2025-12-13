package com.example;

import com.example.Color;
import com.example.exceptions.InvalidMove;

public class Move {
    private int x;
    private int y;
    private Color state;

    public Move(int x, int y, Color colour) throws InvalidMove {
        if (x < 1 || y < 1) throw new InvalidMove("This move has invalid coordinates.");
        this.x = x;
        this.y = y;
        this.state = colour;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public Color getState() { return state; }
}

