package me.vlink102.personal.chess;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class OnlineAssets {
    private final BoardGUI boardGUI;
    private static Image savedBoard;
    public static boolean hasResized;

    public OnlineAssets(BoardGUI boardGUI) {
        this.boardGUI = boardGUI;
        savedBoard = getBoard(boardGUI);
        hasResized = false;
    }

    public static Image getSavedBoard() {
        return savedBoard;
    }

    public static Image getPiece(BoardGUI board, Piece piece) {
        try {
            return ImageIO.read(new URL("https://www.chess.com/chess-themes/pieces/" + board.getPieceTheme().getLinkString() + "/300/" + (piece.isWhite() ? "w" : "b") + piece.getAbbr().toLowerCase() + ".png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Image getBoard(BoardGUI board) {
        try {
            Image newBoard = ImageIO.read(new URL("https://www.chess.com/boards/" + board.getBoardTheme().getLinkString() + "/300.png"));
            savedBoard = newBoard;
            return newBoard;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Image getPiece(BoardGUI board, Piece piece, int dimensions) {
        return getPiece(board, piece).getScaledInstance(dimensions, dimensions, Image.SCALE_SMOOTH);
    }

    public void updateSavedImage(BoardGUI boardGUI) {
        savedBoard = getBoard(boardGUI);
    }

    public void updatePieceDesigns(BoardGUI boardGUI) {
        for (Piece[] pieces : boardGUI.getGamePieces()) {
            for (Piece piece : pieces) {
                if (piece != null) {
                    piece.setIcon(getPiece(boardGUI, piece));
                }
            }
        }
    }
}
