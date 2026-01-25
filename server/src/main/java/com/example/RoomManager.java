package com.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * This class manages creating/removing rooms, joining/leaving them, generating unique ID
 */
public class RoomManager {
    private final Map<String, Room> rooms = new HashMap<>();

    public synchronized Room createRoom(String roomName, ClientHandler owner) {
        String roomId = generateUniqueId();
        Room room = new Room(roomId, roomName, owner);
        rooms.put(roomId, room);
        return room;
    }

    public synchronized boolean joinRoom(String roomId, ClientHandler client) {
        if (client.getCurrentRoom() != null) return false;

        Room room = rooms.get(roomId);
        if (room != null) {
            boolean success = room.join(client);
            if (success) {
                client.setCurrentRoom(room);
                return true;
            }
        }
        return false;
    }

    public synchronized void leaveRoom(ClientHandler client) {
        Room room = client.getCurrentRoom();
        if (room == null) return;

        room.leave(client);
        client.setCurrentRoom(null);

        if (room.getPlayers().isEmpty()) {
            rooms.remove(room.getRoomId());
        } else if (room.getOwner() == client) {
            ClientHandler newOwner = room.getPlayers().get(0);
            room.setOwner(newOwner);

            Message m = new Message();
            m.type = MessageType.NEW_OWNER; // <-- add this to MessageType
            m.nick = newOwner.username;
            room.broadcast(m);
        }
    }

    public synchronized Room getRoom(String roomId) {
        return rooms.get(roomId);
    }

    public synchronized void removeRoom(String roomId) {
        rooms.remove(roomId);
    }

    private String generateUniqueId() {
        String id;
        do {
            id = UUID.randomUUID().toString().substring(0, 8);
        } while (rooms.containsKey(id));
        return id;
    }

    public synchronized List<String> listRooms() {
        return rooms.values().stream()
                .map(room -> room.getRoomId() + " - " + room.getRoomName())
                .collect(Collectors.toList());
    }
}

