package com.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

public class Client {
    private static final String HOST = "localhost";
    private static final int PORT = 1664;
    private static String USERNAME;
    private static int boardSize = 19;

    public static void main(String[] args) {
        

        try (Socket socket = new Socket(HOST, PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                
                Scanner scanner = new Scanner(System.in);

                System.out.println("Type your username: ");
                USERNAME = scanner.nextLine();
                out.writeObject(USERNAME);
                System.out.println("Connected to server as " + USERNAME);
                String command;
                do { 
                    System.out.println("Type your move (row, column), 'pass' or 'resign'");
                    command = scanner.nextLine().trim().toLowerCase();
                    switch(command) {
                        case "pass":
                            out.writeObject("pass");
                            out.flush();
                            break;
                        case "resign":
                            out.writeObject("resign");
                            out.flush();
                            break;
                        default:
                            String[] parts = command.split("\\s+");
                            if (parts.length != 2) {
                                System.out.println("Enter row and column separated by space");
                                continue;
                            }
                            try {
                                int row = Integer.parseInt(parts[0]);
                                int column = Integer.parseInt(parts[1]);
                                if (row < 1 || row > boardSize || column < 1 || column > boardSize)
                                    System.out.println("Parameters out of bounds [1, " + boardSize + "]");
                                out.writeObject("move " + row + " " + column);
                                out.flush();
                            } catch (NumberFormatException e) {
                                System.out.println("Parameters should be positive integers");
                            }
                    }
                } while (!command.equalsIgnoreCase("resign"));
                System.out.println("Game ended. Player " + USERNAME + " resigned");
                scanner.close();
        } catch(ConnectException e) {
            System.err.println("Connection error: Server not found on " + HOST + ":" + PORT);
        } catch (IOException e) {
            System.err.println("I/O error ocured: " + e.getMessage());
        }
        System.out.println("Disconnected");
    }
}