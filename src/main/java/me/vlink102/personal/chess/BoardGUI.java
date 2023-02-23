package me.vlink102.personal.chess;

import me.vlink102.personal.chess.pieces.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;
import java.util.Timer;

public class BoardGUI extends JPanel {
    private final Chess chess;
    private final PieceInteraction pieceInteraction;

    private Chess.BoardLayout currentLayout;

    private final OnlineAssets onlineAssets;
    private boolean useOnline;
    private PieceDesign pieceTheme;
    private Colours boardTheme;
    private int pieceSize;
    private Piece[][] gamePieces;
    private Move.MoveHighlights[][] highlightedSquares;
    private boolean[][] moveHighlights;
    private Move.InfoIcons[][] gameHighlights;
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
        ILLEGAL_POSITION
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

    public enum TradeType {
        WHITE,
        BLACK,
        EQUAL
    }

    public void setOpponent(OpponentType type) {
        opponentType = type;
    }

    public void setupBoard(Chess.BoardLayout layout) {
        this.currentLayout = layout;
        this.dimension = pieceSize * 8;
        this.view = playAsWhite ? BoardView.WHITE : BoardView.BLACK;
        this.gamePieces = new Piece[8][8];
        this.highlightedSquares = new Move.MoveHighlights[8][8];
        this.moveHighlights = new boolean[8][8];
        this.gameHighlights = new Move.InfoIcons[8][8];

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
        setupBoard(layout);
        repaint();
        displayPieces();
        chess.createPopUp("Success!", "Board reset", Move.MoveHighlights.EXCELLENT);
    }

    public void resetBoard() {
        resetBoard(currentLayout);
    }

    public BoardGUI(Chess chess, PieceDesign pieceTheme, Colours boardTheme, int pSz, boolean useOnline, boolean playAsWhite, Chess.BoardLayout layout, OpponentType type, MoveStyle moveMethod, HintStyle.Move moveStyle, HintStyle.Capture captureStyle) {
        this.chess = chess;
        this.useOnline = useOnline;
        this.boardTheme = boardTheme;
        this.pieceTheme = pieceTheme;
        this.pieceSize = pSz;
        this.playAsWhite = playAsWhite;
        this.opponentType = type;
        this.moveMethod = moveMethod;
        this.captureStyle = captureStyle;
        this.moveStyle = moveStyle;
        this.onlineAssets = new OnlineAssets(this);

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
        registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_2, 0, false), "show-point-trades", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                showTrades(true);
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

        setLayout(new GridLayout(8, 8));
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                JPanel square = new JPanel(new BorderLayout());
                square.setOpaque(false);
                add(square);
            }
        }
    }

    public void registerKeyBinding(KeyStroke keyStroke, String name, Action action) {
        InputMap im = getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = getActionMap();

        im.put(keyStroke, name);
        am.put(name, action);
    }

    public boolean validateFEN(String FEN) {
        String[] sections = FEN.split(" ");
        if (sections.length != 6) return false;
        if (sections[0].split("/").length != 8) return false;
        if (!sections[1].equalsIgnoreCase("w") && !sections[1].equalsIgnoreCase("b")) return false;
        if (!(Integer.parseInt(sections[4]) / 2 == Integer.parseInt(sections[5]) - 1)) return false;
        if (!(sections[0].contains("k") && sections[0].contains("K"))) return false;
        return BoardCoordinate.isValidTile(sections[3]) || sections[3].equalsIgnoreCase("-");
    }

    public void loadFEN(String FEN) {
        if (validateFEN(FEN)) {
            gamePieces = new Piece[8][8];
            history = new ArrayList<>();
            String[] sections = FEN.split(" ");
            String[] board = sections[0].split("/");

            whiteCanCastleKingside = sections[2].contains("K");
            whiteCanCastleQueenside = sections[2].contains("Q");
            blackCanCastleKingside = sections[2].contains("k");
            blackCanCastleQueenside = sections[2].contains("q");

            for (int rank = 0; rank < 8; rank++) {
                String[] row = board[7 - rank].split("");
                StringBuilder parseEmpty = new StringBuilder();
                for (String string : row) {
                    if (string.matches("\\d")) {
                        parseEmpty.append(" ".repeat(Integer.parseInt(string)));
                    } else {
                        parseEmpty.append(string);
                    }
                }
                String[] newRow = parseEmpty.toString().split("");
                for (int i = 0; i < 8; i++) {
                    boolean isUpper = Character.isUpperCase(newRow[i].toCharArray()[0]);
                    switch (newRow[i].toLowerCase()) {
                        case "r" -> {
                            if (isUpper) {
                                if (i == 0 && rank == 0 && whiteCanCastleQueenside) {
                                    gamePieces[rank][i] = new Rook(this, true, new BoardCoordinate(rank, i));
                                } else if (i == 7 && rank == 0 && whiteCanCastleKingside) {
                                    gamePieces[rank][i] = new Rook(this, true, new BoardCoordinate(rank, i));
                                } else {
                                    gamePieces[rank][i] = new Rook(this, true, new BoardCoordinate(rank, i));
                                }

                            } else {
                                if (i == 0 && rank == 7 && blackCanCastleQueenside) {
                                    gamePieces[rank][i] = new Rook(this, false, new BoardCoordinate(rank, i));
                                } else if (i == 7 && rank == 7 && blackCanCastleKingside) {
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
                    }
                }
            }

            switch (currentLayout) {
                case DEFAULT -> {
                    if (gamePieces[0][0] == null && whiteCanCastleQueenside) {
                        whiteCanCastleQueenside = false;
                    }
                    if (gamePieces[0][7] == null && whiteCanCastleKingside) {
                        whiteCanCastleKingside = false;
                    }
                    if (gamePieces[7][0] == null && blackCanCastleQueenside) {
                        blackCanCastleQueenside = false;
                    }
                    if (gamePieces[7][7] == null && blackCanCastleKingside) {
                        blackCanCastleKingside = false;
                    }
                }
                case CHESS960 -> {
                    for (int i = 0; i < 8; i++) {
                        for (int j = 0; j < 8; j++) {
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

            if (bothKingsInCheck(gamePieces)) {
                chess.createPopUp("Warning: Both kings are in check", "Illegal game state", Move.MoveHighlights.MISTAKE);
                gameOver = GameOverType.ILLEGAL_POSITION;
            } else if (kingIsCheckmated(gamePieces, true)) {
                chess.createPopUp("Warning: White king is already checkmated", "Game state over", (playAsWhite ? Move.InfoIcons.CHECKMATE_BLACK : Move.InfoIcons.WINNER));
                gameOver = GameOverType.CHECKMATE_BLACK;
            } else if (kingIsCheckmated(gamePieces, false)) {
                chess.createPopUp("Warning: Black king is already checkmated", "Game state over", (playAsWhite ? Move.InfoIcons.WINNER : Move.InfoIcons.CHECKMATE_WHITE));
                gameOver = GameOverType.CHECKMATE_WHITE;
            } else {
                chess.createPopUp("Success!", "Loaded game state", Move.MoveHighlights.EXCELLENT);
                gameOver = null;
            }

            displayPieces();
            repaint();
        } else {
            String result = fixFENString(FEN);
            if (result == null) {
                chess.createPopUp("Invalid FEN String: " + FEN + "\n\nFix Failed:\n - Wrong number of Kings\n\n" + result, "Could not load game state", Move.MoveHighlights.MISTAKE);
                gameOver = GameOverType.ILLEGAL_POSITION;
            } else {
                if (!validateFEN(result)) {
                    chess.createPopUp("Invalid FEN String: " + FEN + "\n\nFix Failed:\n - Could not repair FEN String\n\n" + result, "Could not load game state", Move.MoveHighlights.BLUNDER);
                    gameOver = GameOverType.ILLEGAL_POSITION;
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
        Piece[][] board = new Piece[8][8];
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

    public boolean isValidFENRow(String FENRow) {
        int count = 0;
        for (String s : FENRow.split("")) {
            if (s.matches("[RNBQKPrnbqkp]")) {
                count++;
            } else if (s.matches("[1-8]")) {
                count += Integer.parseInt(s);
            } else {
                return false;
            }
        }
        return count == 8;
    }

    public String fixFENRow(String FENRow) {
        StringBuilder result = new StringBuilder();
        int count = 0;
        for (String s : FENRow.split("")) {
            if (s.matches("[RNBQKPrnbqkp]")) {
                if (count + 1 > 8) {
                    break;
                } else {
                    result.append(s);
                    count++;
                }
            } else if (s.matches("[1-8]")) {
                if (count + Integer.parseInt(s) > 8 ) {
                    break;
                } else {
                    result.append(s);
                    count += Integer.parseInt(s);
                }
            }
        }
        if (count < 8) {
            result.append(8 - count);
        }

        String toTruncate = result.toString();
        List<String> truncatedResult = new ArrayList<>();
        boolean lastWasDigit = false;
        for (char c : toTruncate.toCharArray()) {
            if (Character.isDigit(c)) {
                if (lastWasDigit) {
                    truncatedResult.set(truncatedResult.size() - 1, String.valueOf(Integer.parseInt(truncatedResult.get(truncatedResult.size() - 1)) + Integer.parseInt(String.valueOf(c))));
                } else {
                    truncatedResult.add(String.valueOf(c));
                }
                lastWasDigit = true;
            }
            if (String.valueOf(c).matches("[A-Za-z]")) {
                truncatedResult.add(String.valueOf(c));
                lastWasDigit = false;
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

    public String squish(List<String> strings) {
        StringBuilder builder = new StringBuilder();
        for (String string : strings) {
            builder.append(string);
        }
        return builder.toString();
    }

    public String fenBoard(String[] board) {
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

    public String fixFENBoard(String FENBoard) {
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
        System.out.println(FENBoard);

        String[] rows = FENBoard.split("/");

        for (int i = 0; i < rows.length; i++) {
            if (rows[i] == null || rows[i].equalsIgnoreCase("")) {
                rows[i] = "8";
            }
            if (!isValidFENRow(rows[i])) {
                rows[i] = fixFENRow(rows[i]);
            }
        }

        FENBoard = fenBoard(rows);

        System.out.println(FENBoard);

        String[] validRows = FENBoard.split("/");
        if (validRows.length < 8) {
            if (Chess.shouldRelocateBackline) {
                FENBoard = relocateFENBackLine(FENBoard, validRows);
            } else {
                for (int i = 0; i < 8 - validRows.length; i++) {
                    FENBoard = FENBoard + "/8";
                }
            }
        }
        if (validRows.length > 8) {
            FENBoard = relocateFENBackLine(FENBoard, validRows);
        }

        System.out.println(FENBoard);

        return FENBoard;
    }

    private String relocateFENBackLine(String FENBoard, String[] validRows) {
        String whiteBackLine = null;
        String blackBackLine = null;
        for (String validRow : validRows) {
            if (validRow.contains("K")) {
                whiteBackLine = validRow;
                FENBoard = FENBoard.replace(whiteBackLine, "");
            }
            if (validRow.contains("k")) {
                blackBackLine = validRow;
                FENBoard = FENBoard.replace(blackBackLine, "");
            }
        }
        FENBoard = FENBoard.replaceAll("/+", "/");
        String[] withoutBackline = FENBoard.split("/");
        String[] trimmedFenBoard = new String[8];
        trimmedFenBoard[0] = blackBackLine;
        System.arraycopy(withoutBackline, 1, trimmedFenBoard, 1, 6);
        trimmedFenBoard[7] = whiteBackLine;
        FENBoard = fenBoard(trimmedFenBoard);
        return FENBoard;
    }

    public String fixFENString(String FEN) {
        String[] sections = FEN.split(" ");
        int sectionLength = sections.length;

        StringJoiner result = new StringJoiner(" ");

        String board = sectionLength > 0 ? sections[0] : "";
        board = fixFENBoard(board);
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
        if (!BoardCoordinate.isValidTile(enPassantSquare) && !enPassantSquare.equalsIgnoreCase("-")) {
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

    public String translateBoardToFEN(Piece[][] board) {
        StringBuilder fen = new StringBuilder();
        for (int rank = 0; rank < 8; rank++) {
            int empty = 0;
            StringBuilder rankFen = new StringBuilder();
            for (int file = 0; file < 8; file++) {
                if(board[7 - rank][file] == null) {
                    empty++;
                } else {
                    Piece piece = board[7 - rank][file];
                    if (empty != 0) rankFen.append(empty);
                    rankFen.append(piece.isWhite() ? piece.getAbbr().toUpperCase() : piece.getAbbr().toLowerCase());
                    empty = 0;
                }
            }
            if (empty != 0) rankFen.append(empty);
            fen.append(rankFen);
            if (!(rank == board.length - 1)) {
                fen.append("/");
            }
        }
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
        if (white) {
            if (whiteCanCastleKingside) {
                coordinates.add(new BoardCoordinate(0, 6));
            }
            if (whiteCanCastleQueenside) {
                coordinates.add(new BoardCoordinate(0, 2));
            }
        } else {
            if (blackCanCastleKingside) {
                coordinates.add(new BoardCoordinate(7, 6));
            }
            if (blackCanCastleQueenside) {
                coordinates.add(new BoardCoordinate(7, 2));
            }
        }
        return coordinates;
    }

    public List<BoardCoordinate> rookCastleSquares(boolean white, Move.CastleType type) {
        List<BoardCoordinate> coordinates = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
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
        return new BoardCoordinate(white ? 0 : 7, type == Move.CastleType.KINGSIDE ? 6 : 2);
    }

    public BoardCoordinate newRookFile(boolean white, Move.CastleType type) {
        return new BoardCoordinate(white ? 0 : 7, type == Move.CastleType.KINGSIDE ? 5 : 3);
    }

    public void displayPieces() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JPanel panel = (JPanel) getComponent((row * 8) + col);
                int r1 = view == BoardView.WHITE ? 7 - row : row;
                int c1 = view == BoardView.WHITE ? col : 7 - col;

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

    public void setupDefaultBoard(boolean white) {
        int backLine = white ? 0 : 7;
        for (int i = 0; i < 8; i++) {
            gamePieces[white ? 1 : 6][i] = new Pawn(this, white);
            //gamePieces[white ? 2 : 5][i] = new RiceFarmer(this, white);
        }

        gamePieces[backLine][0] = new Rook(this, white, new BoardCoordinate(backLine, 0));
        gamePieces[backLine][7] = new Rook(this, white, new BoardCoordinate(backLine, 7));

        gamePieces[backLine][1] = new Knight(this, white);
        gamePieces[backLine][6] = new Knight(this, white);

        gamePieces[backLine][2] = new Bishop(this, white);
        gamePieces[backLine][5] = new Bishop(this, white);

        gamePieces[backLine][3] = new Queen(this, white);
        gamePieces[backLine][4] = new King(this, white);
    }

    public Piece fromChar(char piece, boolean white, int index) {
        return switch (piece) {
            case 'R', 'X' -> new Rook(this, white, new BoardCoordinate(white ? 0 : 7, index));
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
                setupDefaultBoard(true);
                setupDefaultBoard(false);
            }
            case CHESS960 -> {
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
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (gamePieces[i][j] != null) {
                    if (isHanging(gamePieces, new BoardCoordinate(i, j))) {
                        if (gamePieces[i][j].isWhite() == playAsWhite) {
                            highlightedSquares[i][j] = Move.MoveHighlights.HIGHLIGHT;
                        } else if (gamePieces[i][j].isWhite() != playAsWhite) {
                            highlightedSquares[i][j] = Move.MoveHighlights.BLUE_HIGHLIGHT;
                        }
                    }
                }
            }
        }
    }

    public void showTrades(boolean points) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (gamePieces[i][j] != null) {
                    BoardCoordinate tile = new BoardCoordinate(i, j);
                    TradeType type = tradeOff(gamePieces, tile, whiteTurn, points);
                    if (type != null) {
                        switch (type) {
                            case BLACK -> {
                                if (!playAsWhite) {
                                    highlightedSquares[i][j] = Move.MoveHighlights.GREEN_HIGHLIGHT;
                                } else {
                                    highlightedSquares[i][j] = Move.MoveHighlights.HIGHLIGHT;
                                }
                            }
                            case EQUAL -> highlightedSquares[i][j] = Move.MoveHighlights.ORANGE_HIGHLIGHT;
                            case WHITE -> {
                                if (playAsWhite) {
                                    highlightedSquares[i][j] = Move.MoveHighlights.GREEN_HIGHLIGHT;
                                } else {
                                    highlightedSquares[i][j] = Move.MoveHighlights.HIGHLIGHT;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void removeHighlights(Move.MoveHighlights highlights) {
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (highlightedSquares[i][j] == highlights) {
                    highlightedSquares[i][j] = null;
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
                }
                if (SwingUtilities.isLeftMouseButton(e)) {
                    removeHighlights(Move.MoveHighlights.HIGHLIGHT);
                    removeHighlights(Move.MoveHighlights.GREEN_HIGHLIGHT);
                    removeHighlights(Move.MoveHighlights.ORANGE_HIGHLIGHT);
                    removeHighlights(Move.MoveHighlights.BLUE_HIGHLIGHT);

                    int f1 = e.getX() / pieceSize;
                    int r1 = e.getY() / pieceSize;

                    int r2 = view == BoardView.WHITE ? 7 - r1 : r1;
                    int c2 = view == BoardView.WHITE ? f1 : 7 - f1;

                    BoardCoordinate coordinate = new BoardCoordinate(r1, f1);

                    int row = coordinate.row();
                    int col = coordinate.col();

                    switch (view) {
                        case BLACK -> col = 7 - col;
                        case WHITE -> row = 7 - row;
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
                                if (whiteTurn == playAsWhite) {
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
                            highlight(coordinate.row(), coordinate.col(), Move.MoveHighlights.BLUE_HIGHLIGHT);
                        } else if (e.isControlDown()) {
                            highlight(coordinate.row(), coordinate.col(), Move.MoveHighlights.ORANGE_HIGHLIGHT);
                        } else if (e.isShiftDown()) {
                            highlight(coordinate.row(), coordinate.col(), Move.MoveHighlights.GREEN_HIGHLIGHT);
                        } else {
                            highlight(coordinate.row(), coordinate.col(), Move.MoveHighlights.HIGHLIGHT);
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
                            case BLACK -> col = 7 - col;
                            case WHITE -> row = 7 - row;
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
        moveHighlights = new boolean[8][8];

        int r2 = view == BoardView.WHITE ? 7 - from.row() : from.row();
        int c2 = view == BoardView.WHITE ? from.col() : 7 - from.col();

        int r3 = view == BoardView.WHITE ? 7 - to.row() : to.row();
        int c3 = view == BoardView.WHITE ? to.col() : 7 - to.col();

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (highlightedSquares[i][j] == Move.MoveHighlights.MOVE || highlightedSquares[i][j] == Move.MoveHighlights.SELECTED) {
                    highlightedSquares[i][j] = null;
                }
            }
        }

        highlight(r2, c2, Move.MoveHighlights.MOVE);
        highlight(r3, c3, Move.MoveHighlights.MOVE);
    }

    public void highlight(int row, int col, Move.MoveHighlights type) {
        switch (view) {
            case BLACK -> col = 7 - col;
            case WHITE -> row = 7 - row;
        }

        if (type == Move.MoveHighlights.SELECTED) {
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
                    if (highlightedSquares[i][j] == Move.MoveHighlights.HIGHLIGHT) {
                        highlightedSquares[i][j] = null;
                    }
                }
            }
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
        } else if (type == Move.MoveHighlights.MOVE) {
            moveHighlights[row][col] = true;
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
            Image image = OnlineAssets.getSavedBoard().getScaledInstance(pieceSize * 8, pieceSize * 8, Image.SCALE_FAST);
            g.drawImage(image, 0, 0, null);
        }
        List<Move> moves = selected == null ? new ArrayList<>() : availableMoves(gamePieces, selected, tileSelected, false);

        List<BoardCoordinate> castleRookSquares = new ArrayList<>();

        for (Move move : moves) {
            if (move.getCastleType() != null) {
                castleRookSquares.add(getRook(gamePieces, move.getPiece().isWhite(), move.getCastleType()));
            }
        }

        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                int r1 = view == BoardView.WHITE ? 7 - row : row;
                int c1 = view == BoardView.WHITE ? col : 7 - col;

                if (!useOnline) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor((row + col) % 2 == 0 ? scheme.dark() : scheme.light());
                    g2d.fillRect((c1) * pieceSize, (r1) * pieceSize, pieceSize, pieceSize);
                    g2d.dispose();
                }

                if (highlightedSquares[row][col] != null) {
                    g.setColor(scheme.getHighlight(highlightedSquares[row][col]));
                    g.fillRect((c1) * pieceSize, (r1) * pieceSize, pieceSize, pieceSize);
                } else if (moveHighlights[row][col]) {
                    g.setColor(scheme.getHighlight(Move.MoveHighlights.MOVE));
                    g.fillRect((c1) * pieceSize, (r1) * pieceSize, pieceSize, pieceSize);
                }

                if (gameHighlights[row][col] != null && Move.getInfoIcon(gameHighlights[row][col]) != null) {
                    displayIcon(g, new BoardCoordinate(r1, c1), gameHighlights[row][col]);
                } else if (highlightedSquares[row][col] != null && Move.getMoveHighlightIcon(highlightedSquares[row][col]) != null) {
                    displayIcon(g, new BoardCoordinate(r1, c1), highlightedSquares[row][col]);
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
        this.dimension = pieceSize * 8;
    }

    public BoardView getView() {
        return view;
    }

    public void setView(BoardView view) {
        this.view = view;
    }

    public void setBoardTheme(Colours boardTheme) {
        this.boardTheme = boardTheme;
        onlineAssets.updateSavedImage(this);
        repaint();
    }

    public void setPieceTheme(PieceDesign pieceTheme) {
        this.pieceTheme = pieceTheme;
        onlineAssets.updatePieceDesigns(this);
        displayPieces();
        repaint();
    }

    public Piece[][] getGamePieces() {
        return gamePieces;
    }

    public Piece[][] getSide(boolean white, Piece[][] board) {
        // Returns the opponents of the given color in their current positions
        Piece[][] team = new Piece[8][8];

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
            if (board[to.row()][to.col()] == null) {
                return false;
            }
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

        int whiteAttackers = attackers(board, tile, true);
        int blackAttackers = attackers(board, tile, false);

        if ((whiteAttackers == 0 && whiteToMove) || (blackAttackers == 0 && !whiteToMove)) {
            return null;
        }

        if (whiteToMove) {
            if (piece.isWhite()) {
                return null;
            } else {
                if (whiteAttackers == blackAttackers) {
                    return TradeType.EQUAL;
                } else {
                    if (whiteAttackers > blackAttackers) {
                        return TradeType.WHITE;
                    } else {
                        return TradeType.BLACK;
                    }
                }
            }
        } else {
            if (piece.isWhite()) {
                if (whiteAttackers == blackAttackers) {
                    return TradeType.EQUAL;
                } else {
                    if (blackAttackers > whiteAttackers) {
                        return TradeType.BLACK;
                    } else {
                        return TradeType.WHITE;
                    }
                }
            } else {
                return null;
            }
        }
    }

    public TradeType pointTradeOff(Piece[][] board, BoardCoordinate tile, boolean whiteToMove) {
        Piece piece = board[tile.row()][tile.col()];

        int whiteAttackers = attackers(board, tile, true);
        int blackAttackers = attackers(board, tile, false);

        int[] whiteAttackingPoints = attackingPoints(board, tile, true);
        int[] blackAttackingPoints = attackingPoints(board, tile, false);

        Arrays.sort(whiteAttackingPoints);
        Arrays.sort(blackAttackingPoints);

        int whiteGainedPoints = 0;
        int blackGainedPoints = 0;

        if ((whiteAttackers == 0 && whiteToMove) || (blackAttackers == 0 && !whiteToMove)) {
            return null;
        }

        if (whiteToMove) {
            if (piece.isWhite()) {
                return null;
            } else {
                return getTradeType(piece, whiteAttackingPoints, blackAttackingPoints, whiteGainedPoints, blackGainedPoints, true);
            }
        } else {
            if (piece.isWhite()) {
                return getTradeType(piece, whiteAttackingPoints, blackAttackingPoints, whiteGainedPoints, blackGainedPoints, false);
            } else {
                return null;
            }
        }
    }

    private int[] insert(int[] points, int toInsert) {
        List<Integer> newPoints = new ArrayList<>();
        for (int point : points) {
            newPoints.add(point);
        }
        newPoints.add(toInsert);

        int[] result = new int[newPoints.size()];

        for (int i = 0; i < newPoints.size(); i++) {
            result[i] = newPoints.get(i);
        }

        Arrays.sort(result);
        return result;
    }

    private TradeType getTradeType(Piece piece, int[] whiteAttackingPoints, int[] blackAttackingPoints, int whiteGainedPoints, int blackGainedPoints, boolean whiteToMove) {
        if (whiteToMove) { // fixme TODO
            if (!piece.isWhite()) {
                blackAttackingPoints = insert(blackAttackingPoints, piece.points());
            }
        } else {
            if (piece.isWhite()) {
                whiteAttackingPoints = insert(whiteAttackingPoints, piece.points());
            }
        }
        if (whiteAttackingPoints.length == blackAttackingPoints.length) {
            for (int i = 0; i < whiteAttackingPoints.length; i++) {
                whiteGainedPoints += blackAttackingPoints[i];
                blackGainedPoints += whiteAttackingPoints[i];
            }
        } else {
            if (whiteAttackingPoints.length > blackAttackingPoints.length) {
                for (int i = 0; i < blackAttackingPoints.length; i++) {
                    whiteGainedPoints += blackAttackingPoints[i];
                    blackGainedPoints += whiteAttackingPoints[i];
                }
            } else {
                for (int i = 0; i < whiteAttackingPoints.length; i++) {
                    blackGainedPoints += whiteAttackingPoints[i];
                    whiteGainedPoints += blackAttackingPoints[i];
                }
            }
        }

        System.out.println();
        System.out.println(Arrays.toString(whiteAttackingPoints));
        System.out.println(Arrays.toString(blackAttackingPoints));
        System.out.println(whiteGainedPoints);
        System.out.println(blackGainedPoints);
        System.out.println();

        if (whiteGainedPoints == blackGainedPoints) {
            return TradeType.EQUAL;
        } else {
            if (whiteGainedPoints > blackGainedPoints) {
                return TradeType.WHITE;
            } else {
                return TradeType.BLACK;
            }
        }
    }

    public boolean isHanging(Piece[][] board, BoardCoordinate tile) {
        Piece piece = board[tile.row()][tile.col()];
        int defenders = attackers(board, tile, piece.isWhite());
        int attackers = attackers(board, tile, !piece.isWhite());
        return defenders == 0 && attackers > 0;
    }

    public int attackers(Piece[][] board, BoardCoordinate tile, boolean white) {
        Piece[][] newBoard = Arrays.stream(board).map(Piece[]::clone).toArray(Piece[][]::new);
        Piece piece = newBoard[tile.row()][tile.col()];
        return getAttackers(newBoard, piece, tile, white).size();
    }

    public int[] attackingPoints(Piece[][] board, BoardCoordinate tile, boolean white) {
        Piece[][] newBoard = Arrays.stream(board).map(Piece[]::clone).toArray(Piece[][]::new);
        Piece piece = newBoard[tile.row()][tile.col()];

        List<Move.SimpleMove> moves = getAttackers(newBoard, piece, tile, white);
        int[] points = new int[moves.size()];
        for (int i = 0; i < moves.size(); i++) {
            points[i] = moves.get(i).getPiece().points();
        }
        return points;
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
        if (piece == null || coordinate == null) {
            return moves;
        }
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                BoardCoordinate possibleTile = new BoardCoordinate(i, j);
                BoardCoordinate epTake = getEnpassantTakeSquare();
                if (getEnpassantTargetSquare() != null && board[epTake.row()][epTake.col()].isWhite() != piece.isWhite() && possibleTile.equals(getEnpassantTargetSquare()) && piece instanceof Pawn && piece.validMove(coordinate, possibleTile, true)) {
                    if (kingAvoidsCheck(board, piece, getEnpassantTakeSquare(), coordinate, possibleTile)) {
                        Piece[][] newBoard = moveResult(board, epTake, coordinate, possibleTile);

                        BoardCoordinate epSQt = getEnpassantTakeSquare();
                        Piece taken = board[epSQt.row()][epSQt.col()];

                        Move.Check check = null;

                        if (!isCheckingForMate && kingIsInCheck(newBoard, !piece.isWhite())) {
                            check = Move.Check.CHECK;

                            if (kingIsCheckmated(newBoard, !piece.isWhite())) {
                                check = Move.Check.MATE;
                            }
                        }
                        moves.add(new Move(piece, coordinate, possibleTile, check, true, epSQt, taken, null, null));
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
                            moves.add(new Move(piece, kingFrom, kingTo, check, false, null, null, null, Move.CastleType.KINGSIDE));
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

                            moves.add(new Move(piece, kingFrom, kingTo, check, false, null, null, null, Move.CastleType.QUEENSIDE));
                        }
                    }
                } else {
                    Piece taken = board[possibleTile.row()][possibleTile.col()];
                    boolean capture = taken != null;
                    if (piece.validMove(coordinate, possibleTile, capture) && (!capture || (taken.isWhite() != piece.isWhite())) && notBlocked(board, coordinate, possibleTile)) {
                        if (kingAvoidsCheck(board, piece, possibleTile, coordinate, possibleTile)) {
                            Piece[][] newBoard = moveResult(board, possibleTile, coordinate, possibleTile);
                            Move.Check check = null;
                            if (!isCheckingForMate && kingIsInCheck(newBoard, !piece.isWhite())) {
                                check = Move.Check.CHECK;
                                if (kingIsCheckmated(newBoard, !piece.isWhite())) {
                                    check = Move.Check.MATE;
                                }
                            }
                            moves.add(new Move(piece, coordinate, possibleTile, check, false, possibleTile, taken, null, null));
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
        List<Move> moves = getAllValidMoves(board, white, true);
        for (Move move : moves) {
            if (kingAvoidsCheck(board, move.getPiece(), move.getTakeSquare(), move.getFrom(), move.getTo())) {
                return false;
            }
        }
        return true;
    }

    public List<Move> getAllValidMoves(Piece[][] board, boolean white, boolean isCheckingForMate) {
        List<Move> allMoves = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
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
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                BoardCoordinate from = new BoardCoordinate(i, j);
                Piece pieceFrom = board[i][j];
                if (pieceFrom != null && !from.equals(tile)) {
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
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
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
        BoardMatrixRotation.printBoard(gamePieces, view);
        if (includeInfo) {
            System.out.println();
            System.out.println("FEN String: " + translateBoardToFEN(gamePieces));
            if (!(history == null || history.isEmpty())) {
                Move lastMove = history.get(history.size() - 1);
                System.out.println("Last move: " + (lastMove.isImport() ? "Unknown" : lastMove.toString()));
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
        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
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

        Move.Check check = null;
        if (kingIsInCheck(gamePieces, !piece.isWhite())) {
            check = Move.Check.CHECK;

            if (kingIsCheckmated(gamePieces, !piece.isWhite())) {
                check = Move.Check.MATE;
            }
        }

        Move move = new Move(piece, from, to, check, !takes.equals(to), takes, taken,null, null); // TODO

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

        Move move = new Move(king, kingFrom, kingTo, check, false, null, null, null, type);
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

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                if (highlightedSquares[i][j] == Move.MoveHighlights.SELECTED) {
                    highlightedSquares[i][j] = null;
                }
            }
        }
    }

    public boolean isStaleMate(Piece[][] board) {
        List<Move> moves = getAllValidMoves(board, whiteTurn, false);
        return moves.isEmpty();
    }

    public boolean isDrawByRepetition() {
        for (String s : gameFENHistory) {
            int c = 0;
            for (String s1 : gameFENHistory) {
                if (s.equals(s1)) {
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
        return result;
    }

    public boolean isDrawByInsufficientMaterial(String truncated) {
        String k_vs_KN = anagramRegex("kKN");
        String kn_vs_K = anagramRegex("knK");
        String k_vs_KB = anagramRegex("kKB");
        String kb_vs_K = anagramRegex("kbK");

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
            for (int i = 0; i < 8; i++) {
                for (int j = 0; j < 8; j++) {
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

    public void displayIcon(Graphics g, BoardCoordinate coordinate, Move.MoveHighlights icon) {
        if (coordinate.row() == 0 && view == BoardView.BLACK || coordinate.row() == 7 && view == BoardView.WHITE) {
            g.drawImage(Objects.requireNonNull(Move.getMoveHighlightIcon(icon)).getScaledInstance(pieceSize / 3, pieceSize / 3, Image.SCALE_SMOOTH), (coordinate.col() * pieceSize) + (pieceSize - (pieceSize / 3) + (pieceSize / 20)), (coordinate.row() * pieceSize), this);
        } else {
            g.drawImage(Objects.requireNonNull(Move.getMoveHighlightIcon(icon)).getScaledInstance(pieceSize / 3, pieceSize / 3, Image.SCALE_SMOOTH), (coordinate.col() * pieceSize) + (pieceSize - (pieceSize / 3) + (pieceSize / 20)), (coordinate.row() * pieceSize) - (pieceSize / 20), this);
        }
    }

    public void displayIcon(Graphics g, BoardCoordinate coordinate, Move.InfoIcons icon) {
        if (coordinate.row() == 0 && view == BoardView.BLACK || coordinate.row() == 7 && view == BoardView.WHITE) {
            g.drawImage(Objects.requireNonNull(Move.getInfoIcon(icon)).getScaledInstance(pieceSize / 3, pieceSize / 3, Image.SCALE_SMOOTH), (coordinate.col() * pieceSize) + (pieceSize - (pieceSize / 3) + (pieceSize / 20)), (coordinate.row() * pieceSize), this);
        } else {
            g.drawImage(Objects.requireNonNull(Move.getInfoIcon(icon)).getScaledInstance(pieceSize / 3, pieceSize / 3, Image.SCALE_SMOOTH), (coordinate.col() * pieceSize) + (pieceSize - (pieceSize / 3) + (pieceSize / 20)), (coordinate.row() * pieceSize) - (pieceSize / 20), this);
        }
    }

    public void createGameOverScreen(GameOverType type) {
        BoardCoordinate whiteKing = getKing(getSide(true, gamePieces));
        BoardCoordinate blackKing = getKing(getSide(false, gamePieces));
        switch (type) {
            case CHECKMATE_BLACK -> {
                highlightedSquares[whiteKing.row()][whiteKing.col()] = Move.MoveHighlights.CHECKMATE;
                gameHighlights[whiteKing.row()][whiteKing.col()] = Move.InfoIcons.CHECKMATE_WHITE;
                gameHighlights[blackKing.row()][blackKing.col()] = Move.InfoIcons.WINNER;
            }
            case CHECKMATE_WHITE -> {
                highlightedSquares[blackKing.row()][blackKing.col()] = Move.MoveHighlights.CHECKMATE;
                gameHighlights[blackKing.row()][blackKing.col()] = Move.InfoIcons.CHECKMATE_BLACK;
                gameHighlights[whiteKing.row()][whiteKing.col()] = Move.InfoIcons.WINNER;
            }
            case STALEMATE, ILLEGAL_POSITION, INSUFFICIENT_MATERIAL, DRAW_BY_REPETITION, FIFTY_MOVE_RULE, DRAW_BY_AGREEMENT -> {
                gameHighlights[whiteKing.row()][whiteKing.col()] = Move.InfoIcons.DRAW_WHITE;
                gameHighlights[blackKing.row()][blackKing.col()] = Move.InfoIcons.DRAW_BLACK;
            }
            case RESIGNATION_BLACK -> {
                gameHighlights[blackKing.row()][blackKing.col()] = Move.InfoIcons.RESIGN_BLACK;
                gameHighlights[whiteKing.row()][whiteKing.col()] = Move.InfoIcons.WINNER;
            }
            case RESIGNATION_WHITE -> {
                gameHighlights[whiteKing.row()][whiteKing.col()] = Move.InfoIcons.RESIGN_WHITE;
                gameHighlights[blackKing.row()][blackKing.col()] = Move.InfoIcons.WINNER;
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
        }
    }

    public GameOverType gameOver() {
        if (kingIsCheckmated(gamePieces, playAsWhite)) {
            return playAsWhite ? GameOverType.CHECKMATE_BLACK : GameOverType.CHECKMATE_WHITE;
        }
        if (kingIsCheckmated(gamePieces, !playAsWhite)) {
            return playAsWhite ? GameOverType.CHECKMATE_WHITE : GameOverType.CHECKMATE_BLACK;
        }
        if (fiftyMoveRule >= 50) {
            return GameOverType.FIFTY_MOVE_RULE;
        }
        if (isStaleMate(gamePieces)) {
            return GameOverType.STALEMATE;
        }
        if (isDrawByRepetition()) {
            return GameOverType.DRAW_BY_REPETITION;
        }
        if (isDrawByInsufficientMaterial(simplifyFEN(translateBoardToFEN(gamePieces)))) {
            return GameOverType.INSUFFICIENT_MATERIAL;
        }
        return null;
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
        removeHighlights(Move.MoveHighlights.CHECK);
        if (kingIsInCheck(gamePieces, true)) {
            BoardCoordinate king = getKing(getSide(true, gamePieces));
            highlightedSquares[king.row()][king.col()] = Move.MoveHighlights.CHECK;
        }
        if (kingIsInCheck(gamePieces, false)) {
            BoardCoordinate king = getKing(getSide(false, gamePieces));
            highlightedSquares[king.row()][king.col()] = Move.MoveHighlights.CHECK;
        }
    }

    public void endComputerTurn() {
        gameOver = gameOver();
        if (gameOver != null) {
            createGameOverScreen(gameOver);
        } else {
            highlightChecks();
            displayPieces();
            repaint();
        }
    }

    public void endTurn() {
        deselect();

        gameOver = gameOver();
        if (gameOver != null) {
            createGameOverScreen(gameOver);
        } else {
            highlightChecks();
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
                }, 500);
                case AUTO_SWAP -> new Timer().schedule(new TimerTask() {
                    @Override
                    public void run() {
                        swapPlaySide();
                    }
                }, 500);
            }
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
                displayPieces();
                repaint();
            }

            /*
            if (whiteTurn == white && whiteTurn == playAsWhite) {
                if (getEnpassantTargetSquare() != null && gamePieces[epSQtake.row()][epSQtake.col()].isWhite() != piece.isWhite() && to.equals(getEnpassantTargetSquare()) && piece instanceof Pawn && piece.validMove(from, to, true)) {
                    if (kingAvoidsCheck(gamePieces, piece, getEnpassantTakeSquare(), from, to)) {
                        moveHighlight(from, to);
                        rawMove(piece, epSQtake, from, to);
                        endTurn();
                    }
                } else if (piece instanceof King && getRook(gamePieces, piece.isWhite(), Move.CastleType.KINGSIDE) != null && getAllCastleSquares(piece.isWhite(), Move.CastleType.KINGSIDE).contains(to) && (piece.isWhite() ? whiteCanCastleKingside : blackCanCastleKingside)) {
                    if (!kingIsInCheck(castleResult(piece.isWhite(), Move.CastleType.KINGSIDE), piece.isWhite())) {
                        if (canCastle(piece.isWhite(), Move.CastleType.KINGSIDE)) {
                            moveHighlight(from, to);
                            rawCastle(piece.isWhite(), Move.CastleType.KINGSIDE);
                            endTurn();
                        }
                    }
                } else if (piece instanceof King && getRook(gamePieces, piece.isWhite(), Move.CastleType.QUEENSIDE) != null && getAllCastleSquares(piece.isWhite(), Move.CastleType.QUEENSIDE).contains(to) && (piece.isWhite() ? whiteCanCastleQueenside : blackCanCastleQueenside)) {
                    if (!kingIsInCheck(castleResult(piece.isWhite(), Move.CastleType.QUEENSIDE), piece.isWhite())) {
                        if (canCastle(piece.isWhite(), Move.CastleType.QUEENSIDE)) {
                            moveHighlight(from, to);
                            rawCastle(piece.isWhite(), Move.CastleType.QUEENSIDE);
                            endTurn();
                        }
                    }
                } else {
                    Piece taken = gamePieces[to.row()][to.col()];
                    boolean capture = taken != null;

                    if (piece.validMove(from, to, capture) && (!capture || (taken.isWhite() != piece.isWhite())) && notBlocked(gamePieces, from, to)) {
                        if (kingAvoidsCheck(gamePieces, piece, to, from, to)) {
                            moveHighlight(from, to);
                            rawMove(piece, to, from, to);
                            endTurn();
                        }
                    }
                }
            }
             */
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
}
