package me.vlink102.personal.chess.internal;

import me.vlink102.personal.chess.classroom.ClassroomGUI;
import me.vlink102.personal.chess.pieces.Piece;

import java.awt.*;
import java.util.HashMap;

public class ClassroomAssets {
    private Image savedBoard;
    private final HashMap<String, Image> savedPieces = new HashMap<>();

    public ClassroomAssets(ClassroomGUI classroomGUI) {
        loadCachedPieces(classroomGUI);
        updateSavedImage(classroomGUI);
    }

    public Image getSavedBoard() {
        return savedBoard;
    }

    public Image getSavedPiece(ClassroomGUI classroomGUI, Piece piece) {
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

    public synchronized void updateSavedImage(ClassroomGUI classroomGUI) {
        savedBoard = getBoard(classroomGUI).getScaledInstance(classroomGUI.getPieceSize() * classroomGUI.getBoardSize(), classroomGUI.getPieceSize() * 8, Image.SCALE_FAST);
    }

    public void loadCachedPieces(ClassroomGUI classroomGUI) {
        loadPieces(classroomGUI);
        loadSpecial(classroomGUI);
    }

    private void loadPieces(ClassroomGUI classroomGUI) {
        loadDefaultPiecesToMap(savedPieces, getPiece(classroomGUI, "wp"), getPiece(classroomGUI, "wr"), getPiece(classroomGUI, "wn"), getPiece(classroomGUI, "wb"), getPiece(classroomGUI, "wq"), getPiece(classroomGUI, "wk"), getPiece(classroomGUI, "bp"), getPiece(classroomGUI, "br"), getPiece(classroomGUI, "bn"), getPiece(classroomGUI, "bb"), getPiece(classroomGUI, "bq"), getPiece(classroomGUI, "bk"));
    }

    static void loadDefaultPiecesToMap(HashMap<String, Image> savedPieces, Image wp, Image wr, Image wn, Image wb, Image wq, Image wk, Image bp, Image br, Image bn, Image bb, Image bq, Image bk) {
        savedPieces.put("wp", wp);
        savedPieces.put("wr", wr);
        savedPieces.put("wn", wn);
        savedPieces.put("wb", wb);
        savedPieces.put("wq", wq);
        savedPieces.put("wk", wk);
        savedPieces.put("bp", bp);
        savedPieces.put("br", br);
        savedPieces.put("bn", bn);
        savedPieces.put("bb", bb);
        savedPieces.put("bq", bq);
        savedPieces.put("bk", bk);
    }

    private void loadSpecial(ClassroomGUI classroomGUI) {
        loadSpecialPiecesToMap(savedPieces, getPiece(classroomGUI, "ba"), getPiece(classroomGUI, "bc"), getPiece(classroomGUI, "be"), getPiece(classroomGUI, "bem"), getPiece(classroomGUI, "bm"), getPiece(classroomGUI, "bmi"), getPiece(classroomGUI, "bpr"), getPiece(classroomGUI, "bdh"), getPiece(classroomGUI, "bdk"), getPiece(classroomGUI, "wa"), getPiece(classroomGUI, "wc"), getPiece(classroomGUI, "we"), getPiece(classroomGUI, "wem"), getPiece(classroomGUI, "wm"), getPiece(classroomGUI, "wmi"), getPiece(classroomGUI, "wpr"), getPiece(classroomGUI, "wdh"), getPiece(classroomGUI, "wdk"));
    }

    static void loadSpecialPiecesToMap(HashMap<String, Image> savedPieces, Image ba, Image bc, Image be, Image bem, Image bm, Image bmi, Image bpr, Image bdh, Image bdk, Image wa, Image wc, Image we, Image wem, Image wm, Image wmi, Image wpr, Image wdh, Image wdk) {
        savedPieces.put("ba", ba);
        savedPieces.put("bc", bc);
        savedPieces.put("be", be);
        savedPieces.put("bem", bem);
        savedPieces.put("bm", bm);
        savedPieces.put("bmi", bmi);
        savedPieces.put("bpr", bpr);
        savedPieces.put("bdh", bdh);
        savedPieces.put("bdk", bdk);
        savedPieces.put("wa", wa);
        savedPieces.put("wc", wc);
        savedPieces.put("we", we);
        savedPieces.put("wem", wem);
        savedPieces.put("wm", wm);
        savedPieces.put("wmi", wmi);
        savedPieces.put("wpr", wpr);
        savedPieces.put("wdh", wdh);
        savedPieces.put("wdk", wdk);
    }

    public synchronized void updatePieceDesigns(ClassroomGUI classroomGUI) {
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
