package me.vlink102.personal.chess.pieces.special.historical;

import me.vlink102.personal.chess.classroom.ClassroomGUI;
import me.vlink102.personal.chess.internal.BoardCoordinate;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.pieces.SpecialPiece;

public class Camel extends SpecialPiece {
    public Camel(BoardGUI board, boolean white) {
        super(board, "C", white);
    }
    public Camel(ClassroomGUI board, boolean white) {
        super(board, "C", white);
    }

    @Override
    public boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture) {
        int x0 = from.col();
        int y0 = from.row();
        int x1 = to.col();
        int y1 = to.row();

        return (Math.abs(x1 - x0) == 3 && Math.abs(y1 - y0) == 1) || (Math.abs(x1 - x0) == 1 && Math.abs(y1 - y0) == 3);
    }

    @Override
    public int points() {
        return 3;
    }

    @Override
    public char fenChar() {
        return 'C';
    }
}
