package me.vlink102.personal.chess.pieces.special.asian;

import me.vlink102.personal.chess.classroom.ClassroomGUI;
import me.vlink102.personal.chess.internal.BoardCoordinate;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.pieces.SpecialPiece;

public class DragonKing extends SpecialPiece {
    public DragonKing(BoardGUI board, boolean white) {
        super(board, "DK", white);
    }
    public DragonKing(ClassroomGUI board, boolean white) {
        super(board, "DK", white);
    }

    @Override
    public boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture) {
        int x0 = from.col();
        int y0 = from.row();
        int x1 = to.col();
        int y1 = to.row();

        return (Math.abs(x0 - x1) <= 1 && Math.abs(y0 - y1) <= 1) || isStraight(from, to);
    }

    @Override
    public char fenChar() {
        return 'Y';
    }
}
