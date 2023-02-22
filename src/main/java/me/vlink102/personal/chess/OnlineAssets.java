package me.vlink102.personal.chess;

import me.vlink102.personal.chess.pieces.RiceFarmer;

import javax.imageio.ImageIO;
import java.awt.*;
import java.io.IOException;
import java.net.URL;

public class OnlineAssets {
    private static Image savedBoard;
    public static boolean hasResized;

    public OnlineAssets(BoardGUI boardGUI) {
        savedBoard = getBoard(boardGUI);
        hasResized = false;
    }

    public static Image getSavedBoard() {
        return savedBoard;
    }

    public static Image getPiece(BoardGUI board, Piece piece) {
        if (piece instanceof RiceFarmer) {
            try {
                if (piece.isWhite()) {
                    return ImageIO.read(new URL("https://images-ext-2.discordapp.net/external/Ec-Cw9gKcrFek24optDG_AJTOpLhTevKK5C-hL_liGw/https/i.imgur.com/ELBw34J.png")).getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                } else {
                    return ImageIO.read(new URL("https://images-ext-1.discordapp.net/external/QnljI7DNbjE_B3fj53L-kXiXpqHDs-gfqDhOcUP9Xpo/https/i.imgur.com/KIR834t.png")).getScaledInstance(64, 64, Image.SCALE_SMOOTH);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            try {
                return ImageIO.read(new URL("https://www.chess.com/chess-themes/pieces/" + board.getPieceTheme().getLinkString() + "/" + board.getPieceSize() + "/" + (piece.isWhite() ? "w" : "b") + piece.getAbbr().toLowerCase() + ".png"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
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
