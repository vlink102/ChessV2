package me.vlink102.personal.chess.pieces.special.asian;

import me.vlink102.personal.chess.BoardCoordinate;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.SpecialPiece;

public class DragonHorse extends SpecialPiece {
    public DragonHorse(BoardGUI board, boolean white) {
        super(board, "DH", white);
    }

    @Override
    public boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture) {
        int x0 = from.col();
        int y0 = from.row();
        int x1 = to.col();
        int y1 = to.row();

        return (Math.abs(x0 - x1) <= 1 && Math.abs(y0 - y1) <= 1) || isDiagonal(from, to);
    }

    @Override
    public int points() {
        return 5;
    }

    @Override
    public char fenChar() {
        return 'U';
    }
}
