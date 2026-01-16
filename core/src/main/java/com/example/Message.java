package com.example;

import java.io.Serializable;
import java.util.List;

public class Message implements Serializable {
    public MessageType type;
    public Board board;
    public String nick;
    public String roomName;
    public String[] playerNames;
    public String[] roomList;
    public StoneColor color;
    public Move move;
    public String error;
    public List<int[]> removedStones;
}
