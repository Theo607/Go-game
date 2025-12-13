package com.example;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

public class RoomManager {
    private final Map<String, Room> rooms = new HashMap<>();

    public synchronized Room createRoom(String roomName, ClientHandler owner) {
        String roomId = generateUniqueId();
        Room room = new Room(roomId, roomName, owner);
        rooms.put(roomId, room);
        return room;
    }

    public synchronized boolean joinRoom(String roomId, ClientHandler client) {
        if (client.getCurrentRoom() != null) {
            return false;
        }

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
        if (room != null) {
            boolean ifOwner = false;
            if (room.getOwner() == client) {
                ifOwner = true;
            }
            room.leave(client); // remove player
            client.setCurrentRoom(null);

            // If the owner left, remove the room
            if (ifOwner) {
                rooms.remove(room.getRoomId());
            }
            // Optionally, if room is empty, also remove
            else if (room.getPlayers().isEmpty()) {
                rooms.remove(room.getRoomId());
            }
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
            id = UUID.randomUUID().toString().substring(0, 8); // short ID
        } while (rooms.containsKey(id));
        return id;
    }

    public synchronized List<String> listRooms() {
        return rooms.values().stream()
                .map(room -> room.getRoomId() + " - " + room.getRoomName())
                .collect(Collectors.toList());
    }
}
