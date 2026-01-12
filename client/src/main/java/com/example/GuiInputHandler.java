package com.example;

import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.Arrays;

public class GuiInputHandler extends InputHandler {

    private Stage stage;
    private GoBoard goBoard;

    //username
    private TextField usernameField;
    private Button setNameButton;

    // Lobby / room
    private TextField roomNameField, roomIdField;
    private Button createRoomButton, joinRoomButton, leaveRoomButton, listRoomsButton;
    private ListView<String> roomList, playerList;

    // Game actions
    private Button pickBlackButton, pickWhiteButton;
    private Button requestSwapButton, acceptSwapButton, declineSwapButton;
    private Button beginButton, passButton, resignButton;

    public GuiInputHandler(ClientCommandSender sender, Client client) {
        super(sender, client);
        Platform.startup(() -> {
            stage = new Stage();
            createGui(stage);
            stage.show();
        });
    }

    private void createGui(Stage stage) {
        stage.setTitle("Go Game Client - " + client.getInputHandler().getClass().getSimpleName());

        BorderPane root = new BorderPane();
        root.setPadding(new Insets(10));

        // LEFT: Room & Player info
        VBox leftPanel = new VBox(10);
        leftPanel.setPadding(new Insets(5));

        usernameField = new TextField();
        setNameButton = new Button("Set Name");

        setNameButton.setOnAction(e -> {
            String name = usernameField.getText().trim();
            if (!name.isEmpty()) {
                commandSender.sendSetUsername(name);
                usernameField.clear();
            }
        });

        // Add to the top of leftPanel
        leftPanel.getChildren().addAll(
                new Label("Username:"), usernameField, setNameButton
                );

        roomNameField = new TextField();
        roomIdField = new TextField();
        createRoomButton = new Button("Create Room");
        joinRoomButton = new Button("Join Room");
        leaveRoomButton = new Button("Leave Room");
        listRoomsButton = new Button("List Rooms");

        roomList = new ListView<>();
        roomList.setPrefHeight(150);

        playerList = new ListView<>();
        playerList.setPrefHeight(100);

        leftPanel.getChildren().addAll(
                new Label("Room Name:"), roomNameField, createRoomButton,
                new Label("Room ID:"), roomIdField, joinRoomButton,
                listRoomsButton, leaveRoomButton,
                new Label("Available Rooms:"), roomList,
                new Label("Players in Room:"), playerList
        );

        root.setLeft(leftPanel);

        // CENTER: Go Board
        goBoard = new GoBoard(commandSender);
        root.setCenter(goBoard);

        // RIGHT: Game actions
        VBox rightPanel = new VBox(10);
        rightPanel.setPadding(new Insets(5));

        pickBlackButton = new Button("Pick BLACK");
        pickWhiteButton = new Button("Pick WHITE");
        requestSwapButton = new Button("Request Swap");
        acceptSwapButton = new Button("Accept Swap");
        declineSwapButton = new Button("Decline Swap");
        beginButton = new Button("Begin Game");
        passButton = new Button("Pass");
        resignButton = new Button("Resign");

        rightPanel.getChildren().addAll(
                pickBlackButton, pickWhiteButton,
                requestSwapButton, acceptSwapButton, declineSwapButton,
                beginButton, passButton, resignButton
        );

        root.setRight(rightPanel);

        // Setup actions
        createRoomButton.setOnAction(e -> {
            String name = roomNameField.getText().trim();
            if (!name.isEmpty()) commandSender.sendCreateRoom(name);
        });

        joinRoomButton.setOnAction(e -> {
            String id = roomIdField.getText().trim();
            if (!id.isEmpty()) commandSender.sendJoinRoom(id);
        });

        leaveRoomButton.setOnAction(e -> commandSender.sendLeaveRoom());
        listRoomsButton.setOnAction(e -> commandSender.sendListRooms());

        pickBlackButton.setOnAction(e -> commandSender.sendPickColor("BLACK"));
        pickWhiteButton.setOnAction(e -> commandSender.sendPickColor("WHITE"));
        requestSwapButton.setOnAction(e -> commandSender.sendRequestColorChange());
        acceptSwapButton.setOnAction(e -> commandSender.sendAcceptColorChange());
        declineSwapButton.setOnAction(e -> commandSender.sendDeclineColorChange());
        beginButton.setOnAction(e -> commandSender.sendBegin());
        passButton.setOnAction(e -> commandSender.sendPass());
        resignButton.setOnAction(e -> commandSender.sendResign());

        Scene scene = new Scene(root, 1100, 650);
        stage.setScene(scene);
    }

    @Override
    public void handleServerMessage(String message) {
        Platform.runLater(() -> {
            if (message.startsWith("BOARD_UPDATE")) {
                String boardStr = message.substring("BOARD_UPDATE: ".length());
                String[][] parsed = parseBoardString(boardStr); // implement parsing
                goBoard.updateBoard(parsed);
            } else if (message.startsWith("AVAILABLE_ROOMS")) {
                roomList.getItems().clear();
                String[] rooms = message.substring("AVAILABLE_ROOMS: ".length()).split(",");
                Arrays.stream(rooms).filter(s -> !s.isBlank()).forEach(roomList.getItems()::add);
            } else if (message.startsWith("ROOM_PLAYERS")) {
                playerList.getItems().clear();
                String[] players = message.substring("ROOM_PLAYERS: ".length()).split(",");
                Arrays.stream(players).filter(s -> !s.isBlank()).forEach(playerList.getItems()::add);
            } else if (message.startsWith("USERNAME_SET") || message.startsWith("ROOM_CREATED")
                    || message.startsWith("JOINED_ROOM") || message.startsWith("LEFT_ROOM")
                    || message.startsWith("COLOR_PICKED") || message.startsWith("COLORS_SWAPPED")
                    || message.startsWith("GAME_STARTED") || message.startsWith("PLAYER_PASSED")
                    || message.startsWith("PLAYER_RESIGNED") || message.startsWith("INVALID_MOVE")) {
                // optional: show messages on board or popup
                System.out.println(message); // for now
            }
        });
    }

    @Override
    public void runInputLoop() {
        // JavaFX is event-driven, no loop needed
    }

    // Convert server board string to 2D array for GoBoard
    private String[][] parseBoardString(String boardStr) {
        String[][] board = new String[19][19];
        String[] rows = boardStr.split("\n");
        for (int r = 0; r < Math.min(rows.length, 19); r++) {
            String[] cells = rows[r].split(" ");
            for (int c = 0; c < Math.min(cells.length, 19); c++) {
                board[r][c] = switch (cells[c]) {
                    case "B" -> "B";
                    case "W" -> "W";
                    default -> null;
                };
            }
        }
        return board;
    }
}

