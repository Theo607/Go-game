package com.example;

import java.io.Serializable;

/**
 * Enum of types of messages
 */
public enum MessageType implements Serializable {
    SET_NAME,
    NICK_SET,
    CREATE_ROOM,
    ROOM_CREATED,
    JOIN,
    LEAVE_ROOM,
    LIST_ROOMS,
    ROOM_LIST,
    LIST_PLAYERS,
    PLAYER_LIST,
    PICK_COLOR,
    SWAP,
    ACCEPT_SWAP,
    DECLINE_SWAP,
    SWAP_ACCEPTED,
    SWAP_DECLINED,
    YOUR_TURN,
    BOARD_UPDATE,
    MOVE,
    PASS,
    RESIGN,
    BEGIN,
    GAME_WON,
    GAME_LOST,
    GAME_TIED,
    GAME_RESULT,
    UNKNOWN,
    NEW_OWNER,
    INFO,
    ERROR,
    INVALID_MOVE
}
