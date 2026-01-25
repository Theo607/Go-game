package com.example;

/**
 * Enum of types of errors that can occur during making a move
 */
public enum MoveError {
    OUT_OF_BOUNDS,
    FIELD_OCCUPIED,
    SUICIDE,
    KO
}