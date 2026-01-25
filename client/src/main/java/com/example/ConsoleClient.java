package com.example;

import java.io.IOException;
import java.util.Scanner;

public class ConsoleClient {

    private final NetworkClient networkClient;
    private volatile boolean running = true;
    private Board localBoard = null; // local copy of the board

    public ConsoleClient(String host, int port) throws IOException {
        networkClient = new NetworkClient(host, port, this::handleServerMessage);
    }

    public void start() {
        networkClient.connect();

        Logger.info("Console client started. Type commands. Type 'help' for list of commands.");

        try (Scanner scanner = new Scanner(System.in)) {
            while (running) {
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) continue;

                String[] parts = input.split("\\s+");
                String cmd = parts[0].toLowerCase();
                String[] args = new String[parts.length - 1];
                System.arraycopy(parts, 1, args, 0, args.length);

                processCommand(cmd, args);
            }
        }
    }

    private void processCommand(String cmd, String[] args) {
        Message msg = new Message();

        switch (cmd) {
            case "help" -> printHelp();

            case "setname" -> {
                if (args.length < 1) { Logger.info("Usage: setname <username>"); return; }
                msg.type = MessageType.SET_NAME;
                msg.nick = args[0];
                networkClient.sendMessage(msg);
            }

            case "create" -> {
                if (args.length < 1) { Logger.info("Usage: create <roomName>"); return; }
                msg.type = MessageType.CREATE_ROOM;
                msg.roomName = args[0];
                networkClient.sendMessage(msg);
            }

            case "join" -> {
                if (args.length < 1) { Logger.info("Usage: join <roomId>"); return; }
                msg.type = MessageType.JOIN;
                msg.roomName = args[0];
                networkClient.sendMessage(msg);
            }

            case "leave" -> {
                msg.type = MessageType.LEAVE_ROOM;
                networkClient.sendMessage(msg);
            }

            case "rooms" -> {
                msg.type = MessageType.LIST_ROOMS;
                networkClient.sendMessage(msg);
            }

            case "players" -> {
                msg.type = MessageType.LIST_PLAYERS;
                networkClient.sendMessage(msg);
            }

            case "pick" -> {
                if (args.length < 1) { Logger.info("Usage: pick BLACK|WHITE"); return; }
                msg.type = MessageType.PICK_COLOR;
                msg.color = switch (args[0].toUpperCase()) {
                    case "BLACK" -> StoneColor.BLACK_STONE;
                    case "WHITE" -> StoneColor.WHITE_STONE;
                    default -> {
                        Logger.info("Invalid color: " + args[0]);
                        yield null;
                    }
                };
                if (msg.color != null) networkClient.sendMessage(msg);
            }

            case "swap" -> { msg.type = MessageType.SWAP; networkClient.sendMessage(msg); }
            case "accept" -> { msg.type = MessageType.ACCEPT_SWAP; networkClient.sendMessage(msg); }
            case "decline" -> { msg.type = MessageType.DECLINE_SWAP; networkClient.sendMessage(msg); }

            case "move" -> {
                if (args.length < 2) {
                    Logger.info("Usage: move <x> <y>");
                    return;
                }
                try {
                    int x = Integer.parseInt(args[0]);
                    int y = Integer.parseInt(args[1]);
                    msg.type = MessageType.MOVE;
                    msg.x = x;
                    msg.y = y;
                    networkClient.sendMessage(msg);
                } catch (NumberFormatException e) {
                    Logger.info("Invalid coordinates.");
                } catch (Exception e) {
                    Logger.error("Failed to create move.", e);
                }
            }

            case "pass" -> { msg.type = MessageType.PASS; networkClient.sendMessage(msg); }
            case "resign" -> { msg.type = MessageType.RESIGN; networkClient.sendMessage(msg); }

            case "begin" -> { msg.type = MessageType.BEGIN; networkClient.sendMessage(msg); }

            case "quit" -> {
                Logger.info("Exiting...");
                running = false;
                networkClient.disconnect();
            }

            default -> Logger.info("Unknown command: " + cmd);
        }
    }

    private void printHelp() {
        Logger.info("""
                Available commands:
                  setname <name>       - Set your username
                  create <roomName>    - Create a room
                  join <roomId>        - Join a room
                  leave                - Leave current room
                  rooms                - List all rooms
                  players              - List all players in your current room
                  pick <BLACK|WHITE>   - Pick your stone color
                  swap                 - Request color swap
                  accept               - Accept color swap
                  decline              - Decline color swap
                  move <x> <y>         - Make a move
                  pass                 - Pass turn
                  resign               - Resign game
                  begin                - Begin game (room owner)
                  quit                 - Exit client
                """);
    }

    /** Receives server messages asynchronously */
    private void handleServerMessage(Message msg) {
        Logger.info("Server message: " + msg.type);

        switch (msg.type) {
            case NICK_SET -> Logger.info("Your username is now: " + msg.nick);
            case ROOM_CREATED -> Logger.info("Room created: " + msg.roomName);
            case JOIN -> Logger.info("Joined room: " + msg.roomName);
            case LEAVE_ROOM -> Logger.info("Left room: " + msg.roomName);
            case ROOM_LIST -> {
                Logger.info("Available rooms:");
                if (msg.roomList != null) 
                    for (String r : msg.roomList)
                        Logger.info(r);
            }
            case PLAYER_LIST -> {
                Logger.info("Players in room:");
                if (msg.playerNames != null) for (String p : msg.playerNames) Logger.info(p);
            }
            case PICK_COLOR -> Logger.info("Color picked: " + msg.nick + " -> " + msg.color);
            case SWAP -> Logger.info("Swap requested by: " + msg.nick + " do you accept? ");
            case SWAP_DECLINED -> Logger.info("Swap declined.");
            case SWAP_ACCEPTED -> Logger.info("Swap accepted.");
            case BEGIN -> {
                Logger.info("Game started! Board is ready."); 
                localBoard = new Board(19);
                Logger.info("\n" + localBoard.boardToString());
            }
            case YOUR_TURN -> Logger.info("It's your turn.");
            case INVALID_MOVE -> Logger.info("Invalid move.");
            case BOARD_UPDATE -> {
                if (msg.boardState != null) {
                    if (localBoard == null)
                        localBoard = new Board(msg.boardState.length);
                    for (int r = 0; r < msg.boardState.length; r++) {
                        for (int c = 0; c < msg.boardState[r].length; c++) {
                            localBoard.setInterSec(r+1, c+1, msg.boardState[r][c]);
                        }
                    }
                    Logger.info("Board updated:");
                    Logger.info("\n" + localBoard.boardToString());
                } else {
                    Logger.warn("Received BOARD_UPDATE with null boardState!");
                }
            }
            case GAME_WON -> Logger.info("Game won!");
            case GAME_LOST -> Logger.info("Game lost!");
            case GAME_TIED -> Logger.info("Game tied!");
            case GAME_RESULT -> {
                Logger.info("Game ended!");
                Logger.info("Black:" + msg.blackScore);
                Logger.info("White: " + msg.whiteScore);
            }
            case ERROR -> Logger.error("Server error: " + msg.error);
            default -> Logger.info("Server message: " + msg.type);
        }
    }

    public static void main(String[] args) {
        try {
            new ConsoleClient("localhost", 1664).start();
        } catch (IOException e) {
            Logger.error("Failed to connect to server.", e);
        }
    }
}

