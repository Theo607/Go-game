package com.example;

import javafx.application.Application;

public class Client {

    public static void main(String[] args) {
        String mode = args.length > 0 ? args[0].toLowerCase() : "console";

        switch (mode) {
            case "console" -> {
                try {
                    ConsoleClient client = new ConsoleClient("localhost", 1664);
                    client.start();
                } catch (Exception e) {
                    Logger.error("Failed to start console client.", e);
                }
            }

            case "gui" -> {
                Logger.info("Starting GUI client...");
                try {
                    // Launch JavaFX application
                    Application.launch(GoFXClient.class, args);
                } catch (Exception e) {
                    Logger.error("Failed to start GUI client.", e);
                }
            }

            default -> {
                Logger.info("Unknown mode '" + mode + "'. Starting console client by default.");
                try {
                    ConsoleClient client = new ConsoleClient("localhost", 1664);
                    client.start();
                } catch (Exception e) {
                    Logger.error("Failed to start console client.", e);
                }
            }
        }
    }
}
