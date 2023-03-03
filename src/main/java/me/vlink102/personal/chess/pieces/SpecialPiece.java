package me.vlink102.personal.chess.pieces;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.classroom.ClassroomGUI;

public abstract class SpecialPiece extends Piece {
    public SpecialPiece(BoardGUI board, String abbr, boolean white) {
        super(board, abbr, white);
    }
    public SpecialPiece(ClassroomGUI board, String abbr, boolean white) {
        super(board, abbr, white);
    }

    @Override
    public int points() {
        return 0;
    }

    public abstract char fenChar();


}
