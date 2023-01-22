package me.vlink102.personal.chess;

import me.vlink102.personal.chess.pieces.Pawn;

public class Move {
    private final Piece[][] board;
    private final Piece piece;
    private final BoardCoordinate from;
    private final BoardCoordinate to;
    private final CheckType check;
    private final boolean enPassant;
    private final Piece takes;
    private final Piece promotes;
    private final CastleType castleType;

    public enum CheckType {
        CHECK,
        CHECKMATE
    }

    public enum CastleType {
        KINGSIDE,
        QUEENSIDE
    }

    /**
     * @param piece Piece moved
     * @param from Square from
     * @param to Square to
     * @param check Piece checked opopnent
     * @param enPassant
     * @param takes Piece taken, or null
     * @param promotes Pawn promotion
     */
    public Move(Piece[][] board, Piece piece, BoardCoordinate from, BoardCoordinate to, CheckType check, boolean enPassant, Piece takes, Piece promotes, CastleType castleType) {
        this.board = board;
        this.piece = piece;
        this.from = from;
        this.to = to;
        this.check = check;
        this.enPassant = enPassant;
        this.takes = takes;
        this.promotes = promotes;
        this.castleType = castleType;
    }

    @Override
    public String toString() {
        // TODO
        // - check valid moves for all pieces
        // - check if piece can move to square
        // - check if type is same
        // - prepend the from-square
        StringBuilder move = new StringBuilder();

        if (castleType != null) {
            switch (castleType) {
                case KINGSIDE -> move.append("O-O");
                case QUEENSIDE -> move.append("O-O-O");
            }
        } else {
            if (piece instanceof Pawn) {
                if (takes != null) {
                    move.append(from.getFile());
                    move.append("x");
                    move.append(takes.getAbbr());
                } else {
                    move.append(to.toNotation());
                }
                if (promotes != null) {
                    move.append(promotes.getAbbr());
                }
            } else {
                move.append(piece.getAbbr());
                move.append(from.getFile());
                if (takes != null) {
                    move.append("x");
                    move.append(to.toNotation());
                }
            }

            if (check != null) {
                switch (check) {
                    case CHECK -> move.append("+");
                    case CHECKMATE -> move.append("#");
                }
            }

            if (enPassant) move.append(" e.p.");
        }

        return move.toString();
    }

    public Piece getPiece() {
        return piece;
    }

    public BoardCoordinate getFrom() {
        return from;
    }

    public BoardCoordinate getTo() {
        return to;
    }

    public CheckType getCheck() {
        return check;
    }

    public boolean isEnPassant() {
        return enPassant;
    }

    public Piece getTakes() {
        return takes;
    }

    public Piece getPromotes() {
        return promotes;
    }

    public CastleType getCastleType() {
        return castleType;
    }
}
