package me.vlink102.personal.chess.pieces.special.historical;

import me.vlink102.personal.chess.BoardCoordinate;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.SpecialPiece;

public class Elephant extends SpecialPiece {
    public Elephant(BoardGUI board, boolean white) {
        super(board, "E", white);
    }

    @Override
    public boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture) {
        int x0 = from.col();
        int y0 = from.row();
        int x1 = to.col();
        int y1 = to.row();

        return Math.abs(x0 - x1) == 2 && Math.abs(y0 - y1) == 2;
    }

    @Override
    public int points() {
        return 3;
    }

    @Override
    public char fenChar() {
        return 'E';
    }
}
