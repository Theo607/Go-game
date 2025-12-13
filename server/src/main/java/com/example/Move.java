package com.example;

import com.example.InterSec;
import com.example.exceptions.InvalidMove;

public class Move {
    private int x;
    private int y;
    private InterSec state;

    public Move(int x, int y, InterSec colour) throws InvalidMove {
        if (x < 1 || y < 1) throw new InvalidMove("This move has invalid coordinates.");
        this.x = x;
        this.y = y;
        this.state = colour;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public InterSec getState() { return state; }
}

