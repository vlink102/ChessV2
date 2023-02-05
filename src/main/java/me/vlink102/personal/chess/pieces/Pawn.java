package me.vlink102.personal.chess.pieces;

import me.vlink102.personal.chess.BoardCoordinate;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Piece;

public class Pawn extends Piece {
    private final BoardGUI boardGUI;

    public Pawn(BoardGUI board, boolean white) {
        super(board, "P", white);
        this.boardGUI = board;
    }

    @Override
    public boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture) {
        int x0 = from.getCol();
        int y0 = from.getRow();
        int x1 = to.getCol();
        int y1 = to.getRow();

        return (isWhite() ? y1 > y0 : y1 < y0) &&
                (boardGUI.getHistory().isEmpty() ? Math.abs(y1 - y0) == 2 || Math.abs(y1 - y0) == 1 : Math.abs(y1 - y0) == 1) &&
                (capture ? (Math.abs(y1 - y0) == 1 && Math.abs(x1 - x0) == 1) : x1 == x0);
    }
}
