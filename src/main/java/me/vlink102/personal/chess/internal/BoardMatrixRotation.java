package me.vlink102.personal.chess.internal;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.pieces.Piece;

import java.util.Arrays;

public class BoardMatrixRotation {
    static Piece[][] rotatePieceMatrix(Piece[][] board, BoardGUI.BoardView view) {
        Piece[][] ret = Arrays.stream(board).map(Piece[]::clone).toArray(Piece[][]::new);

        switch (view) {
            case WHITE -> rotateMatrix180(ret);
        }
        return ret;
    }

    public static Move.MoveHighlights[][] rotateHighlightMatrix(Move.MoveHighlights[][] highlightTypes, BoardGUI.BoardView view) {
        Move.MoveHighlights[][] ret = Arrays.stream(highlightTypes).map(Move.MoveHighlights[]::clone).toArray(Move.MoveHighlights[][]::new);

        switch (view) {
            case WHITE -> rotateMatrix180(ret);
        }
        return ret;
    }

    public static <T> void transposeMatrix(T[][] board, int rows, int cols) {
        for (int i = 0; i < rows; i++) {
            for (int j = i; j < cols; j++) {
                T temp = board[j][i];
                board[j][i] = board[i][j];
                board[i][j] = temp;
            }
        }
    }

    public static <T> void rotateMatrix180(T[][] board) {
        int rows = board.length;
        int cols = board[0].length;
        for (int i = 0; i <= (rows / 2) - 1; i++) {
            for (int j = 0; j < cols; j++) {
                T temp = board[i][j];
                board[i][j] = board[rows - i - 1][cols - j - 1];
                board[rows - i - 1][cols - j - 1] = temp;
            }
        }
    }

    public static <T> void rotateMatrix90CW(T[][] board) {
        int rows = board.length;
        int cols = board[0].length;
        transposeMatrix(board, rows, cols);
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < rows / 2; j++) {
                T temp = board[i][j];
                board[i][j] = board[i][rows - j - 1];
                board[i][rows - j - 1] = temp;
            }
        }
    }

    public static <T> void rotateMatrix90AC(T[][] board) {
        int rows = board.length;
        int cols = board[0].length;
        transposeMatrix(board, rows, cols);
        for (int i = 0; i < cols; i++) {
            for (int j = 0, k = cols - 1; j < k; j++, k--) {
                T temp = board[j][i];
                board[j][i] = board[k][i];
                board[k][i] = temp;
            }
        }
    }

    public static void printBoard(Piece[][] board, BoardGUI.BoardView view, int boardSize) {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                System.out.print((row + col) % 2 == 0 ? "\033[47m" : "\033[40m");
                switch (view) {
                    case BLACK -> {
                        if (board[row][(boardSize - 1) - col] != null) {
                            System.out.print(" " + (board[row][(boardSize - 1) - col].isWhite() ? "\033[1;95m" : "\033[1;96m") + board[row][(boardSize - 1) - col].getAbbr() + " ");
                        } else {
                            System.out.print("   ");
                        }
                    }
                    case WHITE -> {
                        if (board[(boardSize - 1) - row][col] != null) {
                            System.out.print(" " + (board[(boardSize - 1) - row][col].isWhite() ? "\033[1;95m" : "\033[1;96m") + board[(boardSize - 1) - row][col].getAbbr() + " ");
                        } else {
                            System.out.print("   ");
                        }
                    }
                }

            }
            System.out.print("\u001b[0m\n");
        }
    }
}