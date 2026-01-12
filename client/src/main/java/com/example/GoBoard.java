package com.example;

import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.MouseEvent;
import javafx.scene.paint.Color;

public class GoBoard extends Canvas {
    private static final int SIZE = 19;
    private static final int CELL_SIZE = 30;
    private final String[][] board = new String[SIZE][SIZE];
    private final ClientCommandSender commandSender;

    public GoBoard(ClientCommandSender sender) {
        super(SIZE * CELL_SIZE, SIZE * CELL_SIZE);
        this.commandSender = sender;
        drawBoard();

        this.addEventHandler(MouseEvent.MOUSE_CLICKED, this::handleClick);
    }

    private void handleClick(MouseEvent e) {
        int col = (int)(e.getX() / CELL_SIZE);
        int row = (int)(e.getY() / CELL_SIZE);

        commandSender.sendMove(row + 1, col + 1); // 1-indexed for server
    }

    public void updateBoard(String[][] newBoard) {
        for (int r = 0; r < SIZE; r++)
            System.arraycopy(newBoard[r], 0, board[r], 0, SIZE);
        drawBoard();
    }

    private void drawBoard() {
        GraphicsContext gc = getGraphicsContext2D();
        gc.setFill(Color.BEIGE);
        gc.fillRect(0, 0, getWidth(), getHeight());

        gc.setStroke(Color.BLACK);
        gc.setLineWidth(1);

        // Draw grid
        for (int i = 0; i < SIZE; i++) {
            gc.strokeLine(CELL_SIZE / 2.0, CELL_SIZE / 2.0 + i * CELL_SIZE,
                    CELL_SIZE / 2.0 + (SIZE - 1) * CELL_SIZE, CELL_SIZE / 2.0 + i * CELL_SIZE);
            gc.strokeLine(CELL_SIZE / 2.0 + i * CELL_SIZE, CELL_SIZE / 2.0,
                    CELL_SIZE / 2.0 + i * CELL_SIZE, CELL_SIZE / 2.0 + (SIZE - 1) * CELL_SIZE);
        }

        // Draw stones
        for (int r = 0; r < SIZE; r++) {
            for (int c = 0; c < SIZE; c++) {
                if (board[r][c] != null) {
                    gc.setFill(board[r][c].equals("B") ? Color.BLACK : Color.WHITE);
                    gc.fillOval(c * CELL_SIZE + 5, r * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
                    gc.setStroke(Color.BLACK);
                    gc.strokeOval(c * CELL_SIZE + 5, r * CELL_SIZE + 5, CELL_SIZE - 10, CELL_SIZE - 10);
                }
            }
        }
    }
}

