package me.vlink102.personal.chess;

public abstract class SpecialPiece extends Piece {
    public SpecialPiece(BoardGUI board, String abbr, boolean white) {
        super(board, abbr, white);
    }

    @Override
    public int points() {
        return 0;
    }

    public abstract char fenChar();


}
