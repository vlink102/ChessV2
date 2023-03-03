package me.vlink102.personal.chess.internal;

import me.vlink102.personal.chess.classroom.ClassroomGUI;
import me.vlink102.personal.chess.pieces.Piece;

import java.awt.*;
import java.util.HashMap;

public class ClassroomAssets {
    private static Image savedBoard;
    private static final HashMap<String, Image> savedPieces = new HashMap<>();

    public ClassroomAssets(ClassroomGUI classroomGUI) {
        loadCachedPieces(classroomGUI);
        updateSavedImage(classroomGUI);
    }

    public static Image getSavedBoard() {
        return savedBoard;
    }

    public static Image getSavedPiece(ClassroomGUI classroomGUI, Piece piece) {
        return savedPieces.get((piece.isWhite() ? "w" : "b") + piece.getAbbr().toLowerCase()).getScaledInstance(classroomGUI.getPieceSize(), classroomGUI.getPieceSize(), Image.SCALE_SMOOTH);
    }

    public static Image getPiece(ClassroomGUI classroomGUI, String abbr) {
        if (abbr.matches("[wb][rnbqkp]")) {
            return Move.getResource("/themes/pieces/" + classroomGUI.getPieceTheme().getLinkString() + "/" + abbr + ".png");
        } else {
            return Move.getResource("/special-pieces/" + abbr + ".png");
        }
    }

    public static Image getBoard(ClassroomGUI classroomGUI) {
        return Move.getResource("/themes/boards/" + classroomGUI.getBoardTheme().getLinkString() + ".png");
    }

    public static synchronized void updateSavedImage(ClassroomGUI classroomGUI) {
        savedBoard = getBoard(classroomGUI).getScaledInstance(classroomGUI.getPieceSize() * classroomGUI.getBoardSize(), classroomGUI.getPieceSize() * 8, Image.SCALE_FAST);
    }

    public static void loadCachedPieces(ClassroomGUI classroomGUI) {
        loadPieces(classroomGUI);
        loadSpecial(classroomGUI);
    }

    private static void loadPieces(ClassroomGUI classroomGUI) {
        ClassroomAssets.savedPieces.put("wp", getPiece(classroomGUI, "wp"));
        ClassroomAssets.savedPieces.put("wr", getPiece(classroomGUI, "wr"));
        ClassroomAssets.savedPieces.put("wn", getPiece(classroomGUI, "wn"));
        ClassroomAssets.savedPieces.put("wb", getPiece(classroomGUI, "wb"));
        ClassroomAssets.savedPieces.put("wq", getPiece(classroomGUI, "wq"));
        ClassroomAssets.savedPieces.put("wk", getPiece(classroomGUI, "wk"));
        ClassroomAssets.savedPieces.put("bp", getPiece(classroomGUI, "bp"));
        ClassroomAssets.savedPieces.put("br", getPiece(classroomGUI, "br"));
        ClassroomAssets.savedPieces.put("bn", getPiece(classroomGUI, "bn"));
        ClassroomAssets.savedPieces.put("bb", getPiece(classroomGUI, "bb"));
        ClassroomAssets.savedPieces.put("bq", getPiece(classroomGUI, "bq"));
        ClassroomAssets.savedPieces.put("bk", getPiece(classroomGUI, "bk"));
    }

    private static void loadSpecial(ClassroomGUI classroomGUI) {
        ClassroomAssets.savedPieces.put("ba", getPiece(classroomGUI, "ba"));
        ClassroomAssets.savedPieces.put("bc", getPiece(classroomGUI, "bc"));
        ClassroomAssets.savedPieces.put("be", getPiece(classroomGUI, "be"));
        ClassroomAssets.savedPieces.put("bem", getPiece(classroomGUI, "bem"));
        ClassroomAssets.savedPieces.put("bm", getPiece(classroomGUI, "bm"));
        ClassroomAssets.savedPieces.put("bmi", getPiece(classroomGUI, "bmi"));
        ClassroomAssets.savedPieces.put("bpr", getPiece(classroomGUI, "bpr"));
        ClassroomAssets.savedPieces.put("bdh", getPiece(classroomGUI, "bdh"));
        ClassroomAssets.savedPieces.put("bdk", getPiece(classroomGUI, "bdk"));
        ClassroomAssets.savedPieces.put("wa", getPiece(classroomGUI, "wa"));
        ClassroomAssets.savedPieces.put("wc", getPiece(classroomGUI, "wc"));
        ClassroomAssets.savedPieces.put("we", getPiece(classroomGUI, "we"));
        ClassroomAssets.savedPieces.put("wem", getPiece(classroomGUI, "wem"));
        ClassroomAssets.savedPieces.put("wm", getPiece(classroomGUI, "wm"));
        ClassroomAssets.savedPieces.put("wmi", getPiece(classroomGUI, "wmi"));
        ClassroomAssets.savedPieces.put("wpr", getPiece(classroomGUI, "wpr"));
        ClassroomAssets.savedPieces.put("wdh", getPiece(classroomGUI, "wdh"));
        ClassroomAssets.savedPieces.put("wdk", getPiece(classroomGUI, "wdk"));
    }

    public static synchronized void updatePieceDesigns(ClassroomGUI classroomGUI) {
        loadCachedPieces(classroomGUI);
        for (Piece[] pieces : classroomGUI.getGamePieces()) {
            for (Piece piece : pieces) {
                if (piece != null) {
                    piece.setIcon(getSavedPiece(classroomGUI, piece));
                }
            }
        }
    }
}
