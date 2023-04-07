package me.vlink102.personal.chess.internal;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Chess;
import me.vlink102.personal.chess.pieces.Piece;
import me.vlink102.personal.chess.pieces.generic.King;
import me.vlink102.personal.chess.pieces.generic.Pawn;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

public class Move {
    private final BoardGUI boardGUI;
    private final Piece[][] snapshot;

    private final BoardCoordinate from;
    private final BoardCoordinate to;
    private final Piece piece;
    private final Check check;
    private final boolean enPassant;
    private final BoardCoordinate takeSquare;
    private Piece taken;
    private final Piece promotes;
    private final CastleType castleType;

    private final String importContent;

    private final MoveType type;

    public enum MoveType {
        MOVE,
        CASTLE,
        IMPORT,
        WHITE,
        BLACK,
        DRAW
    }

    private static final String GAME_ICONS_DIR = "game-icons/";
    private static final String MOVE_RATING_DIR = "game-icons/move-ratings/";
    private static final String TRADE_ICONS_DIR = "game-icons/trade-icons/";
    private static final String SPECIAL_PIECES_DIR = "special-pieces/";

    public static Image getMoveHighlightIcon(MoveHighlights highlights) {
        return getMoveHighlight(highlights.getURL(), highlights.isOnline());
    }

    private static Image getMoveHighlight(String url, boolean online) {
        try {
            if (url != null) {
                if (online) {
                    return ImageIO.read(new URL(url));
                } else {
                    return ImageIO.read(Objects.requireNonNull(Chess.class.getResource("/" + url + ".png")));
                }
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return null;
    }

    public static Image getInfoIcon(InfoIcons icon) {
        return getMoveHighlight(icon.getURL(), icon.isOnline());
    }

    public static Image getResource(String string) {
        URL res = Chess.class.getResource(string);
        if (res == null) {
            return null;
        }
        return Toolkit.getDefaultToolkit().getImage(res);
    }

    public static File getFile(String string) {
        URL res = Chess.class.getResource(string);
        if (res == null) {
            return null;
        }
        return new File(res.getFile());
    }

    public static BufferedImage getBufferedResource(String string) {
        URL res = Chess.class.getResource(string);
        try {
            return ImageIO.read(Objects.requireNonNull(res));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static final HashMap<MoveHighlights, Image> cachedHighlights = new HashMap<>();

    public static synchronized void loadCachedHighlights(double pieceSize) {
        for (MoveHighlights value : MoveHighlights.values()) {
            Image image = getMoveHighlightIcon(value);
            if (image == null) {
                cachedHighlights.put(value, null);
            } else {
                cachedHighlights.put(value, image.getScaledInstance((int) (pieceSize / 3), (int) (pieceSize / 3), Image.SCALE_SMOOTH));
            }
        }
    }

    public static final HashMap<InfoIcons, Image> cachedIcons = new HashMap<>();

    public static synchronized void loadCachedIcons(double pieceSize) {
        for (InfoIcons value : InfoIcons.values()) {
            Image image = getInfoIcon(value);
            if (image == null) {
                cachedIcons.put(value, null);
            } else {
                cachedIcons.put(value, image.getScaledInstance((int) (pieceSize / 3), (int) (pieceSize / 3), Image.SCALE_SMOOTH));
            }
        }
    }

    public enum MoveHighlights {
        BRILLIANT(false, MOVE_RATING_DIR + "brilliant"),
        GREAT(false, MOVE_RATING_DIR + "great"),
        BEST(false, MOVE_RATING_DIR + "best"),
        EXCELLENT(false, MOVE_RATING_DIR + "excellent"),
        GOOD(false, MOVE_RATING_DIR + "good"),
        BOOK(false, MOVE_RATING_DIR + "book"),
        INACCURACY(false, MOVE_RATING_DIR + "inaccuracy"),
        MISTAKE(false, MOVE_RATING_DIR + "mistake"),
        BLUNDER(false, MOVE_RATING_DIR + "blunder"),
        MISSED_WIN(false, MOVE_RATING_DIR + "missed_win"),
        FORCED(false, MOVE_RATING_DIR + "forced"),

        HIGHLIGHT(false,null),
        PREMOVE(false, null),
        ORANGE_HIGHLIGHT(false, null),
        BLUE_HIGHLIGHT(false, null),
        GREEN_HIGHLIGHT(false, null),

        HANGING_GOOD(false, TRADE_ICONS_DIR + "free_piece"),
        HANGING_BAD(false, TRADE_ICONS_DIR + "hanging"),

        TRADE_POINT_EQUAL(false, TRADE_ICONS_DIR + "equal_trade"),
        TRADE_POINT_GOOD(false, TRADE_ICONS_DIR + "upvote"),
        TRADE_POINT_BAD(false, TRADE_ICONS_DIR + "downvote"),

        TRADE_PIECE_EQUAL(false, TRADE_ICONS_DIR + "equal_trade"),
        TRADE_PIECE_GOOD(false, TRADE_ICONS_DIR + "upvote"),
        TRADE_PIECE_BAD(false, TRADE_ICONS_DIR + "downvote"),

        CUSTOM_HIGHLIGHT(false, null),

        MOVE(false, null),
        SELECTED(false, null),
        AVAILABLE(false, null);

        private final String iconUrl;
        private final boolean online;

        MoveHighlights(boolean online, String iconUrl) {
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

    public enum InfoIcons {
        CHECKMATE_BLACK(false, GAME_ICONS_DIR + "checkmate_black"),
        CHECKMATE_WHITE(false, GAME_ICONS_DIR + "checkmate_white"),
        DRAW_BLACK(false, GAME_ICONS_DIR + "draw_black"),
        DRAW_WHITE(false, GAME_ICONS_DIR + "draw_white"),
        RESIGN_BLACK(false, GAME_ICONS_DIR + "resign_black"),
        RESIGN_WHITE(false, GAME_ICONS_DIR + "resign_white"),
        WINNER(false, GAME_ICONS_DIR + "winner"),
        TIME_WHITE(false, GAME_ICONS_DIR + "white_time"),
        TIME_BLACK(false, GAME_ICONS_DIR + "black_time"),
        ABORTED(false, GAME_ICONS_DIR + "aborted");

        private final String iconUrl;
        private final boolean online;

        InfoIcons(boolean online, String iconUrl) {
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

    public Move(BoardGUI boardGUI, Piece moved, BoardCoordinate from, BoardCoordinate to, Check check, boolean enPassant, BoardCoordinate takeSquare, Piece pieceTaken, Piece promotes, CastleType castleType, MoveType type) {
        this.type = type;
        this.piece = moved;
        this.from = from;
        this.to = to;
        this.check = check;
        this.enPassant = enPassant;
        this.takeSquare = takeSquare;
        this.taken = pieceTaken;
        this.promotes = promotes;
        this.castleType = castleType;
        this.importContent = null;
        this.boardGUI = boardGUI;
        this.snapshot = Arrays.stream(boardGUI.getGamePieces()).map(Piece[]::clone).toArray(Piece[][]::new);
        undoRecent();
        moveNotation = toNotation();
    }

    /**
     * Import History move constructor
     */
    public Move(String content, BoardGUI boardGUI) {
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
        this.type = MoveType.IMPORT;
        this.boardGUI = boardGUI;
        this.snapshot = Arrays.stream(boardGUI.getGamePieces()).map(Piece[]::clone).toArray(Piece[][]::new);
        moveNotation = toNotation();
    }

    public void undoRecent() {
        if (from != null) {
            snapshot[from.row()][from.col()] = piece;
        }
        if (to != null) {
            snapshot[to.row()][to.col()] = null;
        }
        if (takeSquare != null) {
            snapshot[takeSquare.row()][takeSquare.col()] = taken;
        }
    }

    private final String moveNotation;

    public String toNotation() {
        if (type == MoveType.IMPORT) {
            return importContent;
        } else {
            assert from != null;
            assert to != null;
            assert piece != null;

            StringBuilder move = new StringBuilder();

            if (type == MoveType.WHITE) {
                move.append("1-0");
            } else if (type == MoveType.BLACK) {
                move.append("0-1");
            } else if (type == MoveType.DRAW) {
                move.append("1/2-1/2");
            } else if (type == MoveType.MOVE) {

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
                    HashMap<Piece, BoardCoordinate> coordinateList = boardGUI.getTypeSimilars(snapshot, piece);
                    for (Piece piece1 : coordinateList.keySet()) {
                        BoardCoordinate coordinate = coordinateList.get(piece1);
                        if (piece1.validMove(coordinate, to, true) && piece.validMove(coordinate, to, true) && boardGUI.notBlocked(snapshot, coordinate, to) && boardGUI.kingAvoidsCheck(snapshot, piece1, takeSquare, coordinate, to)) {
                            // Ambiguous
                            if (coordinate.col() == from.col() && coordinate.row() == from.row()) {
                                continue;
                            }
                            if (coordinate.col() != from.col()) {
                                move.append(from.getColString());
                            }

                            if (coordinate.col() == from.col() && coordinate.row() != from.row()) {
                                move.append(from.toNotation());
                            }
                            break;
                        }
                    }
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
            } else if (type == MoveType.CASTLE && castleType != null) {
                switch (castleType) {
                    case KINGSIDE -> move.append("O-O");
                    case QUEENSIDE -> move.append("O-O-O");
                }
                if (check != null) {
                    switch (check) {
                        case CHECK -> move.append("+");
                        case MATE -> move.append("#");
                    }
                }
            }
            return move.toString();
        }
    }

    @Override
    public String toString() {
        return moveNotation;
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

    /**
     *
     * @param piece
     * @param from
     * @param to
     */
    public record SimpleMove(BoardGUI boardGUI, Piece piece, BoardCoordinate from, BoardCoordinate to) {
        @Override
        public String toString() {
            return piece.toString() + ": " + from.toNotation() + " -> " + to.toNotation();
        }

        public Move toMove() {
            return new Move(boardGUI, piece, from, to, null, false, to, null, null, null, MoveType.MOVE);
        }
    }

    public MoveType getType() {
        return type;
    }
}
