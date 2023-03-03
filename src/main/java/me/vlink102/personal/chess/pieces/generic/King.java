package me.vlink102.personal.chess.pieces.generic;

import me.vlink102.personal.chess.classroom.ClassroomGUI;
import me.vlink102.personal.chess.internal.BoardCoordinate;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.pieces.Piece;

public class King extends Piece {
    public King(BoardGUI board, boolean white) {
        super(board, "K", white);
    }
    public King(ClassroomGUI board, boolean white) {
        super(board, "K", white);
    }

    @Override
    public boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture) {
        int x0 = from.col();
        int y0 = from.row();
        int x1 = to.col();
        int y1 = to.row();

        return Math.abs(x0 - x1) <= 1 && Math.abs(y0 - y1) <= 1;
    }

    @Override
    public int points() {
        return 10;
    }
}
