package me.vlink102.personal.chess;

import me.vlink102.personal.chess.pieces.Pawn;
import me.vlink102.personal.chess.pieces.RiceFarmer;

import javax.imageio.ImageIO;
import javax.imageio.ImageTypeSpecifier;
import javax.swing.*;
import javax.tools.Tool;
import java.awt.*;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashMap;
import java.util.Objects;

public class OnlineAssets {
    private static int previousPieceSize;
    private static Image savedBoard;
    private static HashMap<String, Image> savedPieces;
    public static boolean hasResized;

    public OnlineAssets(BoardGUI boardGUI) {
        loadCachedPieces(boardGUI);
        updateSavedImage(boardGUI);
        hasResized = false;
    }

    public static Image getSavedBoard() {
        return savedBoard;
    }

    public static Image getSavedPiece(BoardGUI boardGUI, Piece piece) {
        return savedPieces.get((piece.isWhite() ? "w" : "b") + piece.getAbbr().toLowerCase()).getScaledInstance(boardGUI.getPieceSize(), boardGUI.getPieceSize(), Image.SCALE_SMOOTH);
    }

    public static Image getPiece(BoardGUI boardGUI, String abbr) {
        if (abbr.matches("[wb][rnbqkp]")) {
            return Move.getResource("/themes/pieces/" + boardGUI.getPieceTheme().getLinkString() + "/" + abbr + ".png");
        } else {
            return Move.getResource("/special-pieces/" + abbr + ".png");
        }
    }

    public static Image getPiece(BoardGUI boardGUI, Piece piece) {
        if (piece instanceof SpecialPiece) {
            return Move.getResource("/special-pieces/" + (piece.isWhite() ? "w" : "b") + piece.getAbbr().toLowerCase() + ".png");
        } else {
            return Move.getResource("/themes/pieces/" + boardGUI.getPieceTheme().getLinkString() + "/" + (piece.isWhite() ? "w" : "b") + piece.getAbbr().toLowerCase() + ".png");
        }
    }

    /*
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
     */

    public static Image getBoard(BoardGUI boardGUI) {
        return Move.getResource("/themes/boards/" + boardGUI.getBoardTheme().getLinkString() + ".png");
    }

    public static synchronized void updateSavedImage(BoardGUI boardGUI) {
        savedBoard = getBoard(boardGUI).getScaledInstance(boardGUI.getPieceSize() * boardGUI.getBoardSize(), boardGUI.getPieceSize() * 8, Image.SCALE_FAST);
    }

    /**
     * scale image
     *
     * @param sbi image to scale
     * @param imageType type of image
     * @param dWidth width of destination image
     * @param dHeight height of destination image
     * @param fWidth x-factor for transformation / scaling
     * @param fHeight y-factor for transformation / scaling
     * @return scaled image
     */
    public static BufferedImage scale(BufferedImage sbi, int imageType, int dWidth, int dHeight, double fWidth, double fHeight) {
        BufferedImage dbi = null;
        if(sbi != null) {
            dbi = new BufferedImage(dWidth, dHeight, imageType);
            Graphics2D g = dbi.createGraphics();
            AffineTransform at = AffineTransform.getScaleInstance(fWidth, fHeight);
            g.drawRenderedImage(sbi, at);
        }
        return dbi;
    }

    public static void loadCachedPieces(BoardGUI boardGUI) {
        savedPieces = new HashMap<>();
        savedPieces.put("wp", getPiece(boardGUI, "wp"));
        savedPieces.put("wr", getPiece(boardGUI, "wr"));
        savedPieces.put("wn", getPiece(boardGUI, "wn"));
        savedPieces.put("wb", getPiece(boardGUI, "wb"));
        savedPieces.put("wq", getPiece(boardGUI, "wq"));
        savedPieces.put("wk", getPiece(boardGUI, "wk"));
        savedPieces.put("bp", getPiece(boardGUI, "bp"));
        savedPieces.put("br", getPiece(boardGUI, "br"));
        savedPieces.put("bn", getPiece(boardGUI, "bn"));
        savedPieces.put("bb", getPiece(boardGUI, "bb"));
        savedPieces.put("bq", getPiece(boardGUI, "bq"));
        savedPieces.put("bk", getPiece(boardGUI, "bk"));
        loadSpecialPieces(boardGUI);
    }

    public static void loadSpecialPieces(BoardGUI boardGUI) {
        savedPieces.put("ba", getPiece(boardGUI, "ba"));
        savedPieces.put("bc", getPiece(boardGUI, "bc"));
        savedPieces.put("be", getPiece(boardGUI, "be"));
        savedPieces.put("bem", getPiece(boardGUI, "bem"));
        savedPieces.put("bm", getPiece(boardGUI, "bm"));
        savedPieces.put("bmi", getPiece(boardGUI, "bmi"));
        savedPieces.put("bpr", getPiece(boardGUI, "bpr"));
        savedPieces.put("bdh", getPiece(boardGUI, "bdh"));
        savedPieces.put("bdk", getPiece(boardGUI, "bdk"));
        savedPieces.put("wa", getPiece(boardGUI, "wa"));
        savedPieces.put("wc", getPiece(boardGUI, "wc"));
        savedPieces.put("we", getPiece(boardGUI, "we"));
        savedPieces.put("wem", getPiece(boardGUI, "wem"));
        savedPieces.put("wm", getPiece(boardGUI, "wm"));
        savedPieces.put("wmi", getPiece(boardGUI, "wmi"));
        savedPieces.put("wpr", getPiece(boardGUI, "wpr"));
        savedPieces.put("wdh", getPiece(boardGUI, "wdh"));
        savedPieces.put("wdk", getPiece(boardGUI, "wdk"));
    }

    public static synchronized void updatePieceDesigns(BoardGUI boardGUI) {
        loadCachedPieces(boardGUI);
        for (Piece[] pieces : boardGUI.getGamePieces()) {
            for (Piece piece : pieces) {
                if (piece != null) {
                    piece.setIcon(getSavedPiece(boardGUI, piece));
                }
            }
        }
    }
}
