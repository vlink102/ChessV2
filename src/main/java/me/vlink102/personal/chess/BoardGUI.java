package me.vlink102.personal.chess;

import me.vlink102.personal.chess.internal.*;
import me.vlink102.personal.chess.pieces.Piece;
import me.vlink102.personal.chess.pieces.SpecialPiece;
import me.vlink102.personal.chess.pieces.generic.*;
import me.vlink102.personal.chess.pieces.generic.special.asian.DragonHorse;
import me.vlink102.personal.chess.pieces.generic.special.asian.DragonKing;
import me.vlink102.personal.chess.pieces.generic.special.historical.*;
import me.vlink102.personal.chess.ratings.Rating;
import me.vlink102.personal.chess.ui.CoordinateGUI;
import me.vlink102.personal.chess.ui.IconDisplayGUI;
import me.vlink102.personal.chess.ui.history.CaptureGUI;
import me.vlink102.personal.chess.ui.history.HistoryGUI;
import me.vlink102.personal.chess.ui.interactive.PieceInteraction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class BoardGUI extends JPanel {
    private final Chess chess;
    private final PieceInteraction pieceInteraction;

    private final HistoryGUI historyGUI;
    private final CaptureGUI captureGUI;
    private final CoordinateGUI coordinateGUI;
    private final IconDisplayGUI iconDisplayGUI;
    private CoordinateDisplayType coordinateDisplayType;

    private Chess.BoardLayout currentLayout;
    public static int boardSize;
    public static int decBoardSize;

    private GameType gameType;

    private final OnlineAssets onlineAssets;
    private boolean useOnline;
    private PieceDesign pieceTheme;
    private Colours boardTheme;
    private int pieceSize;
    private Piece[][] gamePieces;
    private Move.MoveHighlights[][] highlightedSquares;
    private Move.MoveHighlights[][] highlightIconAccompaniment;
    private Move.MoveHighlights[][] moveHighlights;
    private Move.InfoIcons[][] gameHighlights;
    private ColorScheme.StaticColors[][] staticHighlights;
    private List<Piece> capturedPieces;
    private boolean playAsWhite;
    private BoardView view;
    private int dimension;

    private BoardCoordinate epSQ = null; // Used for imports

    private boolean whiteTurn;
    private int halfMoveClock;
    private int fullMoveCount;
    private int fiftyMoveRule;
    public boolean whiteCanCastleQueenside;
    public boolean whiteCanCastleKingside;
    public boolean blackCanCastleQueenside;
    public boolean blackCanCastleKingside;

    private BoardCoordinate tileSelected;
    private Piece selected;

    private List<Move> history;

    private OpponentType opponentType;
    private MoveStyle moveMethod;

    private HintStyle.Move moveStyle;
    private HintStyle.Capture captureStyle;

    private GameOverType gameOver = null;

    private List<String> gameFENHistory;

    private boolean shouldCalculateELO;

    public enum GameOverType {
        STALEMATE,
        CHECKMATE_WHITE,
        CHECKMATE_BLACK,
        FIFTY_MOVE_RULE,
        INSUFFICIENT_MATERIAL,
        DRAW_BY_REPETITION,
        DRAW_BY_AGREEMENT,
        RESIGNATION_WHITE,
        RESIGNATION_BLACK,
        ILLEGAL_POSITION,
        ABORTED_WHITE,
        ABORTED_BLACK,
        ABANDONMENT_WHITE,
        ABANDONMENT_BLACK,
        TIME_WHITE,
        TIME_BLACK
    }

    public enum BoardView {
        BLACK,
        WHITE
    }

    public enum OpponentType {
        AI_1,
        AI_2,
        AUTO_SWAP,
        MANUAL
    }

    public enum GameType {
        DEFAULT,
        ATOMIC,
        FOG_OF_WAR
    }

    public enum MoveStyle {
        DRAG,
        CLICK,
        BOTH
    }

    public class HintStyle {
        public enum Move {
            DOT,
            SQUARE
        }

        public enum Capture {
            RING,
            SQUARE
        }
    }

    public enum CoordinateDisplayType {
        NONE,
        INSIDE,
        OUTSIDE
    }

    public enum TradeType {
        WHITE,
        BLACK,
        EQUAL
    }

    public void setOpponent(OpponentType type) {
        opponentType = type;
    }

    public void setupBoard(Chess.BoardLayout layout) {
        this.gameOver = null;
        this.shouldCalculateELO = true;

        if (layout == Chess.BoardLayout.CHESS960 && boardSize != 8) {
            this.currentLayout = Chess.BoardLayout.DEFAULT;
        } else {
            this.currentLayout = layout;
        }

        this.dimension = pieceSize * boardSize;
        this.view = playAsWhite ? BoardView.WHITE : BoardView.BLACK;
        this.gamePieces = new Piece[boardSize][boardSize];
        this.highlightedSquares = new Move.MoveHighlights[boardSize][boardSize];
        this.highlightIconAccompaniment = new Move.MoveHighlights[boardSize][boardSize];
        this.moveHighlights = new Move.MoveHighlights[boardSize][boardSize];
        this.gameHighlights = new Move.InfoIcons[boardSize][boardSize];
        this.staticHighlights = new ColorScheme.StaticColors[boardSize][boardSize];
        this.capturedPieces = new ArrayList<>();

        this.halfMoveClock = 0;
        this.fullMoveCount = 1;
        this.fiftyMoveRule = 0;

        this.whiteTurn = true;
        this.whiteCanCastleKingside = true;
        this.whiteCanCastleQueenside = true;
        this.blackCanCastleKingside = true;
        this.blackCanCastleQueenside = true;

        this.history = new ArrayList<>();
        this.gameFENHistory = new ArrayList<>();

        this.tileSelected = null;
        this.selected = null;

        setupPieces(layout);
    }

    public void resetBoard(Chess.BoardLayout layout) {
        shouldCalculateELO = true;
        setupBoard(layout);
        repaint();
        displayPieces();
        chess.createPopUp("Success!", "Board reset", Move.MoveHighlights.EXCELLENT);
    }

    public void resetBoard() {
        resetBoard(currentLayout);
    }

    public BoardGUI(Chess chess, int pSz, int boardSize, boolean useOnline, boolean playAsWhite, OpponentType type, GameType gameType, Chess.BoardLayout layout, PieceDesign pieceTheme, Colours boardTheme, MoveStyle moveMethod, HintStyle.Move moveStyle, HintStyle.Capture captureStyle, CoordinateDisplayType coordinateDisplayType) {
        BoardGUI.boardSize = boardSize;
        this.pieceSize = pSz;
        BoardGUI.decBoardSize = BoardGUI.boardSize - 1;
        this.chess = chess;
        if (boardSize != 8) {
            this.useOnline = false;
        } else {
            this.useOnline = useOnline;
        }
        this.gameType = gameType;
        this.coordinateDisplayType = coordinateDisplayType;
        this.boardTheme = boardTheme;
        this.pieceTheme = pieceTheme;
        this.playAsWhite = playAsWhite;
        this.opponentType = type;
        this.moveMethod = moveMethod;
        this.captureStyle = captureStyle;
        this.moveStyle = moveStyle;
        this.historyGUI = new HistoryGUI(this);
        this.captureGUI = new CaptureGUI(this);
        this.coordinateGUI = new CoordinateGUI(this);
        this.iconDisplayGUI = new IconDisplayGUI(this);
        this.onlineAssets = new OnlineAssets(this);

        setFont(Chess.def.deriveFont(Chess.defaultOffset - 4f));

        setupBoard(layout);

        addMouseListener(highlightListener());

        this.pieceInteraction = new PieceInteraction(chess, this);
        addMouseListener(pieceInteraction);
        addMouseMotionListener(pieceInteraction);

        registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, 0, false), "black-view", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setView(BoardView.BLACK);
                displayPieces();
                repaint();
            }
        });
        registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, 0, false), "white-view", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setView(BoardView.WHITE);
                displayPieces();
                repaint();
            }
        });
        registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0, false), "default-view", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setView(playAsWhite ? BoardView.WHITE : BoardView.BLACK);
                displayPieces();
                repaint();
            }
        });
        registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0, false), "show-hanging", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showHangingPieces();
                displayPieces();
                repaint();
            }
        });
        registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_1, 0, true), "show-hanging-released", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeHighlights(Move.MoveHighlights.HANGING_BAD);
                removeHighlights(Move.MoveHighlights.HANGING_GOOD);
                displayPieces();
                repaint();
            }
        });
        registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0, false), "show-point-trades", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTrades(true);
                displayPieces();
                repaint();
            }
        });
        registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0, true), "show-point-trades-released", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeHighlights(Move.MoveHighlights.TRADE_POINT_EQUAL);
                removeHighlights(Move.MoveHighlights.TRADE_POINT_BAD);
                removeHighlights(Move.MoveHighlights.TRADE_POINT_GOOD);
                displayPieces();
                repaint();
            }
        });
        registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0, false), "show-piece-trades", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTrades(false);
                displayPieces();
                repaint();
            }
        });
        registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_3, 0, true), "show-piece-trades-released", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                removeHighlights(Move.MoveHighlights.TRADE_PIECE_EQUAL);
                removeHighlights(Move.MoveHighlights.TRADE_PIECE_BAD);
                removeHighlights(Move.MoveHighlights.TRADE_PIECE_GOOD);
                displayPieces();
                repaint();
            }
        });
        registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_4, 0, false), "show-pressured-tiles", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayPieces();
                repaint();
            }
        });

        setLayout(new GridLayout(boardSize, boardSize));
        for (int rank = 0; rank < boardSize; rank++) {
            for (int file = 0; file < boardSize; file++) {
                JPanel square = new JPanel(new BorderLayout());
                square.setOpaque(false);
                add(square);
            }
        }

        OnlineAssets.updatePieceDesigns(this);

        Move.loadCachedIcons(pieceSize);
        Move.loadCachedHighlights(pieceSize);
    }

    public void registerKeyBinding(KeyStroke keyStroke, String name, Action action) {
        InputMap im = chess.getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = chess.getActionMap();

        im.put(keyStroke, name);
        am.put(name, action);
    }

    public static boolean validateBoard(String board, int boardSize) {
        for (String s : board.split("/")) {
            if (!isValidFENRow(s, boardSize)) {
                return false;
            }
        }
        return true;
    }

    public static boolean validateFEN(String FEN, int boardSize) {
        String[] sections = FEN.split(" ");
        if (sections.length != 6) {
            return false;
        }
        if (sections[0].split("/").length != boardSize) {
            return false;
        }
        if (!sections[1].equalsIgnoreCase("w") && !sections[1].equalsIgnoreCase("b")) {
            return false;
        }
        if (!(Integer.parseInt(sections[4]) / 2 == Integer.parseInt(sections[5]) - 1)) {
            return false;
        }
        if (!(sections[0].contains("k") && sections[0].contains("K"))) {
            return false;
        }
        if (!BoardCoordinate.isValidTile(boardSize, sections[3]) && !sections[3].equalsIgnoreCase("-")) {
            return false;
        }
        return validateBoard(sections[0], boardSize);
    }

    public void loadFEN(String FEN) {
        shouldCalculateELO = false;
        if (validateFEN(FEN, boardSize)) {
            gamePieces = new Piece[boardSize][boardSize];
            history = new ArrayList<>();
            String[] sections = FEN.split(" ");
            String[] board = sections[0].split("/");

            whiteCanCastleKingside = sections[2].contains("K");
            whiteCanCastleQueenside = sections[2].contains("Q");
            blackCanCastleKingside = sections[2].contains("k");
            blackCanCastleQueenside = sections[2].contains("q");

            for (int rank = 0; rank < boardSize; rank++) {
                String row = board[decBoardSize - rank];
                String[] chars = row.split("");
                StringBuilder parseEmpty = new StringBuilder();

                for (int i = 0; i < chars.length; i++) {
                    String s = chars[i];

                    if (s.equals(")")) {
                        continue;
                    }
                    if (s.equals("(")) {
                        String number = "";
                        for (int j = 1; j < chars.length; j++) {
                            if (Character.isDigit(chars[j + i].toCharArray()[0])) {
                                number += Integer.parseInt(chars[j + i]);
                            } else {
                                if (chars[j + i].equals(")")) {
                                    i += j;
                                }
                                break;
                            }
                        }
                        parseEmpty.append(" ".repeat(Integer.parseInt(number)));
                    } else {
                        if (s.matches("\\d")) {
                            parseEmpty.append(" ".repeat(Integer.parseInt(s)));
                        } else {
                            parseEmpty.append(s);
                        }
                    }
                }
                String[] newRow = parseEmpty.toString().split("");
                for (int i = 0; i < boardSize; i++) {
                    boolean isUpper = newRow[i].matches("[A-Z]");
                    switch (newRow[i].toLowerCase()) {
                        case "r" -> {
                            if (isUpper) {
                                if (i == 0 && rank == 0 && whiteCanCastleQueenside) {
                                    gamePieces[rank][i] = new Rook(this, true, new BoardCoordinate(rank, i));
                                } else if (i == decBoardSize && rank == 0 && whiteCanCastleKingside) {
                                    gamePieces[rank][i] = new Rook(this, true, new BoardCoordinate(rank, i));
                                } else {
                                    gamePieces[rank][i] = new Rook(this, true, new BoardCoordinate(rank, i));
                                }

                            } else {
                                if (i == 0 && rank == decBoardSize && blackCanCastleQueenside) {
                                    gamePieces[rank][i] = new Rook(this, false, new BoardCoordinate(rank, i));
                                } else if (i == decBoardSize && rank == decBoardSize && blackCanCastleKingside) {
                                    gamePieces[rank][i] = new Rook(this, false, new BoardCoordinate(rank, i));
                                } else {
                                    gamePieces[rank][i] = new Rook(this, false, new BoardCoordinate(rank, i));
                                }
                            }
                        }
                        case "n" -> gamePieces[rank][i] = new Knight(this, isUpper);
                        case "b" -> gamePieces[rank][i] = new Bishop(this, isUpper);
                        case "q" -> gamePieces[rank][i] = new Queen(this, isUpper);
                        case "k" -> gamePieces[rank][i] = new King(this, isUpper);
                        case "p" -> {
                            Pawn pawn = new Pawn(this, isUpper);
                            if (isUpper) {
                                if (rank != 1) {
                                    pawn.incrementMoves();
                                }
                            } else {
                                if (rank != 6) {
                                    pawn.incrementMoves();
                                }
                            }
                            gamePieces[rank][i] = pawn;
                        }
                        case " " -> gamePieces[rank][i] = null;
                        case "a" -> gamePieces[rank][i] = new Amazon(this, isUpper);
                        case "c" -> gamePieces[rank][i] = new Camel(this, isUpper);
                        case "e" -> gamePieces[rank][i] = new Elephant(this, isUpper);
                        case "s" -> gamePieces[rank][i] = new Princess(this, isUpper);
                        case "m" -> gamePieces[rank][i] = new Man(this, isUpper);
                        case "i" -> gamePieces[rank][i] = new Minister(this, isUpper);
                        case "h" -> gamePieces[rank][i] = new Empress(this, isUpper);
                        case "y" -> gamePieces[rank][i] = new DragonKing(this, isUpper);
                        case "u" -> gamePieces[rank][i] = new DragonHorse(this, isUpper);
                    }
                }
            }

            switch (currentLayout) {
                case DEFAULT -> {
                    if (gamePieces[0][0] == null && whiteCanCastleQueenside) {
                        whiteCanCastleQueenside = false;
                    }
                    if (gamePieces[0][decBoardSize] == null && whiteCanCastleKingside) {
                        whiteCanCastleKingside = false;
                    }
                    if (gamePieces[decBoardSize][0] == null && blackCanCastleQueenside) {
                        blackCanCastleQueenside = false;
                    }
                    if (gamePieces[decBoardSize][decBoardSize] == null && blackCanCastleKingside) {
                        blackCanCastleKingside = false;
                    }
                }
                case CHESS960 -> {
                    for (int i = 0; i < boardSize; i++) {
                        for (int j = 0; j < boardSize; j++) {
                            if (gamePieces[i][j] != null && gamePieces[i][j] instanceof Rook rook) {
                                if (!rook.getInitialSquare().equals(new BoardCoordinate(i, j))) {
                                    rook.disableCheckAbility();
                                }
                            }
                        }
                    }
                }
            }

            whiteTurn = Objects.equals(sections[1], "w");

            if (!sections[3].equalsIgnoreCase("-")) {
                int col = BoardCoordinate.parseCol(sections[3].split("")[0]);
                int row = BoardCoordinate.parseRow(sections[3].split("")[1]);
                epSQ = new BoardCoordinate(row, col);
            }

            halfMoveClock = Integer.parseInt(sections[4]);
            fullMoveCount = Integer.parseInt(sections[5]);

            addImportHistory();

            displayPieces();
            repaint();
            gameOver = null;

            if (bothKingsInCheck(gamePieces)) {
                gameOver = GameOverType.ILLEGAL_POSITION;
                createGameOverScreen(gameOver);
            } else {
                if (!gameOver()) {
                    highlightChecks();
                    displayPieces();
                    repaint();
                    chess.createPopUp("Success!", "Game state loaded", Move.MoveHighlights.EXCELLENT);
                }
            }
        } else {
            String result = fixFENString(FEN);
            if (result == null) {
                chess.createPopUp("Invalid FEN String: " + FEN + "\n\nFix Failed:\n - Wrong number of Kings\n\n" + result, "Could not load game state", Move.MoveHighlights.MISTAKE);
                gameOver = GameOverType.ILLEGAL_POSITION;
                gameOver();
            } else {
                if (!validateFEN(result, boardSize)) {
                    chess.createPopUp("Invalid FEN String: " + FEN + "\n\nFix Failed:\n - Could not repair FEN String\n\n" + result, "Could not load game state", Move.MoveHighlights.BLUNDER);
                    gameOver = GameOverType.ILLEGAL_POSITION;
                    gameOver();
                } else {
                    chess.createPopUp("Invalid FEN String: " + FEN + "\n\nFix Successful:\n - Repaired broken FEN String\n\n" + result, "Game state repaired", Move.MoveHighlights.EXCELLENT);
                    loadFEN(result);
                }
            }
        }
    }

    public enum GameStage {
        END_GAME,
        MID_GAME,
        EARLY_GAME
    }

    public String randomFENBoard(GameStage stage) {
        Piece[][] board = new Piece[boardSize][boardSize];
        randomKing(board, true);
        randomKing(board, false);

        randomPawns(board, stage, true);
        randomPawns(board, stage, false);

        randomPieces(board, new Bishop(this, true), stage);
        randomPieces(board, new Bishop(this, false), stage);

        randomPieces(board, new Knight(this, true), stage);
        randomPieces(board, new Knight(this, false), stage);

        randomPieces(board, new Queen(this, true), stage);
        randomPieces(board, new Queen(this, false), stage);

        randomPieces(board, new Bishop(this, true), stage);
        randomPieces(board, new Bishop(this, false), stage);

        randomQueen(board, stage, true);
        randomQueen(board, stage, false);

        return translateBoardToFEN(board);
    }

    public int[] getRandomNullIndex(Piece[][] board) {
        Random random = new Random();
        ArrayList<int[]> indices = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board[i].length; j++) {
                if (board[i][j] == null) {
                    indices.add(new int[] {i, j});
                }
            }
        }
        int randomIndex = random.nextInt(indices.size());
        return indices.get(randomIndex);
    }

    public void randomKing(Piece[][] board, boolean white) {
        placeRandomPiece(board, new King(this, white), 1);
    }

    public void randomPieces(Piece[][] board, Piece piece, GameStage stage) {
        Random random = new Random();
        int pieceCount = switch (stage) {
            case END_GAME -> random.nextInt(2);
            case MID_GAME -> random.nextInt(1, 2);
            case EARLY_GAME -> 2;
        };
        placeRandomPiece(board, piece, pieceCount);
    }

    public void randomQueen(Piece[][] board, GameStage stage, boolean white) {
        Random random = new Random();
        int queenCount = switch (stage) {
            case END_GAME -> random.nextInt(3);
            case MID_GAME, EARLY_GAME -> random.nextInt(2);
        };
        placeRandomPiece(board, new Queen(this, white), queenCount);
    }

    private void placeRandomPiece(Piece[][] board, Piece piece, int pieceCount) {
        for (int i = 0; i < pieceCount; i++) {
            int[] randomTile = getRandomNullIndex(board);
            int rRank = randomTile[0];
            int rFile = randomTile[1];
            if (board[rRank][rFile] == null) {
                board[rRank][rFile] = piece;
            }
        }
    }

    public void randomPawns(Piece[][] board, GameStage stage, boolean white) {
        Random random = new Random();
        int pawnCount = switch (stage) {
            case END_GAME -> random.nextInt(3);
            case MID_GAME -> random.nextInt(3, 6);
            case EARLY_GAME -> random.nextInt(7, 9);
        };
        for (int i = 0; i < pawnCount; i++) {
            int[] randomTile = getRandomNullIndex(board);
            int rRank = randomTile[0];
            int rFile = randomTile[1];
            if (board[rRank][rFile] == null) {
                board[rRank][rFile] = new Pawn(this, white);
            }
        }
    }

    public static boolean isValidFENRow(String FENRow, int boardSize) {
        int count = 0;
        String[] chars = FENRow.split("");
        for (int i = 0; i < chars.length; i++) {
            String s = chars[i];
            if (s.equals("(")) {
                String number = "";
                for (int j = 1; j < chars.length; j++) {
                    if (Character.isDigit(chars[j + i].toCharArray()[0])) {
                        number += Integer.parseInt(chars[j + i]);
                    } else {
                        if (chars[j + i].equals(")")) {
                            i += j;
                        }
                        break;
                    }
                }
                count += Integer.parseInt(number);
            } else {
                if (s.matches("[RNBQKPACESHIMYUrnbqkpaceshimyu]")) {
                    count++;
                } else if (s.matches("\\d")) {
                    count += Integer.parseInt(s);
                } else {
                    return false;
                }
            }
        }
        return count == boardSize;
    }

    public static String fixFENRow(String FENRow, int boardSize) {
        StringBuilder result = new StringBuilder();
        int count = 0;
        String[] chars = FENRow.split("");
        for (int i = 0; i < chars.length; i++) {
            String s = chars[i];
            if (s.equals(")")) {
                continue;
            }
            if (s.equals("(")) {
                String number = "";
                for (int j = 1; j < chars.length; j++) {
                    if (Character.isDigit(chars[j + i].toCharArray()[0])) {
                        number += Integer.parseInt(chars[j + i]);
                    } else {
                        if (chars[j + i].equals(")")) {
                            i += j;
                        }
                        break;
                    }
                }
                count += Integer.parseInt(number);
            } else {
                if (s.matches("[RNBQKPACESHIMYUrnbqkpaceshimyu]")) {
                    if (count + 1 > boardSize) {
                        break;
                    } else {
                        result.append(s);
                        count++;
                    }
                } else if (s.matches("\\d")) {
                    if (count + Integer.parseInt(s) > boardSize ) {
                        break;
                    } else {
                        result.append(s);
                        count += Integer.parseInt(s);
                    }
                }
            }
        }
        if (count < boardSize) {
            if (boardSize - count >= 10) {
                result.append("(").append(boardSize - count).append(")");
            } else {
                result.append(boardSize - count);
            }
        }

        String toTruncate = result.toString();

        List<String> truncatedResult = new ArrayList<>();
        boolean lastWasDigit = false;
        String[] cs = toTruncate.split("");
        for (int i = 0; i < cs.length; i++) {
            String s = cs[i];
            if (s.equals(")")) {
                truncatedResult.add(s);
            }
            if (s.equals("(")) {
                truncatedResult.add(s);
                for (int j = 1; j < cs.length; j++) {
                    truncatedResult.add(cs[i + j]);
                    if (cs[i + j].equals(")")) {
                        i += j;
                        break;
                    }
                }
            } else {
                if (Character.isDigit(s.charAt(0))) {
                    if (lastWasDigit) {
                        truncatedResult.set(truncatedResult.size() - 1, String.valueOf(Integer.parseInt(truncatedResult.get(truncatedResult.size() - 1)) + Integer.parseInt(s)));
                    } else {
                        truncatedResult.add(s);
                    }
                    lastWasDigit = true;
                }
                if (s.matches("[A-Za-z]")) {
                    truncatedResult.add(s);
                    lastWasDigit = false;
                }
            }
        }

        return squish(truncatedResult);
    }

    public String squish(String[] strings) {
        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            builder.append(string);
        }
        return builder.toString();
    }

    public static String squish(List<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            builder.append(string);
        }
        return builder.toString();
    }

    public static String fenBoard(String[] board) {
        StringJoiner joiner = new StringJoiner("/");
        for (String s : board) {
            joiner.add(s);
        }
        return joiner.toString();
    }

    public String fenBoard(List<String> board) {
        StringJoiner joiner = new StringJoiner("/");
        for (String s : board) {
            joiner.add(s);
        }
        return joiner.toString();
    }

    public static String fixFENBoard(String FENBoard, int boardSize) {
        if (!(FENBoard.contains("K") && FENBoard.contains("k"))) {
            return null;
        }
        if (FENBoard.chars().filter(c -> c == 'K').count() > 1) {
            return null;
        }
        if (FENBoard.chars().filter(c -> c == 'k').count() > 1) {
            return null;
        }

        FENBoard = FENBoard.replaceAll("/+", "/");
        if (FENBoard.startsWith("/")) {
            FENBoard = FENBoard.substring(1);
        }
        if (FENBoard.endsWith("/")) {
            FENBoard = FENBoard.substring(0, FENBoard.length() - 1);
        }

        String[] rows = FENBoard.split("/");

        for (int i = 0; i < rows.length; i++) {
            if (rows[i] == null || rows[i].equalsIgnoreCase("")) {
                if (boardSize >= 10) {
                    rows[i] = "(" + boardSize + ")";
                } else {
                    rows[i] = String.valueOf(boardSize);
                }
            }
            if (!isValidFENRow(rows[i], boardSize)) {
                rows[i] = fixFENRow(rows[i], boardSize);
            }
        }

        FENBoard = fenBoard(rows);

        String[] validRows = FENBoard.split("/");
        if (validRows.length < boardSize) {
            for (int i = 0; i < boardSize - validRows.length; i++) {
                if (boardSize >= 10) {
                    FENBoard += "/(" + boardSize + ")";
                } else {
                    FENBoard += "/" + boardSize;
                }
            }
            if (Chess.shouldRelocateBackline) {
                FENBoard = relocateFENBackLine(FENBoard, validRows, boardSize);
            }
        }
        if (validRows.length > boardSize) {
            FENBoard = relocateFENBackLine(FENBoard, validRows, boardSize);
        }

        return FENBoard;
    }

    private static String relocateFENBackLine(String FENBoard, String[] validRows, int boardSize) {
        String whiteBackLine = null;
        String blackBackLine = null;
        boolean shouldRelocate = true;
        for (String validRow : validRows) {
            if (validRow.contains("k") && validRow.contains("K")) {
                shouldRelocate = false;
                break;
            } else {
                if (validRow.contains("K")) {
                    whiteBackLine = validRow;
                    FENBoard = FENBoard.replace(whiteBackLine, "");
                } else if (validRow.contains("k")) {
                    blackBackLine = validRow;
                    FENBoard = FENBoard.replace(blackBackLine, "");
                }
            }
        }

        FENBoard = FENBoard.replaceAll("/+", "/");
        String[] trimmedFenBoard = new String[boardSize];
        if (shouldRelocate) {
            FENBoard = "###/" + FENBoard + "/###";
            FENBoard = FENBoard.replaceAll("/+", "/");
            String[] withoutBackline = FENBoard.split("/");
            trimmedFenBoard[0] = blackBackLine;
            for (int i = 1; i < boardSize - 1; i++) {
                trimmedFenBoard[i] = withoutBackline[i];
            }
            trimmedFenBoard[boardSize - 1] = whiteBackLine;

            FENBoard = fenBoard(trimmedFenBoard);
        } else {
            FENBoard = FENBoard.replaceAll("/+", "/");
            FENBoard = fenBoard(FENBoard.split("/"));
        }
        return FENBoard;
    }

    public static String fixFENString(String FEN) {
        String[] sections = FEN.split(" ");
        int sectionLength = sections.length;

        StringJoiner result = new StringJoiner(" ");

        String board = sectionLength > 0 ? sections[0] : "";
        board = fixFENBoard(board, boardSize);
        if (board == null) {
            return null;
        }
        result.add(board);

        String turn = sectionLength > 1 ? sections[1] : "";
        if (!turn.equalsIgnoreCase("b") && !turn.equalsIgnoreCase("w")) {
            turn = "w";
        }
        result.add(turn);

        String castles = sectionLength > 2 ? sections[2] : "";
        if (!castles.equalsIgnoreCase("-") && !(castles.contains("K") || castles.contains("k") || castles.contains("Q") || castles.contains("q"))) {
            castles = "-";
        }
        result.add(castles);

        String enPassantSquare = sectionLength > 3 ? sections[3] : "";
        if (!BoardCoordinate.isValidTile(boardSize, enPassantSquare) && !enPassantSquare.equalsIgnoreCase("-")) {
            enPassantSquare = "-";
        }
        result.add(enPassantSquare);

        String halfMoveClock = sectionLength > 4 ? sections[4] : "";
        if (!halfMoveClock.matches("-?\\d+(\\.\\d+)?")) {
            halfMoveClock = "0";
        }

        String fullMoveCount = sectionLength > 5 ? sections[5] : "";
        if (!fullMoveCount.matches("-?\\d+(\\.\\d+)?")) {
            fullMoveCount = "1";
        }

        if (!(Integer.parseInt(halfMoveClock) / 2 == Integer.parseInt(fullMoveCount) - 1)) {
            halfMoveClock = "0";
            fullMoveCount = "1";
        }

        result.add(halfMoveClock);
        result.add(fullMoveCount);

        return result.toString();
    }

    public void addImportHistory() {
        history.add(new Move("[ Imported"));
        history.add(new Move("FEN String ]"));
    }

    public static StringBuilder translateBoard(Piece[][] board, int boardSize) {
        StringBuilder fen = new StringBuilder();
        for (int rank = 0; rank < boardSize; rank++) {
            int empty = 0;
            StringBuilder rankFen = new StringBuilder();
            for (int file = 0; file < boardSize; file++) {
                if (board[(boardSize - 1) - rank][file] == null) {
                    empty++;
                } else {
                    Piece piece = board[(boardSize - 1) - rank][file];
                    if (empty != 0) {
                        if (boardSize >= 10 && empty >= 10) {
                            rankFen.append("(").append(empty).append(")");
                        } else {
                            rankFen.append(empty);
                        }
                    }
                    if (piece instanceof SpecialPiece piece1) {
                        rankFen.append(piece.isWhite() ? String.valueOf(piece1.fenChar()).toUpperCase() : String.valueOf(piece1.fenChar()).toLowerCase());
                    } else {
                        rankFen.append(piece.isWhite() ? piece.getAbbr().toUpperCase() : piece.getAbbr().toLowerCase());
                    }
                    empty = 0;
                }
            }
            if (empty != 0) {
                if (boardSize >= 10 && empty >= 10) {
                    rankFen.append("(").append(empty).append(")");
                } else {
                    rankFen.append(empty);
                }
            }
            fen.append(rankFen);
            if (!(rank == board.length - 1)) {
                fen.append("/");
            }
        }
        return fen;
    }

    public String translateBoardToFEN(Piece[][] board) {
        StringBuilder fen = translateBoard(board, boardSize);

        fen.append(" ");
        fen.append(whiteTurn ? "w" : "b");
        fen.append(" ");
        fen.append(whiteCanCastleKingside ? "K" : "");
        fen.append(whiteCanCastleQueenside ? "Q" : "");
        fen.append(blackCanCastleKingside ? "k" : "");
        fen.append(blackCanCastleQueenside ? "q" : "");
        if ((!whiteCanCastleKingside) && (!whiteCanCastleQueenside) && (!blackCanCastleKingside) && (!blackCanCastleQueenside)) {
            fen.append("-");
        }
        fen.append(" ");
        fen.append(getEnpassantString());
        fen.append(" ");
        fen.append(halfMoveClock);
        fen.append(" ");
        fen.append(fullMoveCount);
        return fen.toString();
    }

    public BoardCoordinate getEnpassantTargetSquare() {
        if (epSQ != null) {
            BoardCoordinate temp = epSQ;
            epSQ = null;
            return temp;
        } else {
            if (history != null && !history.isEmpty()) {
                Move lastMove = history.get(history.size() - 1);
                if (lastMove.getPiece() instanceof Pawn pawn) {
                    if (Math.abs(lastMove.getTo().row() - lastMove.getFrom().row()) == 2) {
                        if (pawn.isWhite()) {
                            return new BoardCoordinate(lastMove.getTo().row() - 1, lastMove.getTo().col());
                        } else {
                            return new BoardCoordinate(lastMove.getTo().row() + 1, lastMove.getTo().col());
                        }
                    }
                }
            }
        }

        return null;
    }

    public BoardCoordinate getEnpassantTakeSquare() {
        if (epSQ != null) {
            BoardCoordinate temp = new BoardCoordinate(epSQ.row() + (whiteTurn ? 1 : -1), epSQ.col());
            epSQ = null;
            return temp;
        } else {
            if (history != null && !history.isEmpty()) {
                Move lastMove = history.get(history.size() - 1);
                if (lastMove.getPiece() instanceof Pawn) {
                    if (Math.abs(lastMove.getTo().row() - lastMove.getFrom().row()) == 2) {
                        return new BoardCoordinate(lastMove.getTo().row(), lastMove.getTo().col());
                    }
                }
            }
        }

        return null;
    }

    public String getEnpassantString() {
        BoardCoordinate tile = getEnpassantTargetSquare();
        return tile == null ? "-" : tile.toNotation();
    }

    public List<BoardCoordinate> getAllCastleSquares(boolean white, Move.CastleType type) {
        List<BoardCoordinate> castleSquares = rookCastleSquares(white, type);
        if (currentLayout == Chess.BoardLayout.DEFAULT) {
            castleSquares.addAll(defaultCastleSquares(white, type));
        }
        return castleSquares;
    }

    public List<BoardCoordinate> defaultCastleSquares(boolean white, Move.CastleType type) {
        List<BoardCoordinate> coordinates = rookCastleSquares(white, type);
        if (currentLayout == Chess.BoardLayout.DEFAULT) {
            if (white) {
                if (whiteCanCastleKingside) {
                    coordinates.add(new BoardCoordinate(0, decBoardSize - 1));
                }
                if (whiteCanCastleQueenside) {
                    coordinates.add(new BoardCoordinate(0, 2));
                }
            } else {
                if (blackCanCastleKingside) {
                    coordinates.add(new BoardCoordinate(decBoardSize, decBoardSize - 1));
                }
                if (blackCanCastleQueenside) {
                    coordinates.add(new BoardCoordinate(decBoardSize, 2));
                }
            }
        }
        return coordinates;
    }

    public List<BoardCoordinate> rookCastleSquares(boolean white, Move.CastleType type) {
        List<BoardCoordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (gamePieces[i][j] != null && gamePieces[i][j] instanceof Rook rook) {
                    if (rook.isWhite() == white && rook.getType() == type)  {
                        boolean shouldAdd = false;
                        if (white) {
                            switch (type) {
                                case KINGSIDE -> {
                                    if (whiteCanCastleKingside) {
                                        shouldAdd = true;
                                    }
                                }
                                case QUEENSIDE -> {
                                    if (whiteCanCastleQueenside) {
                                        shouldAdd = true;
                                    }
                                }
                            }
                        } else {
                            switch (type) {
                                case KINGSIDE -> {
                                    if (blackCanCastleKingside) {
                                        shouldAdd = true;
                                    }
                                }
                                case QUEENSIDE -> {
                                    if (blackCanCastleQueenside) {
                                        shouldAdd = true;
                                    }
                                }
                            }
                        }
                        if (shouldAdd) {
                            coordinates.add(new BoardCoordinate(i, j));
                        }
                    }
                }
            }
        }
        return coordinates;
    }

    public BoardCoordinate newKingFile(boolean white, Move.CastleType type) {
        return new BoardCoordinate(white ? 0 : decBoardSize, type == Move.CastleType.KINGSIDE ? decBoardSize - 1 : 2);
    }

    public BoardCoordinate newRookFile(boolean white, Move.CastleType type) {
        return new BoardCoordinate(white ? 0 : decBoardSize, type == Move.CastleType.KINGSIDE ? decBoardSize - 2 : 3);
    }

    public void displayPieces() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                JPanel panel = (JPanel) getComponent((row * boardSize) + col);
                int r1 = view == BoardView.WHITE ? decBoardSize - row : row;
                int c1 = view == BoardView.WHITE ? col : decBoardSize - col;

                if (panel.getComponents().length > 0) {
                    panel.removeAll();
                }
                if (gamePieces[r1][c1] != null) {
                    panel.add(new JLabel(new ImageIcon(gamePieces[r1][c1].getIcon())));
                }
                panel.validate();
            }
        }
    }

    public void setupDefaultBoard(Piece[][] board, boolean white) {
        int backLine = white ? 0 : decBoardSize;

        if (boardSize >= 8) {
            int startingPoint = (boardSize / 2) - 4;
            for (int i = startingPoint; i < startingPoint + 8; i++) {
                board[white ? 1 : decBoardSize - 1][i] = new Pawn(this, white);
            }

            board[backLine][startingPoint] = new Rook(this, white, new BoardCoordinate(backLine, startingPoint));
            board[backLine][startingPoint + 7] = new Rook(this, white, new BoardCoordinate(backLine, startingPoint + 7));

            board[backLine][startingPoint + 1] = new Knight(this, white);
            board[backLine][startingPoint + 6] = new Knight(this, white);

            board[backLine][startingPoint + 2] = new Bishop(this, white);
            board[backLine][startingPoint + 5] = new Bishop(this, white);

            board[backLine][startingPoint + 3] = new Queen(this, white);
            board[backLine][startingPoint + 4] = new King(this, white);
        } else {
            for (int i = 0; i < boardSize; i++) {
                board[white ? 1 : decBoardSize - 1][i] = new Pawn(this, white);
            }
            switch (boardSize) {
                case 4 -> {
                    board[backLine][0] = new Rook(this, white, new BoardCoordinate(backLine, 0));
                    board[backLine][1] = new Queen(this, white);
                    board[backLine][2] = new King(this, white);
                    board[backLine][3] = new Knight(this, white);
                }
                case 5 -> {
                    board[backLine][0] = new Rook(this, white, new BoardCoordinate(backLine, 0));
                    board[backLine][1] = new Queen(this, white);
                    board[backLine][2] = new King(this, white);
                    board[backLine][3] = new Knight(this, white);
                    board[backLine][4] = new Rook(this, white, new BoardCoordinate(backLine, 4));
                }
                case 6 -> {
                    board[backLine][0] = new Rook(this, white, new BoardCoordinate(backLine, 0));
                    board[backLine][1] = new Bishop(this, white);
                    board[backLine][2] = new Queen(this, white);
                    board[backLine][3] = new King(this, white);
                    board[backLine][4] = new Knight(this, white);
                    board[backLine][5] = new Rook(this, white, new BoardCoordinate(backLine, 5));
                }
                case 7 -> {
                    board[backLine][0] = new Rook(this, white, new BoardCoordinate(backLine, 0));
                    board[backLine][1] = new Knight(this, white);
                    board[backLine][2] = new Bishop(this, white);
                    board[backLine][3] = new Queen(this, white);
                    board[backLine][4] = new King(this, white);
                    board[backLine][5] = new Knight(this, white);
                    board[backLine][6] = new Rook(this, white, new BoardCoordinate(backLine, 6));
                }
            }
        }
    }

    public Piece fromChar(char piece, boolean white, int index) {
        return switch (piece) {
            case 'R', 'X' -> new Rook(this, white, new BoardCoordinate(white ? 0 : decBoardSize, index));
            case 'N' -> new Knight(this, white);
            case 'B' -> new Bishop(this, white);
            case 'Q' -> new Queen(this, white);
            case 'K' -> new King(this, white);
            default -> throw new IllegalStateException("Unexpected value found when generating fischer-random: " + piece);
        };
    }

    public void setupPieces(Chess.BoardLayout layout) {
        switch (layout) {
            case DEFAULT -> {
                setupDefaultBoard(gamePieces, false);
                setupDefaultBoard(gamePieces, true);
            }
            case CHESS960 -> {
                if (boardSize != 8) {

                } else {
                    // TODO
                }
                for (int i = 0; i < 8; i++) {
                    gamePieces[1][i] = new Pawn(this, true);
                    gamePieces[6][i] = new Pawn(this, false);
                }

                char[] board = new char[8];

                for (int i = 0; i < 2; i++) {
                    int r = (int) (Math.random() * 4) * 2;

                    if (i == 1) {
                        r++;
                    }

                    board[r] = 'B';
                }

                char[] queenKnights = {'Q', 'N', 'N'};
                for (int i = 0; i < queenKnights.length; i++) {
                    int index = (int) (Math.random() * (6 - i));

                    while (board[index] != 0) {
                        index++;
                    }

                    board[index] = queenKnights[i];
                }

                char[] kingRooks = {'R', 'K', 'X'};
                int index = 0;
                for (char kingRook : kingRooks) {
                    while (board[index] != 0) {
                        index++;
                    }

                    board[index] = kingRook;
                    index++;
                }

                for (int i = 0; i < board.length; i++) {
                    gamePieces[0][i] = fromChar(board[i], true, i);
                    gamePieces[7][i] = fromChar(board[i], false, i);
                }
            }
        }
    }

    public void showHangingPieces() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (gamePieces[i][j] != null) {
                    if (isHanging(gamePieces, new BoardCoordinate(i, j))) {
                        if (gamePieces[i][j].isWhite() == playAsWhite) {
                            highlightedSquares[i][j] = Move.MoveHighlights.HANGING_BAD;
                            highlightIconAccompaniment[i][j] = Move.MoveHighlights.HANGING_BAD;
                        } else if (gamePieces[i][j].isWhite() != playAsWhite) {
                            highlightedSquares[i][j] = Move.MoveHighlights.HANGING_GOOD;
                            highlightIconAccompaniment[i][j] = Move.MoveHighlights.HANGING_GOOD;
                        }
                    }
                }
            }
        }
    }

    public void showTrades(boolean points) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (gamePieces[i][j] != null) {
                    BoardCoordinate tile = new BoardCoordinate(i, j);
                    TradeType type = tradeOff(gamePieces, tile, whiteTurn, points);
                    if (type != null) {
                        switch (type) {
                            case BLACK -> {
                                Move.MoveHighlights highlight;
                                if (!playAsWhite) {
                                    highlight = points ? Move.MoveHighlights.TRADE_POINT_GOOD : Move.MoveHighlights.TRADE_PIECE_GOOD;
                                } else {
                                    highlight = points ? Move.MoveHighlights.TRADE_POINT_BAD : Move.MoveHighlights.TRADE_PIECE_BAD;
                                }
                                highlightedSquares[i][j] = highlight;
                                highlightIconAccompaniment[i][j] = highlight;
                            }
                            case EQUAL -> {
                                Move.MoveHighlights highlight = points ? Move.MoveHighlights.TRADE_POINT_EQUAL : Move.MoveHighlights.TRADE_PIECE_EQUAL;
                                highlightedSquares[i][j] = highlight;
                                highlightIconAccompaniment[i][j] = highlight;
                            }
                            case WHITE -> {
                                Move.MoveHighlights highlight;
                                if (playAsWhite) {
                                    highlight = points ? Move.MoveHighlights.TRADE_POINT_GOOD : Move.MoveHighlights.TRADE_PIECE_GOOD;
                                } else {
                                    highlight = points ? Move.MoveHighlights.TRADE_POINT_BAD : Move.MoveHighlights.TRADE_PIECE_BAD;
                                }
                                highlightedSquares[i][j] = highlight;
                                highlightIconAccompaniment[i][j] = highlight;
                            }
                        }
                    }
                }
            }
        }
    }

    public void removeHighlights(Move.MoveHighlights highlights) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (highlightedSquares[i][j] == highlights) {
                    highlightedSquares[i][j] = null;
                }
                if (highlightIconAccompaniment[i][j] == highlights) {
                    highlightIconAccompaniment[i][j] = null;
                }
            }
        }
    }

    public MouseListener highlightListener() {
        return new MouseListener() {
            int x0h;
            int y0h;

            boolean shouldDeselect = false;

            @Override
            public void mousePressed(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    x0h = e.getX();
                    y0h = e.getY();

                    deselect();
                }
                if (SwingUtilities.isLeftMouseButton(e)) {
                    removeHighlights(Move.MoveHighlights.HIGHLIGHT);
                    removeHighlights(Move.MoveHighlights.GREEN_HIGHLIGHT);
                    removeHighlights(Move.MoveHighlights.ORANGE_HIGHLIGHT);
                    removeHighlights(Move.MoveHighlights.BLUE_HIGHLIGHT);

                    int f1 = e.getX() / pieceSize;
                    int r1 = e.getY() / pieceSize;

                    int r2 = view == BoardView.WHITE ? decBoardSize - r1 : r1;
                    int c2 = view == BoardView.WHITE ? f1 : decBoardSize - f1;

                    BoardCoordinate coordinate = new BoardCoordinate(r1, f1);

                    int row = coordinate.row();
                    int col = coordinate.col();

                    switch (view) {
                        case BLACK -> col = decBoardSize - col;
                        case WHITE -> row = decBoardSize - row;
                    }

                    if (gamePieces[r2][c2] != null) {
                        BoardCoordinate clicked = new BoardCoordinate(row, col);
                        updateSelectedPiece(row, col, clicked);
                    }
                    repaint();
                    displayPieces();
                }
            }

            private void updateSelectedPiece(int row, int col, BoardCoordinate clicked) {
                List<Move> moves = availableMoves(gamePieces, selected, tileSelected, false);
                if (tileSelected == null) {
                    tileSelected = clicked;
                    highlightedSquares[row][col] = Move.MoveHighlights.SELECTED;

                    shouldDeselect = false;
                } else {
                    if (!tileSelected.equals(clicked)) {
                        Piece selectedPiece = gamePieces[tileSelected.row()][tileSelected.col()];
                        Piece clickedPiece = gamePieces[clicked.row()][clicked.col()];
                        if (selectedPiece.isWhite() == playAsWhite) {
                            if (clickedPiece.isWhite() != playAsWhite) {
                                if (whiteTurn == playAsWhite && pieceInteraction.tileSelected != null) {
                                    if (pieceInteraction.tileSelected.equals(tileSelected)) {
                                        for (Move move : moves) {
                                            if (move.getTo().equals(clicked)) {
                                                return;
                                            }
                                        }
                                    }
                                }
                            }
                        }

                        highlightedSquares[tileSelected.row()][tileSelected.col()] = null;
                        tileSelected = clicked;
                        highlightedSquares[row][col] = Move.MoveHighlights.SELECTED;
                        shouldDeselect = false;
                    } else {
                        shouldDeselect = true;
                    }
                }
                selected = gamePieces[tileSelected.row()][tileSelected.col()];
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (SwingUtilities.isRightMouseButton(e)) {
                    int f0 = e.getX() / pieceSize;
                    int r0 = e.getY() / pieceSize;

                    int f1 = x0h / pieceSize;
                    int r1 = y0h / pieceSize;

                    BoardCoordinate coordinate = new BoardCoordinate(r1, f1);
                    if (f0 == f1 && r0 == r1) {
                        if (e.isAltDown()) {
                            highlight(coordinate.row(), coordinate.col(), Move.MoveHighlights.BLUE_HIGHLIGHT, false, false);
                        } else if (e.isControlDown()) {
                            highlight(coordinate.row(), coordinate.col(), Move.MoveHighlights.ORANGE_HIGHLIGHT, false, false);
                        } else if (e.isShiftDown()) {
                            highlight(coordinate.row(), coordinate.col(), Move.MoveHighlights.GREEN_HIGHLIGHT, false, false);
                        } else {
                            highlight(coordinate.row(), coordinate.col(), Move.MoveHighlights.HIGHLIGHT, false, false);
                        }

                        x0h = 0;
                        y0h = 0;
                        repaint();
                        displayPieces();
                    }
                }
                if (SwingUtilities.isLeftMouseButton(e)) {
                    if (shouldDeselect) {
                        int f1 = e.getX() / pieceSize;
                        int r1 = e.getY() / pieceSize;

                        BoardCoordinate coordinate = new BoardCoordinate(r1, f1);
                        int row = coordinate.row();
                        int col = coordinate.col();

                        switch (view) {
                            case BLACK -> col = decBoardSize - col;
                            case WHITE -> row = decBoardSize - row;
                        }

                        BoardCoordinate clicked = new BoardCoordinate(row, col);

                        if (tileSelected != null && tileSelected.equals(clicked)) {
                            tileSelected = null;
                            highlightedSquares[row][col] = null;
                            selected = null;
                        }
                        shouldDeselect = false;
                    }

                    repaint();
                    displayPieces();
                }
            }

            @Override
            public void mouseEntered(MouseEvent e) {
            }

            @Override
            public void mouseExited(MouseEvent e) {
            }

            @Override
            public void mouseClicked(MouseEvent e) {
            }
        };
    }

    public void moveHighlight(BoardCoordinate from, BoardCoordinate to) {
        moveHighlights = new Move.MoveHighlights[boardSize][boardSize];
        highlightIconAccompaniment = new Move.MoveHighlights[boardSize][boardSize];

        int r2 = view == BoardView.WHITE ? decBoardSize - from.row() : from.row();
        int c2 = view == BoardView.WHITE ? from.col() : decBoardSize - from.col();

        int r3 = view == BoardView.WHITE ? decBoardSize - to.row() : to.row();
        int c3 = view == BoardView.WHITE ? to.col() : decBoardSize - to.col();

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (highlightedSquares[i][j] == Move.MoveHighlights.MOVE || highlightedSquares[i][j] == Move.MoveHighlights.SELECTED) {
                    highlightedSquares[i][j] = null;
                }
            }
        }

        highlight(r2, c2, Move.MoveHighlights.MOVE, true, true);
        highlight(r3, c3, Move.MoveHighlights.MOVE, false, true);
    }

    public void highlight(int row, int col, Move.MoveHighlights type, boolean from, boolean isMoveHighlight) {
        switch (view) {
            case BLACK -> col = decBoardSize - col;
            case WHITE -> row = decBoardSize - row;
        }

        if (type == Move.MoveHighlights.SELECTED) {
            BoardCoordinate clicked = new BoardCoordinate(row, col);
            if (tileSelected == null) {
                tileSelected = clicked;
                highlightedSquares[row][col] = Move.MoveHighlights.SELECTED;
            } else {
                if (tileSelected.equals(clicked)) {
                    tileSelected = null;
                    highlightedSquares[row][col] = null;
                } else {
                    highlightedSquares[tileSelected.row()][tileSelected.col()] = null;
                    tileSelected = clicked;
                    highlightedSquares[row][col] = Move.MoveHighlights.SELECTED;
                }
            }
            selected = tileSelected != null ? gamePieces[tileSelected.row()][tileSelected.col()] : null;
        } else if (isMoveHighlight) {
            moveHighlights[row][col] = type;
            if (!from) {
                highlightIconAccompaniment[row][col] = type;
            }
        } else {
            if (type == highlightedSquares[row][col]) {
                highlightedSquares[row][col] = null;
            } else {
                highlightedSquares[row][col] = type;
            }
        }
    }

    public enum Colours {
        EIGHT_BIT("8-Bit", new ColorScheme(new Color(243,243,244), new Color(106,155,65), new Color(255, 255, 0, 127), new Color(255, 255, 0)), false),
        BASES("Bases", new ColorScheme(new Color(239, 204, 161), new Color(194, 107, 56), new Color(245, 204, 42, 127), new Color(245, 204, 42)), true),
        BLUE("Blue", new ColorScheme(new Color(236,236,215), new Color(77,109,146), new Color(0, 165, 255, 127), new Color(0, 165, 255)), true),
        BROWN("Brown", new ColorScheme(new Color(237,214,176), new Color(184,135,98), new Color(255, 255, 0, 127), new Color(255, 255, 0)), false),
        BUBBLEGUM("Bubblegum", new ColorScheme(new Color(255,255,255), new Color(252,216,221), new Color(222, 93, 111, 127), new Color(222, 93, 111)), false),
        BURLED_WOOD("Burled Wood", new ColorScheme(new Color(217, 176, 136), new Color(137, 81, 50), new Color(238, 144, 22, 127), new Color(238, 144, 22)), true),
        DARK_WOOD("Dark Wood", new ColorScheme(new Color(231, 205, 178), new Color(141, 103, 94), new Color(204, 145, 34, 127), new Color(204, 145, 34)), true),
        DASH("Dash", new ColorScheme(new Color(189, 146, 87), new Color(107, 58, 39), new Color(236, 167, 34, 127), new Color(236, 167, 34)), true),
        GLASS("Glass", new ColorScheme(new Color(102, 113, 136), new Color(40, 47, 63), new Color(91, 145, 179, 127), new Color(91, 145, 179)), true),
        GRAFFITI("Graffiti", new ColorScheme(new Color(174, 174, 174), new Color(185, 111, 24), new Color(243, 144, 17, 127), new Color(243, 144, 17)), true),
        GREEN("Green", new ColorScheme(new Color(238, 238, 210), new Color(118, 150, 86), new Color(255, 255, 0, 127), new Color(255, 255, 0)), false),
        ICY_SEA("Icy Sea", new ColorScheme(new Color(197, 213, 220), new Color(122, 157, 178), new Color(94, 215, 241, 127), new Color(94, 215, 241)), false),
        LIGHT("Light", new ColorScheme(new Color(220,220,220), new Color(171,171,171), new Color(164, 184, 196, 127), new Color(164, 184, 196)), false),
        LOLZ("Lolz", new ColorScheme(new Color(224, 233, 233), new Color(144, 152, 152), new Color(163, 190, 205, 127), new Color(163, 190, 205)), true),
        MARBLE("Marble", new ColorScheme(new Color(199, 189, 170), new Color(112, 107, 102), new Color(240, 219, 134, 127), new Color(240, 219, 134)), true),
        METAL("Metal", new ColorScheme(new Color(201, 201, 201), new Color(110, 110, 110), new Color(163, 190, 205, 127), new Color(163, 190, 205)), true),
        NEON("Neon", new ColorScheme(new Color(185, 185, 185), new Color(99, 99, 99), new Color(109, 144, 166, 127), new Color(109, 144, 166)), true),
        NEWSPAPER("Newspaper", new ColorScheme(new Color(90, 89, 86), new Color(90, 89, 86), new Color(153, 151, 110, 127), new Color(153, 151, 110)), true),
        ORANGE("Orange", new ColorScheme(new Color(252,228,178), new Color(208,139,24), new Color(255, 255, 0, 127), new Color(255, 255, 0)), false),
        OVERLAY("Overlay", new ColorScheme(new Color(72, 120, 160), new Color(120, 158, 189), new Color(13, 154, 207, 127), new Color(13, 154, 207)), true),
        PARCHMENT("Parchment", new ColorScheme(new Color(240, 217, 181), new Color(181, 136, 99), new Color(216, 204, 102, 127), new Color(216, 204, 102)), true),
        PURPLE("Purple", new ColorScheme(new Color(239,239,239), new Color(136,119,183), new Color(125, 172, 201, 127), new Color(125, 172, 201)), false),
        RED("Red", new ColorScheme(new Color(240,216,191), new Color(186,85,70), new Color(248, 248, 147, 127), new Color(248, 248, 147)), false),
        SAND("Sand", new ColorScheme(new Color(229, 211, 196), new Color(184, 165, 144), new Color(226, 188, 135, 127), new Color(226, 188, 135)), true),
        SKY("Sky", new ColorScheme(new Color(239,239,239), new Color(194,215,226), new Color(101, 218, 247, 127), new Color(101, 218, 247)), false),
        STONE("Stone", new ColorScheme(new Color(200, 195, 189), new Color(102, 100, 99), new Color(54, 82, 95, 127), new Color(54, 82, 95)), true),
        TAN("Tan", new ColorScheme(new Color(237,201,162), new Color(211,163,106), new Color(247, 216, 74, 127), new Color(247, 216, 74)), false),
        TOURNAMENT("Tournament", new ColorScheme(new Color(235, 236, 232), new Color(49, 101, 73), new Color(164, 194, 91, 127), new Color(164, 194, 91)), true),
        TRANSLUCENT("Translucent", new ColorScheme(new Color(40, 47, 63), new Color(102, 113, 136), new Color(91, 154, 179, 127), new Color(91, 145, 179)), true),
        WALNUT("Walnut", new ColorScheme(new Color(192, 166, 132), new Color(131, 95, 66), new Color(209, 165, 45, 127), new Color(209, 165, 45)), true);

        private final ColorScheme scheme;
        private final String name;

        Colours(String name, ColorScheme scheme, boolean online) {
            this.scheme = scheme;
            this.name = name;
        }

        public ColorScheme getScheme() {
            return scheme;
        }

        public String getName() {
            return name;
        }

        public String getLinkString() {
            return name.replace(" ", "_").replace("-", "_").toLowerCase();
        }
    }

    public enum PieceDesign {
        EIGHT_BIT("8-Bit"),
        ALPHA("Alpha"),
        BASES("Bases"),
        BLINDFOLD("Blindfold"),
        BOOK("Book"),
        BUBBLEGUM("Bubblegum"),
        CASES("Cases"),
        CLASSIC("Classic"),
        CLUB("Club"),
        CONDAL("Condal"),
        DASH("Dash"),
        GAME_ROOM("Game Room"),
        GLASS("Glass"),
        GOTHIC("Gothic"),
        GRAFFITI("Graffiti"),
        ICY_SEA("Icy Sea"),
        LIGHT("Light"),
        LOLZ("Lolz"),
        MARBLE("Marble"),
        MAYA("Maya"),
        METAL("Metal"),
        MODERN("Modern"),
        NATURE("Nature"),
        NEON("Neon"),
        NEO("Neo"),
        NEO_WOOD("Neo Wood"),
        NEWSPAPER("Newspaper"),
        OCEAN("Ocean"),
        SKY("Sky"),
        SPACE("Space"),
        TIGERS("Tigers"),
        TOURNAMENT("Tournament"),
        VINTAGE("Vintage"),
        WOOD("Wood"),
        WOOD_3D("3D Wood"),
        STAUNTON_3D("3D Staunton"),
        PLASTIC_3D("3D Plastic"),
        CHESSKID_3D("3D Chesskid");

        private final String name;

        PieceDesign(String name) {
            this.name = name;
        }

        public String getLinkString() {
            return name.replace(" ", "_").replace("-", "_").toLowerCase();
        }

        public String getName() {
            return name;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        ColorScheme scheme = boardTheme.getScheme();
        if (useOnline) {
            Image image = OnlineAssets.getSavedBoard();
            g.drawImage(image, 0, 0, this);
        }
        List<Move> moves = (tileSelected == null || selected == null) ? new ArrayList<>() : availableMoves(gamePieces, selected, tileSelected, false);

        List<BoardCoordinate> castleRookSquares = new ArrayList<>();

        for (Move move : moves) {
            if (move.getCastleType() != null) {
                castleRookSquares.add(getRook(gamePieces, move.getPiece().isWhite(), move.getCastleType()));
            }
        }

        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                int r1 = view == BoardView.WHITE ? decBoardSize - row : row;
                int c1 = view == BoardView.WHITE ? col : decBoardSize - col;

                if (coordinateDisplayType == CoordinateDisplayType.INSIDE) {
                    if (c1 == 0) {
                        g.setColor((row + col) % 2 == 0 ? scheme.light() : scheme.dark());
                        Rectangle rectangle = new Rectangle(0, (r1 * pieceSize), pieceSize / 4, pieceSize / 4);
                        CoordinateGUI.drawCenteredString(g, BoardCoordinate.getRowString(row), rectangle, getFont());
                    }
                    if (r1 == 0) {
                        g.setColor((row + col) % 2 == 0 ? scheme.dark() : scheme.light());
                        Rectangle rectangle = new Rectangle( (c1 * pieceSize) + ((pieceSize / 4) * 3), (pieceSize * decBoardSize) + ((pieceSize / 4) * 3), pieceSize / 4, pieceSize / 4);
                        CoordinateGUI.drawCenteredString(g, BoardCoordinate.getColString(col), rectangle, getFont());
                    }
                }

                if (!useOnline) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor((row + col) % 2 == 0 ? scheme.dark() : scheme.light());
                    g2d.fillRect((c1) * pieceSize, (r1) * pieceSize, pieceSize, pieceSize);
                    g2d.dispose();
                }

                if (staticHighlights[row][col] != null) {
                    g.setColor(staticHighlights[row][col].getColor());
                    g.fillRect(c1 * pieceSize, r1 * pieceSize, pieceSize, pieceSize);
                } else if (highlightedSquares[row][col] != null) {
                    g.setColor(scheme.getHighlight(highlightedSquares[row][col]));
                    g.fillRect((c1) * pieceSize, (r1) * pieceSize, pieceSize, pieceSize);
                } else if (moveHighlights[row][col] != null) {
                    g.setColor(scheme.getHighlight(moveHighlights[row][col]));
                    g.fillRect((c1) * pieceSize, (r1) * pieceSize, pieceSize, pieceSize);
                }

                for (Move move : moves) {
                    if (move.getTo().equals(new BoardCoordinate(row, col))) {
                        if (gamePieces[tileSelected.row()][tileSelected.col()].isWhite() == playAsWhite) {
                            if (Chess.shouldShowAvailableSquares) {
                                drawHint(g, scheme, r1, c1, move.getTaken() != null);
                            }
                        } else {
                            if (Chess.shouldShowOppositionAvailableSquares) {
                                drawHint(g, scheme, r1, c1, move.getTaken() != null);
                            }
                        }
                    }
                }
                for (BoardCoordinate castleRookSquare : castleRookSquares) {
                    if (castleRookSquare.equals(new BoardCoordinate(row, col))) {
                        if (gamePieces[castleRookSquare.row()][castleRookSquare.col()].isWhite() == playAsWhite) {
                            if (Chess.shouldShowAvailableSquares) {
                                drawHint(g, scheme, r1, c1, true);
                            }
                        } else {
                            if (Chess.shouldShowOppositionAvailableSquares) {
                                drawHint(g, scheme, r1, c1, true);
                            }
                        }
                    }
                }
            }
        }

        historyGUI.repaint();
        captureGUI.repaint();
        coordinateGUI.repaint();
        iconDisplayGUI.repaint();
    }

    public void drawHint(Graphics g, ColorScheme scheme, int row, int col, boolean takes) {
        Color color = scheme.getHighlight(Move.MoveHighlights.AVAILABLE);
        if (takes) {
            switch (captureStyle) {
                case RING -> {
                    Ring ring = new Ring(col * pieceSize, row * pieceSize, pieceSize, pieceSize, pieceSize / 10, color);
                    ring.draw(g);
                }
                case SQUARE -> {
                    Graphics2D g2d = (Graphics2D) g;
                    Stroke oldStroke = g2d.getStroke();
                    g2d.setColor(color);
                    g2d.setStroke(new BasicStroke(pieceSize / 10f));
                    g2d.drawRect((col * pieceSize) + (pieceSize / 20), (row * pieceSize) + (pieceSize / 20), pieceSize - (pieceSize / 10), pieceSize - (pieceSize / 10));
                    g2d.setStroke(oldStroke);
                }
            }
        } else {
            Graphics2D g2d = (Graphics2D) g.create();
            g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, color.getAlpha() / 255f));
            g.setColor(color);
            switch (moveStyle) {
                case DOT -> g.fillOval((col * pieceSize) + (pieceSize / 3), (row * pieceSize) + (pieceSize / 3), pieceSize / 3, pieceSize / 3);
                case SQUARE -> g.fillRect((col * pieceSize) + (pieceSize / 3), (row * pieceSize) + (pieceSize / 3), pieceSize / 3, pieceSize / 3);
            }

            g2d.dispose();
        }
    }

    public Colours getBoardTheme() {
        return boardTheme;
    }

    public boolean online() {
        return useOnline;
    }

    public int getPieceSize() {
        return pieceSize;
    }

    public int getDimension() {
        return dimension;
    }

    public OnlineAssets getOnlineAssets() {
        return onlineAssets;
    }

    public PieceDesign getPieceTheme() {
        return pieceTheme;
    }

    public boolean isPlayAsWhite() {
        return playAsWhite;
    }

    public boolean isWhiteTurn() {
        return whiteTurn;
    }

    public void setPieceSize(int pieceSize) {
        this.pieceSize = pieceSize;
        this.dimension = pieceSize * boardSize;
    }

    public BoardView getView() {
        return view;
    }

    public void setView(BoardView view) {
        this.view = view;
    }

    public void setBoardTheme(Colours boardTheme) {
        this.boardTheme = boardTheme;
        OnlineAssets.updateSavedImage(this);
        repaint();
    }

    public void setPieceTheme(PieceDesign pieceTheme) {
        this.pieceTheme = pieceTheme;
        OnlineAssets.updatePieceDesigns(this);
        displayPieces();
        repaint();
    }

    public Piece[][] getGamePieces() {
        return gamePieces;
    }

    public Piece[][] getSide(boolean white, Piece[][] board) {
        Piece[][] team = new Piece[boardSize][boardSize];

        for (int i = 0; i < board.length; i++) {
            for (int j = 0; j < board.length; j++) {
                Piece piece = board[i][j];
                team[i][j] = (piece != null && piece.isWhite() == white) ? piece : null;
            }
        }

        return team;
    }

    public boolean completelyBlocked(Piece[][] board, BoardCoordinate from, BoardCoordinate to) {
        if (from.equals(to)) return false;
        if (notBlocked(board, from, to)) {
            return board[to.row()][to.col()] != null;
        }
        return true;
    }

    public boolean notBlocked(Piece[][] board, BoardCoordinate f, BoardCoordinate t) {
        int yfrom = f.row();
        int xfrom = f.col();
        int yto = t.row();
        int xto = t.col();

        Piece from = board[yfrom][xfrom];
        Piece to = board[yto][xto];

        int dx = Integer.compare(xto, xfrom);
        int dy = Integer.compare(yto, yfrom);

        int steps = Math.max(Math.abs(xfrom - xto), Math.abs(yfrom - yto));

        if (xfrom == xto || yfrom == yto || Math.abs(xfrom - xto) == Math.abs(yfrom - yto)) {
            for (int i = 1; i < steps; i++) {
                int x = xfrom + i * dx;
                int y = yfrom + i * dy;
                if ((board[y][x] != null && board[y][x] != to && board[y][x] != from)) {
                    return false;
                }
            }
        }
        return true;
    }

    public BoardCoordinate getKing(Piece[][] team) {
        for (int i = 0; i < team.length; i++) {
            for (int j = 0; j < team.length; j++) {
                Piece piece = team[i][j];

                if (piece instanceof King) {
                    return new BoardCoordinate(i, j);
                }
            }
        }
        return null;
    }

    public TradeType tradeOff(Piece[][] board, BoardCoordinate tile, boolean whiteToMove, boolean points) {
        if (points) {
            return pointTradeOff(board, tile, whiteToMove);
        } else {
            return pieceTradeOff(board, tile, whiteToMove);
        }
    }

    public TradeType pieceTradeOff(Piece[][] board, BoardCoordinate tile, boolean whiteToMove) {
        Piece piece = board[tile.row()][tile.col()];

        int whiteAttackers = attackerCount(board, tile, true);
        int blackAttackers = attackerCount(board, tile, false);

        List<Move.SimpleMove> whiteAttackingPoints = attackers(board, tile, true);
        List<Move.SimpleMove> blackAttackingPoints = attackers(board, tile, false);

        whiteAttackingPoints.sort((o1, o2) -> o1.getPiece().points() > o2.getPiece().points() ? 1 : 0);
        blackAttackingPoints.sort(((o1, o2) -> o1.getPiece().points() > o2.getPiece().points() ? 1 : 0));

        int blackFinal = 0;
        int whiteFinal = 0;

        if (piece.isWhite() == whiteToMove) {
            return null;
        } else {
            if (whiteAttackers == 0 && whiteToMove && !piece.isWhite()) return null;
            if (blackAttackers == 0 && !whiteToMove && piece.isWhite()) return null;

            List<Move.SimpleMove> series = intertwine(whiteAttackingPoints, blackAttackingPoints, whiteToMove);
            Piece[][] newBoard = Arrays.stream(board).map(Piece[]::clone).toArray(Piece[][]::new);
            for (Move.SimpleMove simpleMove : series) {
                if (simpleMove.getPiece().isWhite()) {
                    whiteFinal += 1;
                } else {
                    blackFinal += 1;
                }
                newBoard = moveResult(newBoard, simpleMove.getTo(), simpleMove.getFrom(), simpleMove.getTo()); // TODO add preview trade
            }
        }

        return (whiteFinal == blackFinal ? TradeType.EQUAL : (whiteFinal > blackFinal ? TradeType.WHITE : TradeType.BLACK));
    }

    public <T> List<T> trim(List<T> a, int x) {
        List<T> trimmed = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            trimmed.add(a.get(i));
        }
        return trimmed;
    }

    public List<Move.SimpleMove> intertwine(List<Move.SimpleMove> a, List<Move.SimpleMove> b, boolean whiteFirst) {
        int x = Math.max(a.size(), b.size());
        List<Move.SimpleMove> result = new ArrayList<>();
        for (int i = 0; i < x; i++) {
            if (whiteFirst) {
                if (a.size() > i) {
                    result.add(a.get(i));
                } else {
                    return result;
                }
            } else {
                if (b.size() > i) {
                    result.add(b.get(i));
                } else {
                    return result;
                }
            }
            if (whiteFirst) {
                if (b.size() > i) {
                    result.add(b.get(i));
                } else {
                    return result;
                }
            } else {
                if (a.size() > i) {
                    result.add(a.get(i));
                } else {
                    return result;
                }
            }
        }
        return result;
    }

    public TradeType pointTradeOff(Piece[][] board, BoardCoordinate tile, boolean whiteToMove) {
        Piece piece = board[tile.row()][tile.col()];

        int whiteAttackers = attackerCount(board, tile, true);
        int blackAttackers = attackerCount(board, tile, false);

        List<Move.SimpleMove> whiteAttackingPoints = attackers(board, tile, true);
        List<Move.SimpleMove> blackAttackingPoints = attackers(board, tile, false);

        whiteAttackingPoints.sort((o1, o2) -> o1.getPiece().points() > o2.getPiece().points() ? 1 : 0);
        blackAttackingPoints.sort(((o1, o2) -> o1.getPiece().points() > o2.getPiece().points() ? 1 : 0));

        int blackFinal = 0;
        int whiteFinal = 0;

        if (piece.isWhite() == whiteToMove) {
            return null;
        } else {
            if (whiteAttackers == 0 && whiteToMove && !piece.isWhite()) return null;
            if (blackAttackers == 0 && !whiteToMove && piece.isWhite()) return null;

            List<Move.SimpleMove> series = intertwine(whiteAttackingPoints, blackAttackingPoints, whiteToMove);
            Piece[][] newBoard = Arrays.stream(board).map(Piece[]::clone).toArray(Piece[][]::new);
            for (Move.SimpleMove simpleMove : series) {
                if (simpleMove.getPiece().isWhite()) {
                    whiteFinal += newBoard[simpleMove.getTo().row()][simpleMove.getTo().col()].points();
                } else {
                    blackFinal += newBoard[simpleMove.getTo().row()][simpleMove.getTo().col()].points();
                }
                newBoard = moveResult(newBoard, simpleMove.getTo(), simpleMove.getFrom(), simpleMove.getTo());
            }
        }

        return (whiteFinal == blackFinal ? TradeType.EQUAL : (whiteFinal > blackFinal ? TradeType.WHITE : TradeType.BLACK));
    }

    private <T> T[] insert(T[] points, T toInsert) {
        List<T> newPoints = new ArrayList<>();
        newPoints.add(toInsert);
        newPoints.addAll(Arrays.asList(points));

        return (T[]) newPoints.toArray();
    }

    public boolean isHanging(Piece[][] board, BoardCoordinate tile) {
        Piece piece = board[tile.row()][tile.col()];
        int defenders = attackerCount(board, tile, piece.isWhite());
        int attackers = attackerCount(board, tile, !piece.isWhite());
        return defenders == 0 && attackers > 0;
    }

    public int attackerCount(Piece[][] board, BoardCoordinate tile, boolean white) {
        Piece[][] newBoard = Arrays.stream(board).map(Piece[]::clone).toArray(Piece[][]::new);
        Piece piece = newBoard[tile.row()][tile.col()];
        return getAttackers(newBoard, piece, tile, white).size();
    }

    public List<Move.SimpleMove> attackers(Piece[][] board, BoardCoordinate tile, boolean white) {
        Piece[][] newBoard = Arrays.stream(board).map(Piece[]::clone).toArray(Piece[][]::new);
        Piece piece = newBoard[tile.row()][tile.col()];
        return getAttackers(newBoard, piece, tile, white);
    }

    public int getOptimalPointTradeOff(int[] attacking, int used) {
        int c = 0;
        for (int i = 0; i < used; i++) {
            c += attacking[i];
        }
        return c;
    }

    public int getPoints(int[] attacking) {
        int c = 0;
        for (int j : attacking) {
            c += j;
        }
        return c;
    }

    public Move getRandomMove(Piece[][] board) {
        List<Move> moves = getAllValidMoves(board, !playAsWhite, false);
        Random random = new Random();
        return moves.get(random.nextInt(moves.size()));
    }

    public List<Move> availableMoves(Piece[][] board, Piece piece, BoardCoordinate coordinate, boolean isCheckingForMate) {
        List<Move> moves = new ArrayList<>();
        if (piece == null || coordinate == null || gameOver != null) {
            return moves;
        }
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                BoardCoordinate possibleTile = new BoardCoordinate(i, j);
                BoardCoordinate epTake = getEnpassantTakeSquare();
                if (getEnpassantTargetSquare() != null && board[epTake.row()][epTake.col()].isWhite() != piece.isWhite() && possibleTile.equals(getEnpassantTargetSquare()) && piece instanceof Pawn && piece.validMove(coordinate, possibleTile, true)) {
                    if (kingAvoidsCheck(board, piece, getEnpassantTakeSquare(), coordinate, possibleTile)) {
                        Piece[][] newBoard = moveResult(board, epTake, coordinate, possibleTile);

                        BoardCoordinate epSQt = getEnpassantTakeSquare();
                        Piece taken = board[epSQt.row()][epSQt.col()];

                        Move.Check check = null;

                        if (!(taken instanceof King)) {
                            if (!isCheckingForMate && kingIsInCheck(newBoard, !piece.isWhite())) {
                                check = Move.Check.CHECK;

                                if (kingIsCheckmated(newBoard, !piece.isWhite())) {
                                    check = Move.Check.MATE;
                                }
                            }
                        }
                        moves.add(new Move(piece, coordinate, possibleTile, check, true, epSQt, taken, null, null, Move.MoveType.MOVE));
                    }
                } else if (piece instanceof King && getRook(board, piece.isWhite(), Move.CastleType.KINGSIDE) != null && defaultCastleSquares(piece.isWhite(), Move.CastleType.KINGSIDE).contains(possibleTile) && (piece.isWhite() ? whiteCanCastleKingside : blackCanCastleKingside)) {
                    Piece[][] newBoard = castleResult(piece.isWhite(), Move.CastleType.KINGSIDE);
                    if (!kingIsInCheck(newBoard, piece.isWhite())) {
                        if (canCastle(piece.isWhite(), Move.CastleType.KINGSIDE)) {

                            BoardCoordinate kingFrom = getKing(getSide(piece.isWhite(), board));
                            BoardCoordinate kingTo = newKingFile(piece.isWhite(), Move.CastleType.KINGSIDE);
                            Move.Check check = null;

                            if (!isCheckingForMate && kingIsInCheck(newBoard, !piece.isWhite())) {
                                check = Move.Check.CHECK;
                                if (kingIsCheckmated(newBoard, !piece.isWhite())) {
                                    check = Move.Check.MATE;
                                }
                            }
                            moves.add(new Move(piece, kingFrom, kingTo, check, false, null, null, null, Move.CastleType.KINGSIDE, Move.MoveType.CASTLE));
                        }
                    }
                } else if (piece instanceof King && getRook(board, piece.isWhite(), Move.CastleType.QUEENSIDE) != null && defaultCastleSquares(piece.isWhite(), Move.CastleType.QUEENSIDE).contains(possibleTile) && (piece.isWhite() ? whiteCanCastleQueenside : blackCanCastleQueenside)) {
                    Piece[][] newBoard = castleResult(piece.isWhite(), Move.CastleType.QUEENSIDE);
                    if (!kingIsInCheck(newBoard, piece.isWhite())) {
                        if (canCastle(piece.isWhite(), Move.CastleType.QUEENSIDE)) {
                            BoardCoordinate kingFrom = getKing(getSide(piece.isWhite(), board));
                            BoardCoordinate kingTo = newKingFile(piece.isWhite(), Move.CastleType.QUEENSIDE);
                            Move.Check check = null;
                            if (!isCheckingForMate && kingIsInCheck(newBoard, !piece.isWhite())) {
                                check = Move.Check.CHECK;
                                if (kingIsCheckmated(newBoard, !piece.isWhite())) {
                                    check = Move.Check.MATE;
                                }
                            }

                            moves.add(new Move(piece, kingFrom, kingTo, check, false, null, null, null, Move.CastleType.QUEENSIDE, Move.MoveType.CASTLE));
                        }
                    }
                } else {
                    Piece taken = board[possibleTile.row()][possibleTile.col()];
                    boolean capture = taken != null;
                    if (piece.validMove(coordinate, possibleTile, capture) && (!capture || (taken.isWhite() != piece.isWhite())) && notBlocked(board, coordinate, possibleTile)) {
                        if (kingAvoidsCheck(board, piece, possibleTile, coordinate, possibleTile)) {
                            Piece[][] newBoard = moveResult(board, possibleTile, coordinate, possibleTile);
                            Move.Check check = null;

                            if (!(taken instanceof King)) {
                                if (!isCheckingForMate && kingIsInCheck(newBoard, !piece.isWhite())) {
                                    check = Move.Check.CHECK;
                                    if (kingIsCheckmated(newBoard, !piece.isWhite())) {
                                        check = Move.Check.MATE;
                                    }
                                }
                            }
                            moves.add(new Move(piece, coordinate, possibleTile, check, false, possibleTile, taken, null, null, Move.MoveType.MOVE));
                        }
                    }
                }
            }
        }
        return moves;
    }

    public boolean bothKingsInCheck(Piece[][] board) {
        return kingIsInCheck(board, true) && kingIsInCheck(board, false);
    }

    public boolean kingIsCheckmated(Piece[][] board, boolean white) {
        if (kingIsInCheck(board, white)) {
            List<Move> moves = getAllValidMoves(board, white, true);
            for (Move move : moves) {
                if (kingAvoidsCheck(board, move.getPiece(), move.getTakeSquare(), move.getFrom(), move.getTo())) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }

    public List<Move> getAllValidMoves(Piece[][] board, boolean white, boolean isCheckingForMate) {
        List<Move> allMoves = new ArrayList<>();
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j] != null && board[i][j].isWhite() == white) {
                    Piece piece = board[i][j];
                    allMoves.addAll(availableMoves(board, piece, new BoardCoordinate(i, j), isCheckingForMate));
                }
            }
        }
        return allMoves;
    }

    public List<Move.SimpleMove> getAttackers(Piece[][] board, Piece piece, BoardCoordinate tile, boolean white) {
        List<Move.SimpleMove> allMoves = new ArrayList<>();
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                BoardCoordinate from = new BoardCoordinate(i, j);
                Piece pieceFrom = board[i][j];
                if (pieceFrom != null && pieceFrom != piece && !from.equals(tile)) {
                    if (pieceFrom.isWhite() == white && pieceFrom.validMove(from, tile, true) && notBlocked(board, from, tile)) {
                        allMoves.add(new Move.SimpleMove(pieceFrom, from, tile));
                    }
                }
            }
        }
        return allMoves;
    }

    public boolean kingIsInCheck(Piece[][] board, boolean white) {
        BoardCoordinate king = getKing(getSide(white, board));

        return tileIsInCheck(board, king, white);
    }

    public boolean tileIsInCheck(Piece[][] board, BoardCoordinate tile, boolean white) {
        Piece[][] opponent = getSide(!white, board);
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                Piece opp = opponent[i][j];
                if (opp != null) {
                    BoardCoordinate opponentInitialSquare = new BoardCoordinate(i, j);
                    if (opp.validMove(opponentInitialSquare, tile, true) && notBlocked(board, opponentInitialSquare, tile)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public boolean kingAvoidsCheck(Piece[][] board, Piece piece, BoardCoordinate takes, BoardCoordinate from, BoardCoordinate to) {
        Piece[][] newBoard = moveResult(board, takes, from, to);

        return !kingIsInCheck(newBoard, piece.isWhite());
    }

    public void printGame(boolean includeInfo) {
        BoardMatrixRotation.printBoard(gamePieces, view, boardSize);
        if (includeInfo) {
            System.out.println();
            System.out.println("FEN String: " + translateBoardToFEN(gamePieces));
            if (!(history == null || history.isEmpty())) {
                Move lastMove = history.get(history.size() - 1);
                System.out.println("Last move: " + (lastMove.getType() == Move.MoveType.IMPORT ? "Unknown" : lastMove.toString()));
            }
            System.out.println("Turn: " + (whiteTurn ? "White" : "Black"));
            System.out.println();
            System.out.println("Castling:");
            System.out.println("  Black King:   " + (blackCanCastleKingside ? "Yes" : "No"));
            System.out.println("  Black Queen:  " + (blackCanCastleQueenside ? "Yes" : "No"));
            System.out.println("  White King:   " + (whiteCanCastleKingside ? "Yes" : "No"));
            System.out.println("  White Queen:  " + (whiteCanCastleQueenside ? "Yes" : "No"));
            System.out.println();
            System.out.println("Half-move clock: " + halfMoveClock);
            System.out.println("Full-move count: " + fullMoveCount);
            if (history != null && !history.isEmpty()) {
                System.out.println();
                System.out.println("History:");
                int i = 0;
                for (Move move : history) {
                    i++;
                    if (i % 2 == 0) {
                        System.out.print(move.toString());
                        System.out.print("\n");
                    } else {
                        System.out.print("  " + (int) Math.ceil(i / 2f) + ". " + move.toString() + " ");
                    }
                }
            }
        }
    }

    public BoardCoordinate getRook(Piece[][] board, boolean white, Move.CastleType type) {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (board[i][j] != null && board[i][j] instanceof Rook rook) {
                    if (rook.getType() == type && rook.isWhite() == white) {
                        return new BoardCoordinate(i, j);
                    }
                }
            }
        }
        return null;
    }

    public void rawMove(Piece piece, BoardCoordinate takes, BoardCoordinate from, BoardCoordinate to) {
        Piece moved = gamePieces[from.row()][from.col()];
        Piece taken = gamePieces[takes.row()][takes.col()];

        gamePieces[from.row()][from.col()] = null;
        gamePieces[takes.row()][takes.col()] = null;
        gamePieces[to.row()][to.col()] = moved;

        capturedPieces.add(taken);

        Move.Check check = null;
        if (kingIsInCheck(gamePieces, !piece.isWhite())) {
            check = Move.Check.CHECK;

            if (kingIsCheckmated(gamePieces, !piece.isWhite())) {
                check = Move.Check.MATE;
            }
        }

        Move move = new Move(piece, from, to, check, !takes.equals(to), takes, taken,null, null, Move.MoveType.MOVE); // TODO

        history.add(move);
        piece.incrementMoves();

        if (move.getPiece() instanceof Rook rook) {
            if (rook.isWhite()) {
                switch (rook.getType()) {
                    case KINGSIDE -> whiteCanCastleKingside = false;
                    case QUEENSIDE -> whiteCanCastleQueenside = false;
                }
            } else {
                switch (rook.getType()) {
                    case KINGSIDE -> blackCanCastleKingside = false;
                    case QUEENSIDE -> blackCanCastleQueenside = false;
                }
            }
        }
        if (move.getPiece() instanceof King king) {
            if (king.isWhite()) {
                whiteCanCastleKingside = false;
                whiteCanCastleQueenside = false;
            } else {
                blackCanCastleKingside = false;
                blackCanCastleQueenside = false;
            }
        }

        halfMoveClock++;
        if (!whiteTurn) {
            fullMoveCount++;
            gameFENHistory.add(translateBoardToFEN(gamePieces));
            fiftyMoveRule++;
        }
        if (piece instanceof Pawn || taken != null) {
            fiftyMoveRule = 0;
        }
        whiteTurn = !whiteTurn;
    }

    public Piece[][] castleResult(boolean white, Move.CastleType type) {
        Piece[][] newBoard = Arrays.stream(gamePieces).map(Piece[]::clone).toArray(Piece[][]::new);

        BoardCoordinate kingFrom = getKing(getSide(white, newBoard));
        BoardCoordinate rookFrom = getRook(newBoard, white, type);

        BoardCoordinate kingTo = newKingFile(white, type);
        BoardCoordinate rookTo = newRookFile(white, type);

        Piece king = newBoard[kingFrom.row()][kingFrom.col()];
        Piece rook = newBoard[rookFrom.row()][rookFrom.col()];

        newBoard[kingFrom.row()][kingFrom.col()] = null;
        newBoard[rookFrom.row()][rookFrom.col()] = null;

        newBoard[kingTo.row()][kingTo.col()] = king;
        newBoard[rookTo.row()][rookTo.col()] = rook;

        return newBoard;
    }

    public Piece[][] moveResult(Piece[][] board, BoardCoordinate takes, BoardCoordinate from, BoardCoordinate to) {
        Piece[][] newBoard = Arrays.stream(board).map(Piece[]::clone).toArray(Piece[][]::new);
        Piece moved = newBoard[from.row()][from.col()];

        newBoard[from.row()][from.col()] = null;
        if (takes != null) {
            newBoard[takes.row()][takes.col()] = null;
        }
        newBoard[to.row()][to.col()] = moved;

        return newBoard;
    }

    public void rawCastle(boolean white, Move.CastleType type) {
        BoardCoordinate kingFrom = getKing(getSide(white, gamePieces));
        BoardCoordinate rookFrom = getRook(gamePieces, white, type);

        BoardCoordinate kingTo = newKingFile(white, type);
        BoardCoordinate rookTo = newRookFile(white, type);

        Piece king = gamePieces[kingFrom.row()][kingFrom.col()];
        Piece rook = gamePieces[rookFrom.row()][rookFrom.col()];

        king.incrementMoves();
        rook.incrementMoves();

        gamePieces[kingFrom.row()][kingFrom.col()] = null;
        gamePieces[rookFrom.row()][rookFrom.col()] = null;

        gamePieces[kingTo.row()][kingTo.col()] = king;
        gamePieces[rookTo.row()][rookTo.col()] = rook;

        Move.Check check = null;
        if (kingIsInCheck(gamePieces, !white)) {
            check = Move.Check.CHECK;

            if (kingIsCheckmated(gamePieces, !white)) {
                check = Move.Check.MATE;
            }
        }

        Move move = new Move(king, kingFrom, kingTo, check, false, null, null, null, type, Move.MoveType.CASTLE);
        history.add(move);

        if (white) {
            whiteCanCastleKingside = false;
            whiteCanCastleQueenside = false;
        } else {
            blackCanCastleKingside = false;
            blackCanCastleQueenside = false;
        }

        halfMoveClock++;
        if (!whiteTurn) {
            fullMoveCount++;
            gameFENHistory.add(translateBoardToFEN(gamePieces));
        }
        whiteTurn = !whiteTurn;
    }

    public boolean canCastle(boolean white, Move.CastleType type) {
        Piece[][] rookBoard = Arrays.stream(gamePieces).map(Piece[]::clone).toArray(Piece[][]::new);
        Piece[][] kingBoard = Arrays.stream(gamePieces).map(Piece[]::clone).toArray(Piece[][]::new);

        BoardCoordinate kingFrom = getKing(getSide(white, gamePieces));
        BoardCoordinate rookFrom = getRook(gamePieces, white, type);

        BoardCoordinate kingTo = newKingFile(white, type);
        BoardCoordinate rookTo = newRookFile(white, type);

        rookBoard[kingFrom.row()][kingFrom.col()] = null; // Remove king from rook board
        kingBoard[rookFrom.row()][rookFrom.col()] = null; // Remove rook from king board

        if (completelyBlocked(rookBoard, rookFrom, rookTo)) return false;
        if (completelyBlocked(kingBoard, kingFrom, kingTo)) return false;

        List<BoardCoordinate> possibleCheckTiles = getInclusiveIntermediateTiles(kingFrom, kingTo);

        for (BoardCoordinate possibleTile : possibleCheckTiles) {
            if (tileIsInCheck(kingBoard, possibleTile, white)) {
                return false;
            }
        }

        return true;
    }

    public List<BoardCoordinate> getInclusiveIntermediateTiles(BoardCoordinate a, BoardCoordinate b) {
        List<BoardCoordinate> tiles = new ArrayList<>();
        if (a.row() == b.row()) {
            int from = Math.min(a.col(), b.col());
            int to =  Math.max(a.col(), b.col());

            for (int i = from; i <= to; i++) {
                tiles.add(new BoardCoordinate(a.row(), i));
            }
        }
        return tiles;
    }

    public void deselect() {
        pieceInteraction.setTileSelected(null);

        tileSelected = null;
        selected = null;

        removeHighlights(Move.MoveHighlights.SELECTED);
    }

    public boolean isStaleMate(Piece[][] board) {
        List<Move> moves = getAllValidMoves(board, whiteTurn, false);
        return moves.isEmpty() && !kingIsInCheck(board, whiteTurn);
    }

    public boolean isDrawByRepetition() {
        for (String s : gameFENHistory) {
            int c = 0;
            for (String s1 : gameFENHistory) {
                if (s.split(" ")[0].equals(s1.split(" ")[0])) {
                    c++;
                }
            }
            if (c >= 3) {
                return true;
            }
        }
        return false;
    }

    public String simplifyFEN(String FEN) {
        String result = FEN;
        result = result.split(" ")[0];
        result = result.replaceAll("\\d", "");
        result = result.replaceAll("/", "");
        result = result.replaceAll("\\(", "");
        result = result.replaceAll("\\)", "");
        return result;
    }

    public boolean isDrawByInsufficientMaterial(String truncated) {
        String k_vs_K = anagramRegex("kK");
        String k_vs_KN = anagramRegex("kKN");
        String kn_vs_K = anagramRegex("knK");
        String k_vs_KB = anagramRegex("kKB");
        String kb_vs_K = anagramRegex("kbK");

        if (truncated.equals("")) {
            return true;
        }
        if (truncated.matches(k_vs_K)) {
            return true;
        }
        if (truncated.matches(k_vs_KN)) {
            return true;
        }
        if (truncated.matches(kn_vs_K)) {
            return true;
        }
        if (truncated.matches(k_vs_KB)) {
            return true;
        }
        if (truncated.matches(kb_vs_K)) {
            return true;
        }

        String kb_vs_KB = anagramRegex("kbKB");
        if (truncated.matches(kb_vs_KB)) {
            boolean bishop1White = false;
            boolean bishop2White = false;
            for (int i = 0; i < boardSize; i++) {
                for (int j = 0; j < boardSize; j++) {
                    Piece piece = gamePieces[i][j];
                    if (piece != null) {
                        if (piece instanceof Bishop bishop && (i + j) % 2 == 0) {
                            if (!bishop1White) {
                                bishop1White = true;
                            } else {
                                bishop2White = true;
                                break;
                            }
                        }
                    }
                }
            }

            return bishop1White == bishop2White;
        }

        return false;
    }

    public void createGameOverScreen(GameOverType type) {
        if (type != null) {
            gameHighlights = new Move.InfoIcons[boardSize][boardSize];
            staticHighlights = new ColorScheme.StaticColors[boardSize][boardSize];
            highlightIconAccompaniment = new Move.MoveHighlights[boardSize][boardSize];

            highlightChecks();

            BoardCoordinate whiteKing = getKing(getSide(true, gamePieces));
            BoardCoordinate blackKing = getKing(getSide(false, gamePieces));
            switch (type) {
                case CHECKMATE_BLACK -> {
                    staticHighlights[whiteKing.row()][whiteKing.col()] = ColorScheme.StaticColors.MATE;
                    gameHighlights[whiteKing.row()][whiteKing.col()] = Move.InfoIcons.CHECKMATE_WHITE;
                    gameHighlights[blackKing.row()][blackKing.col()] = Move.InfoIcons.WINNER;

                    if (shouldCalculateELO) {
                        if (playAsWhite) {
                            updateELO(ChessMenu.computer, ChessMenu.player, false);
                        } else {
                            updateELO(ChessMenu.player, ChessMenu.computer, false);
                        }
                    }
                }
                case CHECKMATE_WHITE -> {
                    staticHighlights[blackKing.row()][blackKing.col()] = ColorScheme.StaticColors.MATE;
                    gameHighlights[blackKing.row()][blackKing.col()] = Move.InfoIcons.CHECKMATE_BLACK;
                    gameHighlights[whiteKing.row()][whiteKing.col()] = Move.InfoIcons.WINNER;

                    if (shouldCalculateELO) {
                        if (!playAsWhite) {
                            updateELO(ChessMenu.computer, ChessMenu.player, false);
                        } else {
                            updateELO(ChessMenu.player, ChessMenu.computer, false);
                        }
                    }
                }
                case STALEMATE, ILLEGAL_POSITION, INSUFFICIENT_MATERIAL, DRAW_BY_REPETITION, FIFTY_MOVE_RULE, DRAW_BY_AGREEMENT -> {
                    gameHighlights[whiteKing.row()][whiteKing.col()] = Move.InfoIcons.DRAW_WHITE;
                    gameHighlights[blackKing.row()][blackKing.col()] = Move.InfoIcons.DRAW_BLACK;
                    history.add(new Move(null, null, null, null, false, null, null, null, null, Move.MoveType.DRAW));

                    if (shouldCalculateELO) {
                        updateELO(ChessMenu.computer, ChessMenu.player, true);
                    }
                }
                case RESIGNATION_BLACK -> {
                    gameHighlights[blackKing.row()][blackKing.col()] = Move.InfoIcons.RESIGN_BLACK;
                    gameHighlights[whiteKing.row()][whiteKing.col()] = Move.InfoIcons.WINNER;
                    history.add(new Move(null, null, null, null, false, null, null, null, null, Move.MoveType.WHITE));
                    if (shouldCalculateELO) {
                        if (!playAsWhite) {
                            updateELO(ChessMenu.computer, ChessMenu.player, false);
                        } else {
                            updateELO(ChessMenu.player, ChessMenu.computer, false);
                        }
                    }
                }
                case RESIGNATION_WHITE -> {
                    gameHighlights[whiteKing.row()][whiteKing.col()] = Move.InfoIcons.RESIGN_WHITE;
                    gameHighlights[blackKing.row()][blackKing.col()] = Move.InfoIcons.WINNER;
                    history.add(new Move(null, null, null, null, false, null, null, null, null, Move.MoveType.BLACK));
                    if (shouldCalculateELO) {
                        if (playAsWhite) {
                            updateELO(ChessMenu.computer, ChessMenu.player, false);
                        } else {
                            updateELO(ChessMenu.player, ChessMenu.computer, false);
                        }
                    }
                }
            }
            displayPieces();
            repaint();
            switch (type) {
                case CHECKMATE_BLACK -> chess.createPopUp("You " + (!playAsWhite ? "Win" : "Lost") + ": Black wins by checkmate", "Game Over", (!playAsWhite ? Move.InfoIcons.WINNER : Move.InfoIcons.CHECKMATE_WHITE));
                case CHECKMATE_WHITE -> chess.createPopUp("You " + (playAsWhite ? "Win" : "Lost") + ": White wins by checkmate", "Game Over", (playAsWhite ? Move.InfoIcons.WINNER : Move.InfoIcons.CHECKMATE_BLACK));
                case STALEMATE -> chess.createPopUp("Draw by stalemate", "Game Over", (playAsWhite ? Move.InfoIcons.DRAW_WHITE : Move.InfoIcons.DRAW_BLACK));
                case FIFTY_MOVE_RULE -> chess.createPopUp("Draw by 50-move-rule", "Game Over", (playAsWhite ? Move.InfoIcons.DRAW_WHITE : Move.InfoIcons.DRAW_BLACK));
                case ILLEGAL_POSITION -> chess.createPopUp("Draw by illegal position", "Game Over", Move.MoveHighlights.MISTAKE);
                case DRAW_BY_AGREEMENT -> chess.createPopUp("Draw by agreement", "Game Over", (playAsWhite ? Move.InfoIcons.DRAW_WHITE : Move.InfoIcons.DRAW_BLACK));
                case RESIGNATION_BLACK -> chess.createPopUp("You " + (playAsWhite ? "Win" : "Lost") + ": White wins by resignation", "Game Over", (playAsWhite ? Move.InfoIcons.WINNER : Move.InfoIcons.RESIGN_BLACK));
                case RESIGNATION_WHITE -> chess.createPopUp("You " + (!playAsWhite ? "Win" : "Lost") + ": Black wins by resignation", "Game Over", (!playAsWhite ? Move.InfoIcons.WINNER : Move.InfoIcons.RESIGN_BLACK));
                case DRAW_BY_REPETITION -> chess.createPopUp("Draw by three-fold repetition", "Game Over", (playAsWhite ? Move.InfoIcons.DRAW_WHITE : Move.InfoIcons.DRAW_BLACK));
                case INSUFFICIENT_MATERIAL -> chess.createPopUp("Draw by insufficient material", "Game Over", (playAsWhite ? Move.InfoIcons.DRAW_WHITE : Move.InfoIcons.DRAW_BLACK));
                case TIME_BLACK -> chess.createPopUp("You " + (playAsWhite ? "Lost" : "Won") + ": Black wins on time", "Game Over", (playAsWhite ? Move.InfoIcons.TIME_WHITE : Move.InfoIcons.WINNER));
                case TIME_WHITE -> chess.createPopUp("You " + (playAsWhite ? "Won" : "Lost") + ": White wins on time", "Game Over", (!playAsWhite ? Move.InfoIcons.TIME_BLACK : Move.InfoIcons.WINNER));
                case ABORTED_BLACK -> chess.createPopUp("Game ended: Black aborted game", "Game Over", Move.InfoIcons.ABORTED);
                case ABORTED_WHITE -> chess.createPopUp("Game ended: White aborted game", "Game Over", Move.InfoIcons.ABORTED);
                case ABANDONMENT_BLACK -> chess.createPopUp("You " + (playAsWhite ? "Lost" : "Won") + ": Black wins by abandonment", "Game Over", (playAsWhite ? Move.InfoIcons.ABORTED : Move.InfoIcons.WINNER));
                case ABANDONMENT_WHITE -> chess.createPopUp("You " + (!playAsWhite ? "Lost" : "Won") + ": White wins by abandonment", "Game Over", (!playAsWhite ? Move.InfoIcons.ABORTED : Move.InfoIcons.WINNER));
            }
        }
    }

    public void updateELO(Rating winner, Rating loser, boolean draw) {
        if (draw) {
            ChessMenu.results.addDraw(winner, loser);
        } else {
            ChessMenu.results.addResult(winner, loser);
        }
        ChessMenu.calculator.updateRatings(ChessMenu.results);
    }

    public boolean gameOver() {
        if (gameOver != null) {
            createGameOverScreen(gameOver);

            return true;
        } else {
            if (isDrawByInsufficientMaterial(simplifyFEN(translateBoardToFEN(gamePieces)))) {
                gameOver = GameOverType.INSUFFICIENT_MATERIAL;
                createGameOverScreen(gameOver);
                return true;
            }
            if (isStaleMate(gamePieces)) {
                gameOver = GameOverType.STALEMATE;
                createGameOverScreen(gameOver);
                return true;
            }
            if (kingIsCheckmated(gamePieces, playAsWhite)) {
                gameOver = playAsWhite ? GameOverType.CHECKMATE_BLACK : GameOverType.CHECKMATE_WHITE;
                createGameOverScreen(gameOver);
                return true;
            }
            if (kingIsCheckmated(gamePieces, !playAsWhite)) {
                gameOver = playAsWhite ? GameOverType.CHECKMATE_WHITE : GameOverType.CHECKMATE_BLACK;
                createGameOverScreen(gameOver);
                return true;
            }
            if (fiftyMoveRule >= 50) {
                gameOver = GameOverType.FIFTY_MOVE_RULE;
                createGameOverScreen(gameOver);
                return true;
            }
            if (isDrawByRepetition()) {
                gameOver = GameOverType.DRAW_BY_REPETITION;
                createGameOverScreen(gameOver);
                return true;
            }
            gameOver = null;
            return false;
        }
    }

    public String anagramRegex(String truncatedFEN) {
        StringBuilder lookahead = new StringBuilder();
        StringBuilder matchPart = new StringBuilder("^");
        String positiveLookaheadPrefix = "(?=";
        String positiveLookaheadSuffix = ")";
        HashMap<String, Integer> inputCharacterFrequencyMap = new HashMap<>();
        for (int i = 0; i < truncatedFEN.length(); i++) {
            String s = String.valueOf(truncatedFEN.charAt(i));
            inputCharacterFrequencyMap.put(s, inputCharacterFrequencyMap.getOrDefault(s + 1, 1));
        }
        for (String string : inputCharacterFrequencyMap.keySet()) {
            lookahead.append(positiveLookaheadPrefix);
            for (int i = 0; i < inputCharacterFrequencyMap.get(string); i++) {
                lookahead.append(".*");
                if (string.equals(" ")) {
                    lookahead.append("\\s");
                } else {
                    lookahead.append(string);
                }
                matchPart.append(".");
            }
            lookahead.append(positiveLookaheadSuffix);
        }
        matchPart.append("$");
        return lookahead.toString() + matchPart;
    }

    public void highlightChecks() {
        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                if (staticHighlights[i][j] == ColorScheme.StaticColors.CHECK) {
                    staticHighlights[i][j] = null;
                }
            }
        }
        if (kingIsInCheck(gamePieces, true)) {
            BoardCoordinate king = getKing(getSide(true, gamePieces));
            staticHighlights[king.row()][king.col()] = ColorScheme.StaticColors.CHECK;
        }
        if (kingIsInCheck(gamePieces, false)) {
            BoardCoordinate king = getKing(getSide(false, gamePieces));
            staticHighlights[king.row()][king.col()] = ColorScheme.StaticColors.CHECK;
        }
    }

    public void endComputerTurn() {
        if (!gameOver()) {
            highlightChecks();
            displayPieces();
            repaint();
        }
    }

    public void endTurn() {
        deselect();
        if (!gameOver()) {
            switch (opponentType) {
                case AI_1 -> {
                    // TODO best move
                }
                case AI_2 -> new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        Move move = getRandomMove(gamePieces);
                        if (move.getCastleType() != null) {
                            rawCastle(!playAsWhite, move.getCastleType());
                        } else {
                            rawMove(move.getPiece(), move.getTakeSquare(), move.getFrom(), move.getTo());
                        }
                        endComputerTurn();
                    }
                }, 250);
                case AUTO_SWAP -> new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        swapPlaySide();
                    }
                }, 250);
            }

            highlightChecks();
            displayPieces();
            repaint();
        }
    }

    public void movePiece(Piece piece, BoardCoordinate from, BoardCoordinate to) {
        if (!from.equals(to)) {
            boolean white = piece.isWhite();

            List<Move> moves = availableMoves(gamePieces, piece, from, false);
            if (whiteTurn == white && whiteTurn == playAsWhite) {
                for (Move move : moves) {
                    if (move.getTo().equals(to)) {
                        moveHighlight(move.getFrom(), move.getTo());
                        if (move.getCastleType() != null) {
                            rawCastle(piece.isWhite(), move.getCastleType());
                        } else {
                            rawMove(move.getPiece(), move.getTakeSquare(), move.getFrom(), move.getTo());
                        }
                        endTurn();
                        break;
                    }
                }
            }
        }
        displayPieces();
        repaint();
        
    }

    public void resign() {
        if (gameOver == null) {
            gameOver = playAsWhite ? GameOverType.RESIGNATION_WHITE : GameOverType.RESIGNATION_BLACK;
            createGameOverScreen(gameOver);
        }
    }

    public void offerDraw() {
        switch (opponentType) {
            case AI_2, AI_1 -> {
                // TODO if ai is up material then dont (random chance like 20%) idk
            }
            case MANUAL, AUTO_SWAP -> {
                if (gameOver == null) {
                    gameOver = GameOverType.DRAW_BY_AGREEMENT;
                    createGameOverScreen(gameOver);
                }
            }
        }
    }

    public void abort() {
        if (gameOver == null) {
            gameOver = playAsWhite ? GameOverType.ABORTED_WHITE : GameOverType.ABORTED_BLACK;
            createGameOverScreen(gameOver);
        }
    }

    public List<Move> getHistory() {
        return history;
    }

    public void swapPlaySide() {
        deselect();
        playAsWhite = !playAsWhite;
        view = view == BoardView.WHITE ? BoardView.BLACK : BoardView.WHITE;
        displayPieces();
        repaint();
    }

    public OpponentType getOpponentType() {
        return opponentType;
    }

    public Piece getSelected() {
        return selected;
    }

    public BoardCoordinate getTileSelected() {
        return tileSelected;
    }

    public MoveStyle getMoveMethod() {
        return moveMethod;
    }

    public void setMoveMethod(MoveStyle moveMethod) {
        this.moveMethod = moveMethod;
    }

    public void setCaptureStyle(HintStyle.Capture captureStyle) {
        this.captureStyle = captureStyle;
    }

    public void setMoveStyle(HintStyle.Move moveStyle) {
        this.moveStyle = moveStyle;
    }

    public HintStyle.Capture getCaptureStyle() {
        return captureStyle;
    }

    public HintStyle.Move getMoveStyle() {
        return moveStyle;
    }

    public GameOverType getGameOver() {
        return gameOver;
    }

    public int getBoardSize() {
        return boardSize;
    }

    public GameType getGameType() {
        return gameType;
    }

    public HistoryGUI getSidePanelGUI() {
        return historyGUI;
    }

    public CoordinateDisplayType getCoordinateDisplayType() {
        return coordinateDisplayType;
    }

    public void setCoordinateDisplayType(CoordinateDisplayType coordinateDisplayType) {
        this.coordinateDisplayType = coordinateDisplayType;
        Chess.offSet = (coordinateDisplayType == BoardGUI.CoordinateDisplayType.OUTSIDE ? Chess.defaultOffset : 0);
        Chess.heightOffSet = (coordinateDisplayType == CoordinateDisplayType.OUTSIDE ? Chess.defaultOffset / 2 : 0);
        Chess.refreshWindow();
    }

    public CoordinateGUI getCoordinateGUI() {
        return coordinateGUI;
    }

    public Move.InfoIcons[][] getGameHighlights() {
        return gameHighlights;
    }

    public Move.MoveHighlights[][] getHighlightIconAccompaniment() {
        return highlightIconAccompaniment;
    }

    public IconDisplayGUI getIconDisplayGUI() {
        return iconDisplayGUI;
    }

    public CaptureGUI getCaptureGUI() {
        return captureGUI;
    }
}
