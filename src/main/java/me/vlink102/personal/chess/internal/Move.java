package me.vlink102.personal.chess.internal;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Chess;
import me.vlink102.personal.chess.pieces.Piece;
import me.vlink102.personal.chess.pieces.generic.King;
import me.vlink102.personal.chess.pieces.generic.Pawn;
import org.apache.commons.io.FileUtils;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URISyntaxException;
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
    private Piece promotes;
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

    public static File getFile(final String fileName) {
        URL res = Chess.class.getResource(fileName);
        if (res == null) {
            return null;
        }
        try {
            return new File(res.toURI());
        } catch (URISyntaxException e) {
            throw new RuntimeException(e);
        }
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
                        move.append("=").append(promotes.getAbbr());
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

    public static Move parse(BoardGUI boardGUI, Piece[][] board, String moveString, boolean white) {
        Check check = (moveString.endsWith("#") ? Check.MATE : (moveString.endsWith("+") ? Check.CHECK : null));
        if (moveString.matches("(^O-O-O[+#]?)")) {
            BoardCoordinate kingFrom = boardGUI.getKing(boardGUI.getSide(white, board));
            BoardCoordinate kingTo = boardGUI.newKingFile(white, CastleType.QUEENSIDE);
            return new Move(boardGUI, board[kingFrom.row()][kingFrom.col()], kingFrom, kingTo, check, false, null, null, null, CastleType.QUEENSIDE, MoveType.CASTLE);
        }
        if (moveString.matches("(^O-O[+#]?)")) {
            BoardCoordinate kingFrom = boardGUI.getKing(boardGUI.getSide(white, board));
            BoardCoordinate kingTo = boardGUI.newKingFile(white, CastleType.KINGSIDE);
            return new Move(boardGUI, board[kingFrom.row()][kingFrom.col()], kingFrom, kingTo, check, false, null, null, null, CastleType.KINGSIDE, MoveType.CASTLE);
        }
        if (moveString.matches("(^[a-h]\\d[+#]?)")) { // pawn move (no capture) e4
            String[] split = moveString.split("");
            int newRow = BoardCoordinate.parseRow(split[1]);
            int col = BoardCoordinate.parseCol(split[0]);
            int oldRow = (white ? newRow - 1 : newRow + 1);
            Piece moved = board[oldRow][col];

            if (moved == null && (newRow == 3 || newRow == 4)) {
                oldRow = (white ? newRow - 2 : newRow + 2);
                moved = board[oldRow][col];
            }

            BoardCoordinate from = new BoardCoordinate(oldRow, col, boardGUI);
            BoardCoordinate to = new BoardCoordinate(newRow, col, boardGUI);

            return new Move(boardGUI, moved, from, to, check, false, to, null, null, null, MoveType.MOVE);
        }
        if (moveString.matches("(^[a-h]x[a-h]\\d[+#]?)")) { // pawn capture exd5
            String[] split = moveString.split("");
            int newRow = BoardCoordinate.parseRow(split[3]);
            int newCol = BoardCoordinate.parseCol(split[2]);
            int oldCol = BoardCoordinate.parseCol(split[0]);
            int oldRow = (white ? newRow - 1 : newRow + 1);
            Piece moved = board[oldRow][oldCol];
            Piece taken = board[newRow][newCol];
            BoardCoordinate takes = null;
            boolean enPassant = false;
            if (taken == null) { // is enpassant
                takes = new BoardCoordinate(oldRow, newCol, boardGUI);
                enPassant = true;
                taken = board[oldRow][newCol];
            } else {
                takes = new BoardCoordinate(newRow, newCol, boardGUI);
            }
            BoardCoordinate from = new BoardCoordinate(oldRow, oldCol, boardGUI);
            BoardCoordinate to = new BoardCoordinate(newRow, newCol, boardGUI);

            return new Move(boardGUI, moved, from, to, check, enPassant, takes, taken, null, null, MoveType.MOVE);
        }
        if (moveString.matches("(^[a-h][81][=]?[QRNB][+#]?)")) { // pawn promotion (no capture) b8=R
            String[] split = moveString.split("");
            int newRow = BoardCoordinate.parseRow(split[1]);
            int col = BoardCoordinate.parseCol(split[0]);
            int oldRow = (white ? newRow - 1 : newRow + 1);
            Piece moved = board[oldRow][col];
            BoardCoordinate from = new BoardCoordinate(oldRow, col, boardGUI);
            BoardCoordinate to = new BoardCoordinate(newRow, col, boardGUI);

            int choice = switch (split[3]) {
                case "Q" -> 1;
                case "R" -> 2;
                case "N" -> 3;
                case "B" -> 4;
                default -> -1;
            };

            Piece promotes = BoardGUI.conv(boardGUI, choice, to, white);

            return new Move(boardGUI, moved, from, to, check, false, to, null, promotes, null, MoveType.MOVE);
        }
        if (moveString.matches("(^[a-h]x[a-h][81][=]?[QRNB][+#]?)")) { // pawn promotion capture bxc8=Q
            String[] split = moveString.split("");
            int oldCol = BoardCoordinate.parseCol(split[0]);
            int newCol = BoardCoordinate.parseCol(split[2]);
            int newRow = BoardCoordinate.parseRow(split[3]);
            int oldRow = (white ? newRow - 1 : newRow + 1);

            Piece moved = board[oldRow][oldCol];
            Piece taken = board[newRow][newCol];

            BoardCoordinate takes = new BoardCoordinate(newRow, newCol, boardGUI);
            BoardCoordinate from = new BoardCoordinate(oldRow, oldCol, boardGUI);
            BoardCoordinate to = new BoardCoordinate(newRow, newCol, boardGUI);

            int choice = switch (split[5]) {
                case "Q" -> 1;
                case "R" -> 2;
                case "N" -> 3;
                case "B" -> 4;
                default -> -1;
            };

            Piece promotes = BoardGUI.conv(boardGUI, choice, to, white);

            return new Move(boardGUI, moved, from, to, check, false, takes, taken, promotes, null, MoveType.MOVE);
        }
        if (moveString.matches("(^[RNBQ][a-h]{2}\\d[+#]?)")) { // ambiguous move #1 (no capture) Nbd2
            String[] split = moveString.split("");
            int newRow = BoardCoordinate.parseRow(split[3]);
            int newCol = BoardCoordinate.parseCol(split[2]);
            int oldCol = BoardCoordinate.parseCol(split[1]);

            BoardCoordinate to = new BoardCoordinate(newRow, newCol, boardGUI);
            BoardCoordinate from = findPieceInColumn(oldCol, boardGUI, board, to, split[0], white);

            Piece moved = board[from.row()][from.col()];

            return new Move(boardGUI, moved, from, to, check, false, to, null, null, null, MoveType.MOVE);
        }
        if (moveString.matches("(^[RNBQ][a-h]x[a-h]\\d[+#]?)")) { // ambiguous capture #1 Nbxd2
            String[] split = moveString.split("");
            int newRow = BoardCoordinate.parseRow(split[4]);
            int newCol = BoardCoordinate.parseCol(split[3]);
            int oldCol = BoardCoordinate.parseCol(split[1]);

            BoardCoordinate to = new BoardCoordinate(newRow, newCol, boardGUI);
            BoardCoordinate from = findPieceInColumn(oldCol, boardGUI, board, to, split[0], white);

            Piece moved = board[from.row()][from.col()];
            BoardCoordinate takes = new BoardCoordinate(newRow, newCol, boardGUI);
            Piece taken = board[newRow][newCol];

            return new Move(boardGUI, moved, from, to, check, false, takes, taken, null, null, MoveType.MOVE);
        }
        if (moveString.matches("(^[QRNB](?>[a-h]\\d){2}[+#]?)")) { // ambiguous move #2 (no capture) Be4d5
            String[] split = moveString.split("");
            int newRow = BoardCoordinate.parseRow(split[4]);
            int newCol = BoardCoordinate.parseCol(split[3]);
            int oldRow = BoardCoordinate.parseRow(split[2]);
            int oldCol = BoardCoordinate.parseCol(split[1]);

            BoardCoordinate from = new BoardCoordinate(oldRow, oldCol, boardGUI);
            BoardCoordinate to = new BoardCoordinate(newRow, newCol, boardGUI);
            Piece moved = board[oldRow][oldCol];

            return new Move(boardGUI, moved, from, to, check, false, to, null, null, null, MoveType.MOVE);
        }
        if (moveString.matches("(^[QRNB][a-h]\\dx[a-h]\\d[+#]?)")) { // ambiguous capture #2 Be4xd5
            String[] split = moveString.split("");
            int newRow = BoardCoordinate.parseRow(split[5]);
            int newCol = BoardCoordinate.parseCol(split[4]);
            int oldRow = BoardCoordinate.parseRow(split[2]);
            int oldCol = BoardCoordinate.parseCol(split[1]);

            BoardCoordinate from = new BoardCoordinate(oldRow, oldCol, boardGUI);
            BoardCoordinate to = new BoardCoordinate(newRow, newCol, boardGUI);
            BoardCoordinate takes = new BoardCoordinate(newRow, newCol, boardGUI);
            Piece taken = board[newRow][newCol];
            Piece moved = board[oldRow][oldCol];

            return new Move(boardGUI, moved, from, to, check, false, takes, taken, null, null, MoveType.MOVE);
        }
        if (moveString.matches("(^[RNBQK][a-h]\\d[+#]?)")) { // normal move (no capture) Ra8
            String[] split = moveString.split("");
            int newCol = BoardCoordinate.parseCol(split[1]);
            int newRow = BoardCoordinate.parseRow(split[2]);
            BoardCoordinate to = new BoardCoordinate(newRow, newCol, boardGUI);
            BoardCoordinate from = findPiece(boardGUI, board, to, split[0], white);
            if (from == null) throw new RuntimeException();
            Piece moved = board[from.row()][from.col()];

            return new Move(boardGUI, moved, from, to, check, false, to, null, null, null, MoveType.MOVE);
        }
        if (moveString.matches("(^[RNBQK]x[a-h]\\d[+#]?)")) { // normal capture Rxa8
            String[] split = moveString.split("");
            int newCol = BoardCoordinate.parseCol(split[2]);
            int newRow = BoardCoordinate.parseRow(split[3]);
            BoardCoordinate to = new BoardCoordinate(newRow, newCol, boardGUI);
            BoardCoordinate takes = new BoardCoordinate(newRow, newCol, boardGUI);
            Piece taken = board[newRow][newCol];
            BoardCoordinate from = findPiece(boardGUI, board, to, split[0], white);
            if (from == null) throw new RuntimeException();
            Piece moved = board[from.row()][from.col()];

            return new Move(boardGUI, moved, from, to, check, false, takes, taken, null, null, MoveType.MOVE);
        }
        if (moveString.equalsIgnoreCase("0-1")) {
            if (!boardGUI.isGameOver()) {
                boardGUI.setGameOver(BoardGUI.GameOverType.RESIGNATION_WHITE);
            }
            return new Move(boardGUI, null, null, null, null, false, null, null, null, null, MoveType.BLACK);
        }
        if (moveString.equalsIgnoreCase("1-0")) {
            if (!boardGUI.isGameOver()) {
                boardGUI.setGameOver(BoardGUI.GameOverType.RESIGNATION_BLACK);
            }
            return new Move(boardGUI, null, null, null, null, false, null, null, null, null, MoveType.WHITE);
        }
        if (moveString.equalsIgnoreCase("1/2-1/2")) {
            if (!boardGUI.isGameOver()) {
                boardGUI.setGameOver(BoardGUI.GameOverType.DRAW_BY_AGREEMENT);
            }
            return new Move(boardGUI, null, null, null, null, false, null, null, null, null, MoveType.DRAW);
        }
        return null;
    }

    private static BoardCoordinate findPiece(BoardGUI boardGUI, Piece[][] board, BoardCoordinate to, String abbr, boolean white) {
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                Piece piece = board[i][j];
                if (piece != null) {
                    if (piece.isWhite() != white) continue;
                    BoardCoordinate from = new BoardCoordinate(i, j, boardGUI);
                    if (piece.getAbbr().equalsIgnoreCase(abbr) && piece.validMove(from, to, board[to.row()][to.col()] != null) && boardGUI.notBlocked(board, from, to)) {
                        return new BoardCoordinate(i, j, boardGUI);
                    }
                }
            }
        }
        return null;
    }

    private static BoardCoordinate findPieceInColumn(int col, BoardGUI boardGUI, Piece[][] board, BoardCoordinate to, String abbr, boolean white) {
        for (int i = 0; i < board.length; i++) {
            Piece piece = board[i][col];
            if (piece != null) {
                if (piece.isWhite() != white) continue;
                BoardCoordinate from = new BoardCoordinate(i, col, boardGUI);
                if (piece.getAbbr().equalsIgnoreCase(abbr) && piece.validMove(from, to, board[to.row()][to.col()] != null) && boardGUI.notBlocked(board, from, to)) {
                    return new BoardCoordinate(i, col, boardGUI);
                }
            }
        }
        return null;
    }

    private static BoardCoordinate findPieceInRow(int row, BoardGUI boardGUI, Piece[][] board, BoardCoordinate to, String abbr, boolean white) {
        for (int i = 0; i < board.length; i++) {
            Piece piece = board[row][i];
            if (piece != null) {
                if (piece.isWhite() != white) continue;
                BoardCoordinate from = new BoardCoordinate(row, i, boardGUI);
                if (piece.getAbbr().equalsIgnoreCase(abbr) && piece.validMove(from, to, board[to.row()][to.col()] != null) && boardGUI.notBlocked(board, from, to)) {
                    return new BoardCoordinate(row, i, boardGUI);
                }
            }
        }
        return null;
    }

    public MoveType getType() {
        return type;
    }

    public void setPromotes(Piece promotes) {
        this.promotes = promotes;
    }
}
