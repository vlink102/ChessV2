package me.vlink102.personal.chess;

import me.vlink102.personal.chess.pieces.Pawn;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Objects;

public class Move {
    private final BoardCoordinate from;
    private final BoardCoordinate to;
    private final Piece piece;
    private final Check check;
    private final boolean enPassant;
    private final BoardCoordinate takeSquare;
    private final Piece taken;
    private final Piece promotes;
    private final CastleType castleType;

    private final boolean isImport;
    private final String importContent;

    public static Image getHighlightIcon(Highlights highlights) {
        try {
            if (highlights.isOnline()) {
                return ImageIO.read(new URL(highlights.getURL()));
            } else {
                return ImageIO.read(Objects.requireNonNull(Chess.class.getResource("/" + highlights.getURL() + ".png")));
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public enum Highlights {
        BRILLIANT(true, "https://images.chesscomfiles.com/uploads/v1/images_users/tiny_mce/PedroPinhata/phpCWiDaX.png"),
        GREAT(false, "great"),
        BEST(true, "https://images.chesscomfiles.com/uploads/v1/images_users/tiny_mce/PedroPinhata/phpKAZWos.png"),
        EXCELLENT(true, "https://images.chesscomfiles.com/uploads/v1/images_users/tiny_mce/PedroPinhata/phpOnfDmd.png"),
        GOOD(true, "https://images.chesscomfiles.com/uploads/v1/images_users/tiny_mce/PedroPinhata/phplIugqj.png"),
        BOOK(true, "https://images.chesscomfiles.com/uploads/v1/images_users/tiny_mce/PedroPinhata/phpfMwiZv.png"),
        INACCURACY(true, "https://images.chesscomfiles.com/uploads/v1/images_users/tiny_mce/PedroPinhata/phppqqBrb.png"),
        MISTAKE(true, "https://images.chesscomfiles.com/uploads/v1/images_users/tiny_mce/PedroPinhata/phpEgRwsV.png"),
        BLUNDER(true, "https://images.chesscomfiles.com/uploads/v1/images_users/tiny_mce/PedroPinhata/phpGZ1eLb.png"),
        MISSED_WIN(true, "https://images.chesscomfiles.com/uploads/v1/images_users/tiny_mce/PedroPinhata/phpcXUmZL.png"),
        FORCED(false, "forced"),

        HIGHLIGHT(false,null),
        MOVE(false, null),
        SELECTED(false, null),
        AVAILABLE(false, null);

        private final String iconUrl;
        private final boolean online;

        Highlights(boolean online, String iconUrl) {
            this.iconUrl = iconUrl;
            this.online = online;
        }

        public boolean isOnline() {
            return online;
        }

        public String getURL() {
            return iconUrl;
        }
    }

    public enum CastleType {
        KINGSIDE,
        QUEENSIDE
    }

    public enum Check {
        CHECK,
        MATE
    }

    /**
     * @param moved
     * @param from
     * @param to
     * @param check
     * @param enPassant
     * @param takeSquare
     * @param promotes
     * @param castleType
     */
    public Move(Piece moved, BoardCoordinate from, BoardCoordinate to, Check check, boolean enPassant, BoardCoordinate takeSquare, Piece pieceTaken, Piece promotes, CastleType castleType) {
        this.piece = moved;
        this.from = from;
        this.to = to;
        this.check = check;
        this.enPassant = enPassant;
        this.takeSquare = takeSquare;
        this.taken = pieceTaken;
        this.promotes = promotes;
        this.castleType = castleType;
        this.isImport = false;
        this.importContent = null;
    }

    /**
     * Import History move constructor
     */
    public Move(String content) {
        this.isImport = true;
        this.importContent = content;

        this.piece = null;
        this.from = null;
        this.to = null;
        this.check = null;
        this.enPassant = false;
        this.takeSquare = null;
        this.taken = null;
        this.promotes = null;
        this.castleType = null;
    }

    @Override
    public String toString() {
        if (isImport) {
            return importContent;
        } else {
            assert from != null;
            assert to != null;
            assert piece != null;

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
                    if (taken != null) {
                        move.append(from.getColString());
                        move.append("x");
                        move.append(to.toNotation());
                    } else {
                        move.append(to.toNotation());
                    }
                    if (promotes != null) {
                        move.append(promotes.getAbbr());
                    }
                } else {
                    move.append(piece.getAbbr());
                    if (taken != null) {
                        move.append("x");
                    }
                    move.append(to.toNotation());
                }

                if (check != null) {
                    switch (check) {
                        case CHECK -> move.append("+");
                        case MATE -> move.append("#");
                    }
                }

                if (enPassant) move.append(" e.p.");
            }

            return move.toString();
        }
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

    public Check getCheck() {
        return check;
    }

    public boolean isEnPassant() {
        return enPassant;
    }

    public BoardCoordinate getTakeSquare() {
        return takeSquare;
    }

    public Piece getPromotes() {
        return promotes;
    }

    public Piece getTaken() {
        return taken;
    }

    public CastleType getCastleType() {
        return castleType;
    }

    public boolean isImport() {
        return isImport;
    }
}
