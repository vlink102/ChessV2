package me.vlink102.personal.chess.pieces.generic;

import me.vlink102.personal.chess.classroom.ClassroomGUI;
import me.vlink102.personal.chess.internal.BoardCoordinate;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.pieces.Piece;

public class Knight extends Piece {
    public Knight(BoardGUI board, boolean white) {
        super(board, "N", white);
    }

    public Knight(ClassroomGUI board, boolean white) {
        super(board, "N", white);
    }

    @Override
    public boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture) {
        int x0 = from.col();
        int y0 = from.row();
        int x1 = to.col();
        int y1 = to.row();

        return (Math.abs(x1 - x0) == 2 && Math.abs(y1 - y0) == 1) || (Math.abs(x1 - x0) == 1 && Math.abs(y1 - y0) == 2);
    }

    @Override
    public int points() {
        return 3;
    }
}
