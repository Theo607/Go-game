package com.example;

import java.io.IOException;
import java.io.ObjectOutputStream;

public class ClientCommandSender {
    private final ObjectOutputStream out;

    public ClientCommandSender(ObjectOutputStream out) {
        this.out = out;
    }

    public void sendCommand(String commandType, String... params) {
        try {
            out.writeObject(new ClientCommand(commandType, params));
            out.flush();
        } catch (IOException e) {
            System.out.println("Failed to send command: " + commandType);
        }
    }

    // Convenience methods
    public void sendSetUsername(String name) { sendCommand("SET_USERNAME", name); }
    public void sendCreateRoom(String roomName) { sendCommand("CREATE_ROOM", roomName); }
    public void sendJoinRoom(String roomId) { sendCommand("JOIN_ROOM", roomId); }
    public void sendLeaveRoom() { sendCommand("LEAVE_ROOM"); }
    public void sendListRooms() { sendCommand("LIST_ROOMS"); }
    public void sendPickColor(String color) { sendCommand("PICK_COLOR", color); }
    public void sendRequestColorChange() { sendCommand("REQUEST_COLOR_CHANGE"); }
    public void sendAcceptColorChange() { sendCommand("ACCEPT_COLOR_CHANGE"); }
    public void sendDeclineColorChange() { sendCommand("DECLINE_COLOR_CHANGE"); }
    public void sendMove(int x, int y) { sendCommand("MOVE", String.valueOf(x), String.valueOf(y)); }
    public void sendPass() { sendCommand("PASS"); }
    public void sendResign() { sendCommand("RESIGN"); }
    public void sendStart() { sendCommand("START"); }
    public void sendBegin() { sendCommand("BEGIN"); }
}
