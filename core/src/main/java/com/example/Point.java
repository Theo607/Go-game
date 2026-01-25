package com.example;

import java.io.Serializable;

public class Point implements Serializable {

    int x, y;

    public Point (int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int x() {
        return this.x;
    }

    public int y() {
        return this.y;
    }
}