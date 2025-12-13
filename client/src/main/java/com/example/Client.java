package com.example;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.util.Scanner;

public class Client{
    private static final String HOST = "localhost";
    private static final int PORT = 4484;
    private static String USERNAME;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);

        System.out.println("Type your username: ");
        USERNAME = scanner.nextLine();

        try (Socket socket = new Socket(HOST, PORT);
            ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
            ObjectInputStream in = new ObjectInputStream(socket.getInputStream())) {
                
                System.out.println("Connected to server as " + USERNAME);
                do { 
                    System.out.println("Type your move (row, column), 'pass' or 'resign");
                } while (!scanner.nextLine().equals("resign"));
                System.out.println("Game ended");
                socket.close();
        } catch(ConnectException e) {
            System.err.println("Connection error: Server not found on " + HOST + ":" + PORT);
        } catch (IOException e) {
            System.err.println("I/O error ocured: " + e.getMessage());
        }
        System.out.println("Disconnected");
    }
}