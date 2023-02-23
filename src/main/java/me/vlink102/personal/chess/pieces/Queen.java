package me.vlink102.personal.chess.pieces;

import me.vlink102.personal.chess.BoardCoordinate;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Piece;

public class Queen extends Piece {
    public Queen(BoardGUI board, boolean white) {
        super(board, "Q", white);
    }

    @Override
    public boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture) {
        return isDiagonal(from, to) || isStraight(from, to);
    }

    @Override
    public int points() {
        return 9;
    }
}
