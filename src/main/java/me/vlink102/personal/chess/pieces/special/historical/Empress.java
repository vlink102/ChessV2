package me.vlink102.personal.chess.pieces.special.historical;

import me.vlink102.personal.chess.BoardCoordinate;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.SpecialPiece;

public class Empress extends SpecialPiece {
    public Empress(BoardGUI board, boolean white) {
        super(board, "EM", white);
    }

    @Override
    public boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture) {
        int x0 = from.col();
        int y0 = from.row();
        int x1 = to.col();
        int y1 = to.row();

        return ((Math.abs(x1 - x0) == 2 && Math.abs(y1 - y0) == 1) || (Math.abs(x1 - x0) == 1 && Math.abs(y1 - y0) == 2)) || isStraight(from, to);
    }

    @Override
    public int points() {
        return 8;
    }

    @Override
    public char fenChar() {
        return 'H';
    }
}
