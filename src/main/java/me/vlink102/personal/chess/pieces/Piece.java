package me.vlink102.personal.chess.pieces;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.classroom.ClassroomGUI;
import me.vlink102.personal.chess.internal.BoardCoordinate;
import me.vlink102.personal.chess.internal.ClassroomAssets;
import me.vlink102.personal.chess.internal.OnlineAssets;

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
        this.icon = OnlineAssets.getSavedPiece(board, this);
    }

    public Piece(ClassroomGUI board, String abbr, boolean white) {
        this.abbr = abbr;
        this.white = white;
        this.moves = 0;
        this.icon = ClassroomAssets.getSavedPiece(board, this); // TODO
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
        return "Piece{abbr='" + abbr + "', white=" + white + "}";
    }

    public int getMoves() {
        return moves;
    }

    public void swapSides() {
        this.white = !this.white;
    }

}
