package me.vlink102.personal.chess.pieces.generic;

import me.vlink102.personal.chess.classroom.ClassroomGUI;
import me.vlink102.personal.chess.internal.BoardCoordinate;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.pieces.Piece;

public class Queen extends Piece {
    public Queen(BoardGUI board, boolean white) {
        super(board, "Q", white);
    }
    public Queen(ClassroomGUI board, boolean white) {
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
