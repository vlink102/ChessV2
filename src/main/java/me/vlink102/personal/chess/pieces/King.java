package me.vlink102.personal.chess.pieces;

import me.vlink102.personal.chess.BoardCoordinate;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Piece;

public class King extends Piece {
    public King(BoardGUI board, boolean white) {
        super(board, "K", white);
    }

    @Override
    public boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture) {
        int x0 = from.getFile();
        int y0 = from.getRank();
        int x1 = to.getFile();
        int y1 = to.getRank();

        return Math.abs(x0 - x1) <= 1 && Math.abs(y0 - y1) <= 1;
    }
}
