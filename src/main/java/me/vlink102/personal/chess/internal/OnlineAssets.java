package me.vlink102.personal.chess.internal;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.pieces.Piece;
import me.vlink102.personal.chess.pieces.SpecialPiece;

import java.awt.*;
import java.util.HashMap;

public class OnlineAssets {
    private static Image savedBoard;
    private static final HashMap<String, Image> savedPieces = new HashMap<>();

    public OnlineAssets(BoardGUI boardGUI) {
        loadCachedPieces(boardGUI);
        updateSavedImage(boardGUI);
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

    public static void loadCachedPieces(BoardGUI boardGUI) {
        loadPieces(boardGUI);
        loadSpecial(boardGUI);
    }

    private static void loadPieces(BoardGUI boardGUI) {
        OnlineAssets.savedPieces.put("wp", getPiece(boardGUI, "wp"));
        OnlineAssets.savedPieces.put("wr", getPiece(boardGUI, "wr"));
        OnlineAssets.savedPieces.put("wn", getPiece(boardGUI, "wn"));
        OnlineAssets.savedPieces.put("wb", getPiece(boardGUI, "wb"));
        OnlineAssets.savedPieces.put("wq", getPiece(boardGUI, "wq"));
        OnlineAssets.savedPieces.put("wk", getPiece(boardGUI, "wk"));
        OnlineAssets.savedPieces.put("bp", getPiece(boardGUI, "bp"));
        OnlineAssets.savedPieces.put("br", getPiece(boardGUI, "br"));
        OnlineAssets.savedPieces.put("bn", getPiece(boardGUI, "bn"));
        OnlineAssets.savedPieces.put("bb", getPiece(boardGUI, "bb"));
        OnlineAssets.savedPieces.put("bq", getPiece(boardGUI, "bq"));
        OnlineAssets.savedPieces.put("bk", getPiece(boardGUI, "bk"));
    }

    private static void loadSpecial(BoardGUI boardGUI) {
        OnlineAssets.savedPieces.put("ba", getPiece(boardGUI, "ba"));
        OnlineAssets.savedPieces.put("bc", getPiece(boardGUI, "bc"));
        OnlineAssets.savedPieces.put("be", getPiece(boardGUI, "be"));
        OnlineAssets.savedPieces.put("bem", getPiece(boardGUI, "bem"));
        OnlineAssets.savedPieces.put("bm", getPiece(boardGUI, "bm"));
        OnlineAssets.savedPieces.put("bmi", getPiece(boardGUI, "bmi"));
        OnlineAssets.savedPieces.put("bpr", getPiece(boardGUI, "bpr"));
        OnlineAssets.savedPieces.put("bdh", getPiece(boardGUI, "bdh"));
        OnlineAssets.savedPieces.put("bdk", getPiece(boardGUI, "bdk"));
        OnlineAssets.savedPieces.put("wa", getPiece(boardGUI, "wa"));
        OnlineAssets.savedPieces.put("wc", getPiece(boardGUI, "wc"));
        OnlineAssets.savedPieces.put("we", getPiece(boardGUI, "we"));
        OnlineAssets.savedPieces.put("wem", getPiece(boardGUI, "wem"));
        OnlineAssets.savedPieces.put("wm", getPiece(boardGUI, "wm"));
        OnlineAssets.savedPieces.put("wmi", getPiece(boardGUI, "wmi"));
        OnlineAssets.savedPieces.put("wpr", getPiece(boardGUI, "wpr"));
        OnlineAssets.savedPieces.put("wdh", getPiece(boardGUI, "wdh"));
        OnlineAssets.savedPieces.put("wdk", getPiece(boardGUI, "wdk"));
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
