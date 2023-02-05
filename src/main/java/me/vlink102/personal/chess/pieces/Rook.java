package me.vlink102.personal.chess.pieces;

import me.vlink102.personal.chess.BoardCoordinate;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Move;
import me.vlink102.personal.chess.Piece;

public class Rook extends Piece {
    private final Move.CastleType type;

    public Rook(BoardGUI board, boolean white, Move.CastleType type) {
        super(board, "R", white);
        this.type = type;
    }

    public Move.CastleType getType() {
        return type;
    }

    @Override
    public boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture) {
        return isStraight(from, to);
    }
}
