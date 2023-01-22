package me.vlink102.personal.chess.pieces;

import me.vlink102.personal.chess.BoardCoordinate;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Piece;

public class Knight extends Piece {
    public Knight(BoardGUI board, boolean white) {
        super(board, "N", white);
    }

    @Override
    public boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture) {
        int x0 = from.getFile();
        int y0 = from.getRank();
        int x1 = to.getFile();
        int y1 = to.getRank();

        return (Math.abs(x1 - x0) == 2 && Math.abs(y1 - y0) == 1) || (Math.abs(x1 - x0) == 1 && Math.abs(y1 - y0) == 2);
    }
}
