package me.vlink102.personal.chess.pieces.generic;

import me.vlink102.personal.chess.classroom.ClassroomGUI;
import me.vlink102.personal.chess.internal.BoardCoordinate;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.pieces.Piece;

public class Bishop extends Piece {
    public Bishop(BoardGUI board, boolean white) {
        super(board, "B", white);
    }

    public Bishop(ClassroomGUI board, boolean white) {
        super(board, "B", white);
    }

    @Override
    public boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture) {
        return isDiagonal(from, to);
    }

    @Override
    public int points() {
        return 3;
    }
}
