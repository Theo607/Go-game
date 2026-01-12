package com.example;

import java.util.Arrays;

public class ClientRequestHandler {
    private final Client client;

    public ClientRequestHandler(Client client) {
        this.client = client;
    }

    public void handleRequest(ServerRequest request) {
        String type = request.getType();
        String[] params = request.getParameters();

        switch (type) {
            case "USERNAME_SET" -> client.printStr("Your username is now: " + params[0]);

            case "ROOM_CREATED" ->
                client.printStr("Room created: " + params[0] + " (ID: " + params[1] + ")");

            case "JOINED_ROOM" ->
                client.printStr("Successfully joined room: " + params[0]);

            case "JOIN_ROOM_FAILED" ->
                client.printStr("Failed to join room: " + params[0]);

            case "LEFT_ROOM" ->
                client.printStr("You left the room: " + params[0]);

            case "AVAILABLE_ROOMS" -> {
                client.printStr("Rooms available:");
                Arrays.stream(params).forEach(client::printStr);
            }

            case "NO_ROOMS_AVAILABLE" -> client.printStr("No rooms available.");

            case "ROOM_PLAYERS" -> {
                client.printStr("Players in your room:");
                Arrays.stream(params).forEach(client::printStr);
            }

            case "COLOR_PICKED" ->
                client.printStr(params[0] + " picked " + params[1] + " pieces.");

            case "COLOR_CHANGE_REQUEST" ->
                client.printStr(params[0] + " wants to swap colors. Type 'accept' or 'decline'.");

            case "COLORS_SWAPPED" -> client.printStr("Colors have been swapped.");

            case "COLOR_CHANGE_DECLINED" -> client.printStr("Color change was declined.");

            case "GAME_STARTED" -> {
                client.printStr("Game started!");
            }

            case "BOARD_UPDATE" -> {
                client.printStr("Board updated:");
                client.printStr(params[0]); // board string
            }

            case "YOUR_TURN" -> {
                client.printStr("It's your turn (" + params[0] + "). Make a move: move <x> <y>, pass, or resign.");
            }

            case "OPPONENT_TURN" -> {
                client.printStr("Waiting for opponent's turn (" + params[0] + ")...");
            }

            case "MOVE_MADE" -> client.printStr(params[0] + " played at (" + params[1] + ", " + params[2] + ").");

            case "PLAYER_PASSED" -> client.printStr(params[0] + " passed their turn.");

            case "PLAYER_RESIGNED" -> client.printStr(params[0] + " resigned. Game over.");

            case "GAME_OVER" -> {
                client.printStr("Game over!");
                if (params.length > 0) client.printStr("Winner: " + params[0]);
            }

            case "INVALID_MOVE" -> client.printStr("Invalid move: " + params[0]);

            default -> {
                client.printStr("Server message (" + type + "): " + String.join(", ", params));
            }
        }
        if (client.getInputHandler() instanceof GuiInputHandler gui) {
            gui.handleServerMessage(type + ": " + Arrays.toString(params));
        }
    }
}
