package me.vlink102.personal.chess;

import java.awt.*;

public abstract class Piece {
    private Image icon;
    private final String abbr;
    private boolean white;
    private int moves;

    public Piece(BoardGUI board, String abbr, boolean white) {
        this.abbr = abbr;
        this.white = white;
        this.moves = 0;
        this.icon = OnlineAssets.getPiece(board, this);
    }

    public abstract boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture);

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
        return "Piece{abbr='" + abbr + "', white=" + white + "}";
    }

    public int getMoves() {
        return moves;
    }

}
