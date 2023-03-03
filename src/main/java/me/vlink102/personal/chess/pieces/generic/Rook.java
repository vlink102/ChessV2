package me.vlink102.personal.chess.pieces.generic;

import me.vlink102.personal.chess.*;
import me.vlink102.personal.chess.classroom.ClassroomGUI;
import me.vlink102.personal.chess.internal.BoardCoordinate;
import me.vlink102.personal.chess.internal.Move;
import me.vlink102.personal.chess.pieces.Piece;

public class Rook extends Piece {
    private Move.CastleType type = null;
    private final BoardGUI boardGUI;
    private final ClassroomGUI classroomGUI;
    private final BoardCoordinate initialSquare;

    public Rook(BoardGUI board, boolean white, BoardCoordinate initialSquare) {
        super(board, "R", white);
        this.boardGUI = board;
        this.classroomGUI = null;
        this.initialSquare = initialSquare;
    }

    public Rook(ClassroomGUI board, boolean white, BoardCoordinate initialSquare) {
        super(board, "R", white);
        this.classroomGUI = board;
        this.boardGUI = null;
        this.initialSquare = initialSquare;
    }

    public BoardCoordinate getInitialSquare() {
        return initialSquare;
    }

    public void disableCheckAbility() {
        if (boardGUI != null) {
            if (isWhite()) {
                if (type == Move.CastleType.QUEENSIDE) {
                    boardGUI.whiteCanCastleQueenside = false;
                }
                if (type == Move.CastleType.KINGSIDE) {
                    boardGUI.whiteCanCastleKingside = false;
                }
            } else {
                if (type == Move.CastleType.QUEENSIDE) {
                    boardGUI.blackCanCastleQueenside = false;
                }
                if (type == Move.CastleType.KINGSIDE) {
                    boardGUI.blackCanCastleKingside = false;
                }
            }
        }
    }

    public Move.CastleType getType() {
        if (type == null && boardGUI != null) {
            BoardCoordinate king = boardGUI.getKing(boardGUI.getSide(isWhite(), boardGUI.getGamePieces()));
            if (king.col() > initialSquare.col()) {
                this.type = Move.CastleType.QUEENSIDE;
            } else {
                this.type = Move.CastleType.KINGSIDE;
            }
        }
        return type;
    }


    @Override
    public boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture) {
        return isStraight(from, to);
    }

    @Override
    public int points() {
        return 5;
    }
}
