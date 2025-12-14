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
        if (room == null)
            return;

        room.leave(client); // remove player
        client.setCurrentRoom(null);

        if (room.getPlayers().isEmpty()) {
            // No players left → remove room
            rooms.remove(room.getRoomId());
        } else if (room.getOwner() == client) {
            // Owner left → transfer ownership to the first remaining player
            ClientHandler newOwner = room.getPlayers().get(0);
            room.setOwner(newOwner); // Need to add setter in Room class
            room.broadcast(new ServerRequest("NEW_OWNER", newOwner.getUsername()));
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
