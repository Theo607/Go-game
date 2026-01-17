package com.example;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.stage.Stage;

public class GoFXClient extends Application {

    private NetworkClient networkClient;
    private Board localBoard = null;

    private String username = null;
    private boolean inRoom = false;

    private VBox root;
    private TextArea logArea;
    private GridPane boardGrid;

    private TextField nickField;
    private Button setNickBtn;

    private TextField roomField;
    private Button createRoomBtn;
    private Button joinRoomBtn;
    private Button leaveRoomBtn;

    private Button pickBlackBtn;
    private Button pickWhiteBtn;

    private Button swapBtn;
    private Button acceptSwapBtn;
    private Button declineSwapBtn;

    private Button passBtn;
    private Button resignBtn;


    private Button beginBtn;

    private int boardSize = 19;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        root = new VBox(10);
        root.setPadding(new Insets(10));
        root.setAlignment(Pos.TOP_CENTER);

        // --- Log area ---
        logArea = new TextArea();
        logArea.setEditable(false);
        logArea.setPrefHeight(200);

        // --- Nickname input ---
        HBox nickBox = new HBox(5);
        nickBox.setAlignment(Pos.CENTER);
        nickField = new TextField();
        nickField.setPromptText("Enter nickname");
        setNickBtn = new Button("Set Nickname");
        nickBox.getChildren().addAll(nickField, setNickBtn);

        // --- Room controls ---
        HBox roomBox = new HBox(5);
        roomBox.setAlignment(Pos.CENTER);
        roomField = new TextField();
        roomField.setPromptText("Room name / ID");
        createRoomBtn = new Button("Create Room");
        joinRoomBtn = new Button("Join Room");
        leaveRoomBtn = new Button("Leave Room");
        roomBox.getChildren().addAll(roomField, createRoomBtn, joinRoomBtn, leaveRoomBtn);

        // --- Color pick ---
        HBox colorBox = new HBox(5);
        colorBox.setAlignment(Pos.CENTER);
        pickBlackBtn = new Button("Pick Black");
        pickWhiteBtn = new Button("Pick White");
        colorBox.getChildren().addAll(pickBlackBtn, pickWhiteBtn);

        // --- Swap controls ---
        HBox swapBox = new HBox(5);
        swapBox.setAlignment(Pos.CENTER);
        swapBtn = new Button("Request Swap");
        acceptSwapBtn = new Button("Accept Swap");
        declineSwapBtn = new Button("Decline Swap");
        swapBox.getChildren().addAll(swapBtn, acceptSwapBtn, declineSwapBtn);
        HBox listBox = new HBox(5);
        listBox.setAlignment(Pos.CENTER);
        Button listRoomsBtn = new Button("List Rooms");

        Button listPlayersBtn = new Button("List Players");
        
        listBox.getChildren().addAll(listPlayersBtn, listRoomsBtn);

        HBox gameActionBox = new HBox(5);
        gameActionBox.setAlignment(Pos.CENTER);
        passBtn = new Button("Pass");
        resignBtn = new Button("Resign");
        gameActionBox.getChildren().addAll(passBtn, resignBtn);



        // --- Begin button ---
        beginBtn = new Button("Begin Game");

        // --- Board grid ---
        boardGrid = new GridPane();
        boardGrid.setHgap(2);
        boardGrid.setVgap(2);
        boardGrid.setAlignment(Pos.CENTER);
        initBoardGrid();

        root.getChildren().addAll(
                nickBox, roomBox, colorBox, swapBox, listBox, gameActionBox, beginBtn, boardGrid, logArea
        );

        // --- Setup network ---
        try {
            networkClient = new NetworkClient("localhost", 1664, this::handleServerMessage);
            networkClient.connect();
            log("Connected to server.");
        } catch (Exception e) {
            log("Failed to connect: " + e.getMessage());
        }

        // --- Button actions ---
        setNickBtn.setOnAction(e -> setNickname());
        createRoomBtn.setOnAction(e -> createRoom());
        joinRoomBtn.setOnAction(e -> joinRoom());
        leaveRoomBtn.setOnAction(e -> leaveRoom());

        pickBlackBtn.setOnAction(e -> pickColor(StoneColor.BLACK_STONE));
        pickWhiteBtn.setOnAction(e -> pickColor(StoneColor.WHITE_STONE));

        swapBtn.setOnAction(e -> sendCommand(MessageType.SWAP));
        acceptSwapBtn.setOnAction(e -> sendCommand(MessageType.ACCEPT_SWAP));
        declineSwapBtn.setOnAction(e -> sendCommand(MessageType.DECLINE_SWAP));

        listRoomsBtn.setOnAction(e -> {
            Message msg = new Message();
            msg.type = MessageType.LIST_ROOMS;
            networkClient.sendMessage(msg);
        });
        listPlayersBtn.setOnAction(e -> {
            Message msg = new Message();
            msg.type = MessageType.LIST_PLAYERS;
            networkClient.sendMessage(msg);
        });

        beginBtn.setOnAction(e -> sendCommand(MessageType.BEGIN));

        // --- Disable buttons initially ---
        updateButtonStates();

        Scene scene = new Scene(root, 1000, 800);
        stage.setScene(scene);
        stage.setTitle("Go Game Client");
        stage.show();
    }

    // --- Button logic ---
    private void updateButtonStates() {
        boolean hasNick = username != null;
        boolean roomSelected = inRoom;

        createRoomBtn.setDisable(!hasNick);
        joinRoomBtn.setDisable(!hasNick);
        leaveRoomBtn.setDisable(!roomSelected);

        pickBlackBtn.setDisable(!roomSelected);
        pickWhiteBtn.setDisable(!roomSelected);

        swapBtn.setDisable(!roomSelected);
        acceptSwapBtn.setDisable(!roomSelected);
        declineSwapBtn.setDisable(!roomSelected);

        beginBtn.setDisable(!roomSelected);
    }

    private void setNickname() {
        String nick = nickField.getText().trim();
        if (nick.isEmpty()) return;
        username = nick;
        Message msg = new Message();
        msg.type = MessageType.SET_NAME;
        msg.nick = nick;
        networkClient.sendMessage(msg);
        updateButtonStates();
    }

    private void createRoom() {
        String roomName = roomField.getText().trim();
        if (roomName.isEmpty()) return;
        Message msg = new Message();
        msg.type = MessageType.CREATE_ROOM;
        msg.roomName = roomName;
        networkClient.sendMessage(msg);
    }

    private void joinRoom() {
        String roomId = roomField.getText().trim();
        if (roomId.isEmpty()) return;
        Message msg = new Message();
        msg.type = MessageType.JOIN;
        msg.roomName = roomId;
        networkClient.sendMessage(msg);
    }

    private void leaveRoom() {
        Message msg = new Message();
        msg.type = MessageType.LEAVE_ROOM;
        networkClient.sendMessage(msg);
        inRoom = false;
        updateButtonStates();
    }

    private void pickColor(StoneColor color) {
        Message msg = new Message();
        msg.type = MessageType.PICK_COLOR;
        msg.color = color;
        networkClient.sendMessage(msg);
    }

    private void sendCommand(MessageType type) {
        Message msg = new Message();
        msg.type = type;
        networkClient.sendMessage(msg);
    }

    // --- Board setup ---
    private void initBoardGrid() {
        boardGrid.getChildren().clear();
        for (int r = 0; r < boardSize; r++) {
            for (int c = 0; c < boardSize; c++) {
                Button cell = new Button("+");
                cell.setPrefSize(30, 30);
                int row = r + 1, col = c + 1;
                cell.setOnAction(e -> sendMove(row, col));
                boardGrid.add(cell, c, r);
            }
        }
    }

    private void sendMove(int x, int y) {
        if (localBoard == null) return;
        Message msg = new Message();
        msg.type = MessageType.MOVE;
        try {
            msg.move = new Move(x, y, null);
        } catch (Exception e) {
            Logger.info("Invalid move: " + e.getMessage());
        }
        networkClient.sendMessage(msg);
    }

    private void updateBoardGrid(Board board) {
        for (int r = 1; r <= board.getSize(); r++) {
            for (int c = 1; c <= board.getSize(); c++) {
                StoneColor s = board.getInterSec(r, c);
                Button cell = (Button) getNodeByRowColumnIndex(r - 1, c - 1, boardGrid);
                if (cell != null) {
                    cell.setText(s == StoneColor.BLACK_STONE ? "X" :
                                 s == StoneColor.WHITE_STONE ? "O" : "+");
                }
            }
        }
    }

    private javafx.scene.Node getNodeByRowColumnIndex(int row, int column, GridPane gridPane) {
        for (javafx.scene.Node node : gridPane.getChildren()) {
            if (GridPane.getRowIndex(node) == row && GridPane.getColumnIndex(node) == column) return node;
        }
        return null;
    }

    // --- Logging ---
    private void log(String text) {
        Platform.runLater(() -> logArea.appendText(text + "\n"));
    }

    // --- Handle server messages ---
    private void handleServerMessage(Message msg) {
        Platform.runLater(() -> {
            log("Server message: " + msg.type);

            switch (msg.type) {
                case NICK_SET -> {
                    username = msg.nick;
                    log("Nickname set: " + username);
                    updateButtonStates();
                }
                case ROOM_CREATED, JOIN -> {
                    inRoom = true;
                    log("Joined room: " + msg.roomName);
                    updateButtonStates();
                }
                case LEAVE_ROOM -> {
                    inRoom = false;
                    log("Left room: " + msg.roomName);
                    updateButtonStates();
                }
                case PICK_COLOR -> log("Color picked: " + msg.nick + " -> " + msg.color);
                case BEGIN -> {
                    log("Game started!");
                    localBoard = new Board(boardSize);
                    updateBoardGrid(localBoard);
                }
                case MOVE -> {
                    if (localBoard == null) break;
                    int x = msg.move.getX();
                    int y = msg.move.getY();
                    StoneColor color = msg.move.getState();
                    localBoard.setInterSec(x, y, color);

                    // Remove captured stones
                    if (msg.removedStones != null) {
                        for (int[] pos : msg.removedStones) {
                            localBoard.setInterSec(pos[0], pos[1], StoneColor.EMPTY_STONE);
                        }
                    }

                    updateBoardGrid(localBoard);
                    log(msg.nick + " played: " + x + "," + y);
                }
                case BOARD_UPDATE -> {
                    if (msg.board != null) {
                        localBoard = msg.board;
                        updateBoardGrid(localBoard);
                    }
                }
                case YOUR_TURN -> log("It's your turn.");
                case INVALID_MOVE -> log("Invalid move.");
                case INFO -> log(msg.nick);
                case ERROR -> log("Server error: " + msg.error);
                case PLAYER_LIST -> {
                    log("Players: ");
                    for(String p : msg.playerNames) log(p);
                }
                case ROOM_LIST -> {
                    log("Rooms: ");
                    for(String r : msg.roomList) log(r);
                }
            }
        });
    }
}

