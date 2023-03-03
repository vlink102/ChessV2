package me.vlink102.personal.chess.pieces.generic.special.historical;

import me.vlink102.personal.chess.classroom.ClassroomGUI;
import me.vlink102.personal.chess.internal.BoardCoordinate;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.pieces.SpecialPiece;

public class Minister extends SpecialPiece {
    public Minister(BoardGUI board, boolean white) {
        super(board, "MI", white);
    }
    public Minister(ClassroomGUI board, boolean white) {
        super(board, "MI", white);
    }

    @Override
    public boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture) {
        int x0 = from.col();
        int y0 = from.row();
        int x1 = to.col();
        int y1 = to.row();

        return Math.abs(x0 - x1) == 1 && Math.abs(y0 - y1) == 1;
    }

    @Override
    public int points() {
        return 2;
    }

    @Override
    public char fenChar() {
        return 'I';
    }
}
