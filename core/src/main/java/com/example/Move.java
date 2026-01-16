package com.example;

import java.io.Serializable;
import com.example.exceptions.InvalidMove;

public class Move implements Serializable {
    private int x;
    private int y;
    private StoneColor state;

    public Move(int x, int y, StoneColor colour) throws InvalidMove {
        if (x < 1 || y < 1) throw new InvalidMove("This move has invalid coordinates.");
        this.x = x;
        this.y = y;
        this.state = colour;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public StoneColor getState() { return state; }
    public void setState(StoneColor c) { state = c; }
}

