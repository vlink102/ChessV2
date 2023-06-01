package me.vlink102.personal.chess.internal;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.pieces.Piece;
import me.vlink102.personal.chess.ui.sidepanel.CaptureGUI;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CapturedPieces {
    private int pawn;
    private int knight;
    private int bishop;
    private int queen;
    private int rook;

    private int pawn2;
    private int knight2;
    private int bishop2;
    private int queen2;
    private int rook2;

    public CapturedPieces() {
        this.pawn = 0;
        this.knight = 0;
        this.bishop = 0;
        this.queen = 0;
        this.rook = 0;
        this.pawn2 = 0;
        this.knight2 = 0;
        this.bishop2 = 0;
        this.queen2 = 0;
        this.rook2 = 0;
    }

    public void add(Piece piece) {
        if (piece.isWhite()) {
            switch (piece.getAbbr().toLowerCase()) {
                case "p" -> pawn = Math.min(8, pawn + 1);
                case "n" -> knight = Math.min(2, knight + 1);
                case "b" -> bishop = Math.min(2, bishop + 1);
                case "q" -> queen = Math.min(1, queen + 1);
                case "r" -> rook = Math.min(2, rook + 1);
            }
        } else {
            switch (piece.getAbbr().toLowerCase()) {
                case "p" -> pawn2 = Math.min(8, pawn2 + 1);
                case "n" -> knight2 = Math.min(2, knight2 + 1);
                case "b" -> bishop2 = Math.min(2, bishop2 + 1);
                case "q" -> queen2 = Math.min(1, queen2 + 1);
                case "r" -> rook2 = Math.min(2, rook2 + 1);
            }
        }
    }

    public HashMap<Boolean, List<Image>> getDisplays(BoardGUI boardGUI) {
        HashMap<Boolean, List<Image>> res = new HashMap<>();
        String prefix = "W_";
        yeehaw(true, boardGUI, res, prefix, pawn, knight, bishop, queen, rook);
        prefix = "B_";
        yeehaw(false, boardGUI, res, prefix, pawn2, knight2, bishop2, queen2, rook2);
        return res;
    }

    private void yeehaw(boolean white, BoardGUI boardGUI, HashMap<Boolean, List<Image>> res, String prefix, int pawn2, int knight2, int bishop2, int queen2, int rook2) {
        List<Image> bruhBruh = new ArrayList<>();
        if (pawn2 > 0) bruhBruh.add(boardGUI.getOnlineAssets().getSavedCapturePiece(CaptureGUI.CaptureDisplay.valueOf(prefix + "PAWN_" + pawn2)));
        if (knight2 > 0) bruhBruh.add(boardGUI.getOnlineAssets().getSavedCapturePiece(CaptureGUI.CaptureDisplay.valueOf(prefix + "KNIGHT_" + knight2)));
        if (bishop2 > 0) bruhBruh.add(boardGUI.getOnlineAssets().getSavedCapturePiece(CaptureGUI.CaptureDisplay.valueOf(prefix + "BISHOP_" + bishop2)));
        if (queen2 > 0) bruhBruh.add(boardGUI.getOnlineAssets().getSavedCapturePiece(CaptureGUI.CaptureDisplay.valueOf(prefix + "QUEEN_" + queen2)));
        if (rook2 > 0) bruhBruh.add(boardGUI.getOnlineAssets().getSavedCapturePiece(CaptureGUI.CaptureDisplay.valueOf(prefix + "ROOK_" + rook2)));
        res.put(white, bruhBruh);
    }

    public int getPawn() {
        return pawn;
    }

    public int getPawn2() {
        return pawn2;
    }

    public int getKnight() {
        return knight;
    }

    public int getKnight2() {
        return knight2;
    }

    public int getQueen() {
        return queen;
    }

    public int getQueen2() {
        return queen2;
    }

    public int getBishop() {
        return bishop;
    }

    public int getBishop2() {
        return bishop2;
    }

    public int getRook() {
        return rook;
    }

    public int getRook2() {
        return rook2;
    }

    public CapturedPieces(int pawn, int pawn2, int knight, int knight2, int queen, int queen2, int bishop, int bishop2, int rook, int rook2) {
        this.pawn = pawn;
        this.pawn2 = pawn2;
        this.knight = knight;
        this.knight2 = knight2;
        this.queen = queen;
        this.queen2 = queen2;
        this.bishop = bishop;
        this.bishop2 = bishop2;
        this.rook = rook;
        this.rook2 = rook2;
    }

    public CapturedPieces clonePieces() {
        return new CapturedPieces(pawn, pawn2, knight, knight2, queen, queen2, bishop, bishop2, rook, rook2);
    }
}
