package com.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class Logger {
    public enum Level {
        INFO, WARN, ERROR, DEBUG
    }

    private static final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static volatile Level currentLevel = Level.INFO;

    private Logger() { /*no instances*/ }

    public static void setLevel(Level level) {
        currentLevel = level;
    }

    private static synchronized void log(Level level, String message, Throwable throwable) {
        if(level.ordinal() < currentLevel.ordinal()) {
            return;
        }
        String timestamp = LocalDateTime.now().format(formatter);
        String thread_name = Thread.currentThread().getName();

        System.out.printf(
                "[%s] [%s] [%s] %s%n",
                timestamp,
                level,
                thread_name,
                message
                );

        if(throwable != null) {
            throwable.printStackTrace(System.out);
        }
    }

    public static void info(String message) {
        log(Level.INFO, message, null);
    }

    public static void warn(String message) {
        log(Level.WARN, message, null);
    }

    public static void error(String message) {
        log(Level.ERROR, message, null);
    }

    public static void error(String message, Throwable throwable) {
        log(Level.ERROR, message, throwable);
    }

    public static void debug(String message) {
        log(Level.DEBUG, message, null);
    }

}
