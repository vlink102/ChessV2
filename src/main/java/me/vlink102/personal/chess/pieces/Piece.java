package me.vlink102.personal.chess.pieces;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.classroom.ClassroomGUI;
import me.vlink102.personal.chess.internal.BoardCoordinate;
import me.vlink102.personal.chess.internal.Move;
import me.vlink102.personal.chess.pieces.generic.*;
import org.json.JSONObject;

import java.awt.*;

public abstract class Piece {
    private Image icon;
    private final String abbr;
    private final boolean white;
    private int moves;

    public Piece(BoardGUI board, String abbr, boolean white) {
        this.abbr = abbr;
        this.white = white;
        this.moves = 0;
        this.icon = board == null ? null : board.getOnlineAssets().getSavedPiece(board, this);
    }

    public Piece(ClassroomGUI board, String abbr, boolean white) {
        this.abbr = abbr;
        this.white = white;
        this.moves = 0;
        this.icon = board.getClassroomAssets().getSavedPiece(board, this);
    }

    public abstract boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture);

    public abstract int points();

    public boolean isDiagonal(BoardCoordinate from, BoardCoordinate to) {
        return Math.abs(from.col() - to.col()) == Math.abs(from.row() - to.row());
    }
    public boolean isStraight(BoardCoordinate from, BoardCoordinate to) {
        return (from.col() == to.col() && from.row() != to.row()) || (from.col() != to.col() && from.row() == to.row());
    }

    public String getAbbr() {
        return abbr;
    }
    public Image getIcon() {
        return icon;
    }
    public boolean isWhite() {
        return white;
    }

    public void incrementMoves() {
        this.moves ++;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }

    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        o.put("abbr", abbr);
        o.put("white", white);
        o.put("move-count", moves);
        if (this instanceof Rook) {
            JSONObject rookData = new JSONObject();
            rookData.put("castle-type", ((Rook) this).getType().toString());
            rookData.put("initial-square", ((Rook) this).getInitialSquare().toString());
            o.put("rook-data", rookData);
        }
        return o.toString();
    }

    public int getMoves() {
        return moves;
    }

    public static Piece parse(JSONObject json, BoardGUI boardGUI, BoardCoordinate initialRookSquare) {
        return switch (json.getString("abbr")) {
            case "R" -> new Rook(boardGUI, json.getBoolean("white"), initialRookSquare);
            case "N" -> new Knight(boardGUI, json.getBoolean("white"));
            case "B" -> new Bishop(boardGUI, json.getBoolean("white"));
            case "Q" -> new Queen(boardGUI, json.getBoolean("white"));
            case "K" -> new King(boardGUI, json.getBoolean("white"));
            case "P" -> new Pawn(boardGUI, json.getBoolean("white"));
            default -> null;
        };
    }

}
