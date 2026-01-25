package com.example;

import java.io.Serializable;

/**
 * Point is made of two parameters: abscissa and ordinate
 */
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