package com.example;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class ConsoleInputHandler {
    private final ClientCommandSender commandSender;
    private final Client client;

    private final Map<String, Command> commandMap = new HashMap<>();

    public ConsoleInputHandler(ClientCommandSender commandSender, Client client) {
        this.commandSender = commandSender;
        this.client = client;
        initCommands();
    }

    private void initCommands() {
        // Room and username commands
        commandMap.put("setname", args -> {
            if (args.length >= 1)
                commandSender.sendSetUsername(args[0]);
            else
                client.printStr("Usage: setname <username>");
        });

        commandMap.put("create", args -> {
            if (args.length >= 1)
                commandSender.sendCreateRoom(args[0]);
            else
                client.printStr("Usage: create <roomName>");
        });

        commandMap.put("join", args -> {
            if (args.length >= 1)
                commandSender.sendJoinRoom(args[0]);
            else
                client.printStr("Usage: join <roomId>");
        });

        commandMap.put("leave", args -> commandSender.sendLeaveRoom());
        commandMap.put("list", args -> commandSender.sendListRooms());
        commandMap.put("begin", args -> commandSender.sendStart());
        commandMap.put("pick", args -> {
            if (args.length >= 1)
                commandSender.sendPickColor(args[0].toUpperCase());
            else
                client.printStr("Usage: pick BLACK|WHITE");
        });

        // Color change commands
        commandMap.put("swap", args -> commandSender.sendRequestColorChange());
        commandMap.put("accept", args -> commandSender.sendAcceptColorChange());
        commandMap.put("decline", args -> commandSender.sendDeclineColorChange());

        // Game commands
        commandMap.put("move", args -> {
            if (args.length >= 2) {
                try {
                    int x = Integer.parseInt(args[0]);
                    int y = Integer.parseInt(args[1]);
                    commandSender.sendMove(x, y);
                } catch (NumberFormatException e) {
                    client.printStr("Invalid coordinates.");
                }
            } else {
                client.printStr("Usage: move <x> <y>");
            }
        });

        commandMap.put("pass", args -> commandSender.sendPass());
        commandMap.put("resign", args -> commandSender.sendResign());
        commandMap.put("start", args -> commandSender.sendStart());
        commandMap.put("begin", args -> commandSender.sendBegin());

        // Quit
        commandMap.put("quit", args -> {
            client.printStr("Exiting...");
            client.kill();
        });
    }

    public void runInputLoop() {
        client.printStr("""
                Enter commands:
                  setname <name>       - Set your username before joining or creating rooms
                  create <roomName>    - Create a new room (requires username)
                  join <roomId>        - Join an existing room (requires username)
                  leave                - Leave your current room
                  list                 - List all rooms or players in your current room
                  pick <BLACK|WHITE>   - Choose your color in the room
                  swap                 - Request to swap colors with the other player
                  accept               - Accept a pending color swap request
                  decline              - Decline a pending color swap request
                  begin                - Start the game (room owner only)
                  move <x> <y>         - Make a move on the board
                  pass                 - Pass your turn
                  resign               - Resign from the game
                  quit                 - Exit the client
                """);

        try (Scanner scanner = new Scanner(System.in)) { // <-- scanner auto-closed
            while (client.isRunning()) {
                String input = scanner.nextLine().trim();
                if (input.isEmpty())
                    continue;

                String[] parts = input.split("\\s+");
                String cmd = parts[0].toLowerCase();
                String[] args = new String[parts.length - 1];
                System.arraycopy(parts, 1, args, 0, args.length);

                Command command = commandMap.get(cmd);
                if (command != null) {
                    command.execute(args);
                } else {
                    client.printStr("Unknown command: " + cmd);
                }

                if ("quit".equals(cmd))
                    break; // stop loop after quit
            }
        }
    }

    // Functional interface for commands
    @FunctionalInterface
    private interface Command {
        void execute(String[] args);
    }
}
