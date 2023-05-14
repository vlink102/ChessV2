package me.vlink102.personal.chess.classroom;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.internal.BoardCoordinate;
import me.vlink102.personal.chess.internal.ClassroomAssets;
import me.vlink102.personal.chess.internal.Move;
import me.vlink102.personal.chess.pieces.Piece;
import me.vlink102.personal.chess.pieces.generic.*;
import me.vlink102.personal.chess.pieces.special.asian.DragonHorse;
import me.vlink102.personal.chess.pieces.special.asian.DragonKing;
import me.vlink102.personal.chess.pieces.special.historical.*;
import me.vlink102.personal.chess.ui.interactive.ClassroomInteraction;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Objects;

public class ClassroomGUI extends JPanel {
    private final Classroom classRoom;
    private final boolean useOnline;

    private final int boardSize;
    private int pieceSize;

    private Piece[][] gamePieces;

    private BoardGUI.PieceDesign pieceTheme;
    private BoardGUI.Colours boardTheme;

    private final boolean playAsWhite;
    private BoardGUI.BoardView view;

    private final ClassroomAssets classroomAssets;

    public ClassroomGUI(Classroom classRoom, int pieceSize, int boardSize, boolean useOnline, boolean playAsWhite, BoardGUI.PieceDesign pieceTheme, BoardGUI.Colours boardTheme) {
        this.classRoom = classRoom;
        this.pieceSize = pieceSize;
        this.boardSize = boardSize;
        if (boardSize != 8) {
            this.useOnline = false;
        } else {
            this.useOnline = useOnline;
        }
        this.playAsWhite = playAsWhite;
        this.pieceTheme = pieceTheme;
        this.boardTheme = boardTheme;
        this.classroomAssets = new ClassroomAssets(this);

        setupBoard();

        ClassroomInteraction classroomInteraction = new ClassroomInteraction(classRoom, this);
        addMouseListener(classroomInteraction);
        addMouseMotionListener(classroomInteraction);

        registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_OPEN_BRACKET, 0, false), "black-view", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setView(BoardGUI.BoardView.BLACK);
                displayPieces();
                repaint();
            }
        });
        registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_CLOSE_BRACKET, 0, false), "white-view", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setView(BoardGUI.BoardView.WHITE);
                displayPieces();
                repaint();
            }
        });
        registerKeyBinding(KeyStroke.getKeyStroke(KeyEvent.VK_EQUALS, 0, false), "default-view", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setView(playAsWhite ? BoardGUI.BoardView.WHITE : BoardGUI.BoardView.BLACK);
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

        classroomAssets.updatePieceDesigns(this);
    }

    public void registerKeyBinding(KeyStroke keyStroke, String name, Action action) {
        InputMap im = classRoom.getInputMap(WHEN_IN_FOCUSED_WINDOW);
        ActionMap am = classRoom.getActionMap();

        im.put(keyStroke, name);
        am.put(name, action);
    }


    public void setupBoard() {
        this.view = playAsWhite ? BoardGUI.BoardView.WHITE : BoardGUI.BoardView.BLACK;
        this.gamePieces = new Piece[boardSize][boardSize];

        setupDefaultBoard(gamePieces, true);
        setupDefaultBoard(gamePieces, false);
    }

    public void displayPieces() {
        for (int row = 0; row < boardSize; row++) {
            for (int col = 0; col < boardSize; col++) {
                JPanel panel = (JPanel) getComponent((row * boardSize) + col);
                int r1 = view == BoardGUI.BoardView.WHITE ? (boardSize - 1) - row : row;
                int c1 = view == BoardGUI.BoardView.WHITE ? col : (boardSize - 1) - col;

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
        int backLine = white ? 0 : (boardSize - 1);

        if (boardSize >= 8) {
            int startingPoint = (boardSize / 2) - 4;
            for (int i = startingPoint; i < startingPoint + 8; i++) {
                board[white ? 1 : (boardSize - 1) - 1][i] = new Pawn(this, white);
            }

            board[backLine][startingPoint] = new Rook(this, white, new BoardCoordinate(backLine, startingPoint, this));
            board[backLine][startingPoint + 7] = new Rook(this, white, new BoardCoordinate(backLine, startingPoint + 7, this));

            board[backLine][startingPoint + 1] = new Knight(this, white);
            board[backLine][startingPoint + 6] = new Knight(this, white);

            board[backLine][startingPoint + 2] = new Bishop(this, white);
            board[backLine][startingPoint + 5] = new Bishop(this, white);

            board[backLine][startingPoint + 3] = new Queen(this, white);
            board[backLine][startingPoint + 4] = new King(this, white);
        } else {
            for (int i = 0; i < boardSize; i++) {
                board[white ? 1 : (boardSize - 1) - 1][i] = new Pawn(this, white);
            }
            switch (boardSize) {
                case 4 -> {
                    board[backLine][0] = new Rook(this, white, new BoardCoordinate(backLine, 0, this));
                    board[backLine][1] = new Queen(this, white);
                    board[backLine][2] = new King(this, white);
                    board[backLine][3] = new Knight(this, white);
                }
                case 5 -> {
                    board[backLine][0] = new Rook(this, white, new BoardCoordinate(backLine, 0, this));
                    board[backLine][1] = new Queen(this, white);
                    board[backLine][2] = new King(this, white);
                    board[backLine][3] = new Knight(this, white);
                    board[backLine][4] = new Rook(this, white, new BoardCoordinate(backLine, 4, this));
                }
                case 6 -> {
                    board[backLine][0] = new Rook(this, white, new BoardCoordinate(backLine, 0, this));
                    board[backLine][1] = new Bishop(this, white);
                    board[backLine][2] = new Queen(this, white);
                    board[backLine][3] = new King(this, white);
                    board[backLine][4] = new Knight(this, white);
                    board[backLine][5] = new Rook(this, white, new BoardCoordinate(backLine, 5, this));
                }
                case 7 -> {
                    board[backLine][0] = new Rook(this, white, new BoardCoordinate(backLine, 0, this));
                    board[backLine][1] = new Knight(this, white);
                    board[backLine][2] = new Bishop(this, white);
                    board[backLine][3] = new Queen(this, white);
                    board[backLine][4] = new King(this, white);
                    board[backLine][5] = new Knight(this, white);
                    board[backLine][6] = new Rook(this, white, new BoardCoordinate(backLine, 6, this));
                }
            }
        }
    }

    public void movePiece(BoardCoordinate from, BoardCoordinate to) {
        if (gamePieces[from.row()][from.col()] != null) {
            Piece moved = gamePieces[from.row()][from.col()];
            gamePieces[from.row()][from.col()] = null;
            gamePieces[to.row()][to.col()] = moved;
            displayPieces();
        }
    }

    public int getBoardSize() {
        return boardSize;
    }

    public int getPieceSize() {
        return pieceSize;
    }

    public Piece[][] getGamePieces() {
        return gamePieces;
    }

    public BoardGUI.PieceDesign getPieceTheme() {
        return pieceTheme;
    }

    public BoardGUI.Colours getBoardTheme() {
        return boardTheme;
    }

    public BoardGUI.BoardView getView() {
        return view;
    }

    public void setView(BoardGUI.BoardView view) {
        this.view = view;
    }

    public void setPieceSize(int pieceSize) {
        this.pieceSize = pieceSize;
    }

    public void setBoardTheme(BoardGUI.Colours boardTheme) {
        this.boardTheme = boardTheme;
        classroomAssets.updateSavedImage(this);
        repaint();
    }

    public void setPieceTheme(BoardGUI.PieceDesign pieceTheme) {
        this.pieceTheme = pieceTheme;
        classroomAssets.updatePieceDesigns(this);
        displayPieces();
        repaint();
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (useOnline) {
            Image image = classroomAssets.getSavedBoard();
            g.drawImage(image, 0, 0, this);
        }

        for (int i = 0; i < boardSize; i++) {
            for (int j = 0; j < boardSize; j++) {
                int r1 = view == BoardGUI.BoardView.WHITE ? (boardSize - 1) - i : i;
                int c1 = view == BoardGUI.BoardView.WHITE ? j : (boardSize - 1) - j;

                if (!useOnline) {
                    Graphics2D g2d = (Graphics2D) g.create();
                    g2d.setColor((i + j) % 2 == 0 ? boardTheme.getScheme().dark() : boardTheme.getScheme().light());
                    g2d.fillRect((c1) * pieceSize, (r1) * pieceSize, pieceSize, pieceSize);
                    g2d.dispose();
                }


            }
        }
    }

    public String translateBoardToFEN(Piece[][] board) {
        return BoardGUI.translateBoard(board, boardSize).toString();
    }

    public static Piece[][] generateBoard(String FEN, int boardSize, BoardGUI boardGUI) {
        Piece[][] gamePieces = new Piece[boardSize][boardSize];
        String[] board = FEN.split("/");

        for (int rank = 0; rank < boardSize; rank++) {
            String row = board[(boardSize - 1) - rank];
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
                    case "r" -> gamePieces[rank][i] = new Rook(boardGUI, isUpper, new BoardCoordinate(rank, i, boardGUI));
                    case "n" -> gamePieces[rank][i] = new Knight(boardGUI, isUpper);
                    case "b" -> gamePieces[rank][i] = new Bishop(boardGUI, isUpper);
                    case "q" -> gamePieces[rank][i] = new Queen(boardGUI, isUpper);
                    case "k" -> gamePieces[rank][i] = new King(boardGUI, isUpper);
                    case "p" -> gamePieces[rank][i] = new Pawn(boardGUI, isUpper);
                    case " " -> gamePieces[rank][i] = null;
                    case "a" -> gamePieces[rank][i] = new Amazon(boardGUI, isUpper);
                    case "c" -> gamePieces[rank][i] = new Camel(boardGUI, isUpper);
                    case "e" -> gamePieces[rank][i] = new Elephant(boardGUI, isUpper);
                    case "s" -> gamePieces[rank][i] = new Princess(boardGUI, isUpper);
                    case "m" -> gamePieces[rank][i] = new Man(boardGUI, isUpper);
                    case "i" -> gamePieces[rank][i] = new Minister(boardGUI, isUpper);
                    case "h" -> gamePieces[rank][i] = new Empress(boardGUI, isUpper);
                    case "y" -> gamePieces[rank][i] = new DragonKing(boardGUI, isUpper);
                    case "u" -> gamePieces[rank][i] = new DragonHorse(boardGUI, isUpper);
                }
            }
        }
        return gamePieces;
    }

    public void loadFEN(String FEN) {
        if (FEN.split(" ").length > 0) {
            FEN = FEN.split(" ")[0];
        }
        if (BoardGUI.validateBoard(FEN, boardSize)) {
            gamePieces = new Piece[boardSize][boardSize];
            String[] board = FEN.split("/");

            for (int rank = 0; rank < boardSize; rank++) {
                String row = board[(boardSize - 1) - rank];
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
                        case "r" -> gamePieces[rank][i] = new Rook(this, isUpper, new BoardCoordinate(rank, i, this));
                        case "n" -> gamePieces[rank][i] = new Knight(this, isUpper);
                        case "b" -> gamePieces[rank][i] = new Bishop(this, isUpper);
                        case "q" -> gamePieces[rank][i] = new Queen(this, isUpper);
                        case "k" -> gamePieces[rank][i] = new King(this, isUpper);
                        case "p" -> gamePieces[rank][i] = new Pawn(this, isUpper);
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

            displayPieces();
            repaint();

            createPopUp("Successfully loaded FEN string: " + FEN, "Loaded board", Move.MoveHighlights.EXCELLENT);
        } else {
            String result = BoardGUI.fixFENBoard(FEN, boardSize, true);
            if (result == null) {
                createPopUp("Invalid FEN String: " + FEN + "\n\nFix Failed:\n - Wrong number of Kings\n\n", "FEN import cancelled", Move.MoveHighlights.MISTAKE);
            } else {
                if (!BoardGUI.validateBoard(result, boardSize)) {
                    createPopUp("Invalid FEN String: " + FEN + "\n\nFix Failed:\n - Could not repair FEN String\n\n" + result, "FEN import cancelled", Move.MoveHighlights.BLUNDER);
                } else {
                    createPopUp("Invalid FEN String: " + FEN + "\n\nFix Successful:\n - Repaired broken FEN String\n\n" + result, "Game state repaired", Move.MoveHighlights.EXCELLENT);
                    loadFEN(result);
                }
            }
        }
    }

    public void resetBoard() {
        this.gamePieces = new Piece[boardSize][boardSize];
        setupBoard();
        repaint();
        displayPieces();
    }

    public void createPopUp(String message, String title, Move.MoveHighlights highlights) {
        ImageIcon imageIcon = new ImageIcon(Objects.requireNonNull(Move.getMoveHighlightIcon(highlights)).getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        JOptionPane.showConfirmDialog(this, message, title,  JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, imageIcon);
        requestFocus();
    }

    public void createPopUp(String message, String title, Move.InfoIcons highlights) {
        ImageIcon imageIcon = new ImageIcon(Objects.requireNonNull(Move.getInfoIcon(highlights)).getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        JOptionPane.showConfirmDialog(this, message, title,  JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, imageIcon);
        requestFocus();
    }

    public ClassroomAssets getClassroomAssets() {
        return classroomAssets;
    }
}
