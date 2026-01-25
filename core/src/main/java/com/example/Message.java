package com.example;

import java.io.Serializable;
import java.util.List;

/**
 * Message with all possible attributes
 */
public class Message implements Serializable {
    public MessageType type;
    public Board board;
    public StoneColor[][] boardState;
    public String nick;
    public String roomName;
    public String[] playerNames;
    public String[] roomList;
    public StoneColor color;
    public Move move;
    public String error;
    public List<Point> removedStones;
    public int blackScore;
    public int whiteScore;
    public int x;
    public int y;
}
