package me.vlink102.personal.chess;

import me.vlink102.personal.chess.pieces.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BoardGUI extends JPanel {
    private final Chess chess;

    private final OnlineAssets onlineAssets;
    private boolean useOnline;
    private PieceDesign pieceTheme;
    private Colours boardTheme;
    private int pieceSize;
    private final Piece[][] gamePieces;
    private Move.Highlights[][] highlightedSquares;
    private final boolean playAsWhite;
    private BoardView view;
    private int dimension;

    private boolean whiteTurn;
    private int halfMoveClock;
    private int fullMoveCount;
    private boolean whiteCanCastleQueenside;
    private boolean whiteCanCastleKingside;
    private boolean blackCanCastleQueenside;
    private boolean blackCanCastleKingside;

    private List<Move> history;

    public enum BoardView {
        BLACK,
        WHITE
    }

    public enum MoveHighlightState {
        MOVE_HIDDEN,
        MOVE_VISIBLE,
        SELECTED
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
        fen.append(getEnpassantTargetSquare());
        fen.append(" ");
        fen.append(halfMoveClock);
        fen.append(" ");
        fen.append(fullMoveCount);
        return fen.toString();
    }

    public String getEnpassantTargetSquare() {
        if (history == null || !history.isEmpty()) {
            Move lastMove = history.get(history.size() - 1);
            if (lastMove.getPiece() instanceof Pawn pawn) {
                if (pawn.isWhite()) {
                    return new BoardCoordinate(lastMove.getTo().getRow() - 1, lastMove.getTo().getCol()).toNotation();
                } else {
                    return new BoardCoordinate(lastMove.getTo().getRow() + 1, lastMove.getTo().getCol()).toNotation();
                }
            }
        }
        return "-";
    }

    public BoardGUI(Chess chess, PieceDesign pieceTheme, Colours boardTheme, int pSz, boolean useOnline, boolean playAsWhite) {
        this.chess = chess;
        this.pieceTheme = pieceTheme;
        this.boardTheme = boardTheme;
        this.pieceSize = pSz;
        this.onlineAssets = new OnlineAssets(this);
        this.useOnline = useOnline;
        this.dimension = pieceSize * 8;
        this.playAsWhite = playAsWhite;
        this.view = playAsWhite ? BoardView.WHITE : BoardView.BLACK;
        this.gamePieces = new Piece[8][8];
        this.highlightedSquares = new Move.Highlights[8][8];

        this.halfMoveClock = 0;
        this.fullMoveCount = 1;

        this.whiteTurn = true;
        this.whiteCanCastleKingside = true;
        this.whiteCanCastleQueenside = true;
        this.blackCanCastleKingside = true;
        this.blackCanCastleQueenside = true;

        this.history = new ArrayList<>();

        setupBoard(true);
        setupBoard(false);
        addMouseListener(highlightListener());

        PieceInteraction interaction = new PieceInteraction(chess, this);
        addMouseListener(interaction);
        addMouseMotionListener(interaction);
        addKeyListener(boardViewListener());

        setLayout(new GridLayout(8, 8));
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                JPanel square = new JPanel(new BorderLayout());
                square.setOpaque(false);
                add(square);
            }
        }
    }


    public void displayPieces() {
        for (int row = 0; row < 8; row++) {
            for (int col = 0; col < 8; col++) {
                JPanel panel = (JPanel) getComponent((row * 8) + col);
                switch (view) {
                    case WHITE -> {
                        if (panel.getComponents().length > 0) {
                            panel.removeAll();
                        }
                        if (gamePieces[7 - row][col] != null) {
                            panel.add(new JLabel(new ImageIcon(gamePieces[7 - row][col].getIcon(pieceSize))));
                        }
                        panel.validate();
                    }
                    case BLACK -> {
                        if (panel.getComponents().length > 0) {
                            panel.removeAll();
                        }
                        if (gamePieces[row][7 - col] != null) {
                            panel.add(new JLabel(new ImageIcon(gamePieces[row][7 - col].getIcon(pieceSize))));
                        }
                        panel.validate();
                    }
                }
            }
        }
    }


    public void setupBoard(boolean white) {
        int backLine = white ? 0 : 7;
        for (int i = 0; i < 8; i++) {
            gamePieces[white ? 1 : 6][i] = new Pawn(this, white);
        }

        gamePieces[backLine][0] = new Rook(this, white, Move.CastleType.QUEENSIDE);
        gamePieces[backLine][7] = new Rook(this, white, Move.CastleType.KINGSIDE);

        gamePieces[backLine][1] = new Knight(this, white);
        gamePieces[backLine][6] = new Knight(this, white);

        gamePieces[backLine][2] = new Bishop(this, white);
        gamePieces[backLine][5] = new Bishop(this, white);

        gamePieces[backLine][3] = new Queen(this, white);
        gamePieces[backLine][4] = new King(this, white);
    }

    public KeyListener boardViewListener() {
        return new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                char c = e.getKeyChar();
                switch (e.getKeyChar()) {
                    case '[' -> setView(BoardView.BLACK);
                    case ']' -> setView(BoardView.WHITE);
                    case '=' -> setView(playAsWhite ? BoardView.WHITE : BoardView.BLACK);
                }
                if (c == '-' || c == '=' || c == '[' || c == ']') {
                    displayPieces();
                    repaint();
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
            }
        };
    }

    public MouseListener highlightListener() {
        return new MouseListener() {
            int x0h;
            int y0h;

            @Override
            public void mousePressed(MouseEvent e) { // TODO left click highlights square with piece
                if (e.getButton() == MouseEvent.BUTTON3) {
                    x0h = e.getX();
                    y0h = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3) {
                    int x1 = e.getX();
                    int y1 = e.getY();

                    int f0 = x0h / pieceSize;
                    int r0 = y0h / pieceSize;
                    int f1 = x1 / pieceSize;
                    int r1 = y1 / pieceSize;

                    if (f0 == f1 && r0 == r1) {
                        BoardCoordinate coordinate = new BoardCoordinate(r1, f1);
                        highlight(coordinate.getCol(), coordinate.getRow(), Move.Highlights.HIGHLIGHT);
                    } else {
                        x0h = 0;
                        y0h = 0;
                    }
                    repaint();
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

    public void highlight(int file, int rank, Move.Highlights type) {
        if (type == highlightedSquares[rank][file]) {
            highlightedSquares[rank][file] = null;
        } else {
            highlightedSquares[rank][file] = type;
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
        ColorScheme scheme = boardTheme.getScheme();

        if (useOnline) {
            Image image = OnlineAssets.getSavedBoard().getScaledInstance(pieceSize * 8, pieceSize * 8, Image.SCALE_FAST);
            /*
            if (view == BoardView.SIDE || view == BoardView.SIDE2) {
                double degrees = Math.toRadians(view == BoardView.SIDE ? 90 : -90);
                double locX = image.getWidth(null) / 2d;
                double locY = image.getHeight(null) / 2d;
                AffineTransform transform = AffineTransform.getRotateInstance(degrees, locX, locY);
                ((Graphics2D)g).drawImage(image, transform, null);
            } else {
            }
            */
            g.drawImage(image, 0, 0, null);
        }


        switch (view) {
            case BLACK -> {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        g.drawString(new BoardCoordinate(row, 7 - col).toNotation(), ((col * pieceSize) + (pieceSize / 2)), (row * pieceSize) + (pieceSize/ 2));
                    }
                }
            }

            case WHITE -> {
                for (int row = 0; row < 8; row++) {
                    for (int col = 0; col < 8; col++) {
                        g.drawString(new BoardCoordinate(7 - row, col).toNotation(), ((col * pieceSize) + (pieceSize / 2)), (row * pieceSize) + (pieceSize/ 2));
                    }
                }
            }
        }
        displayBoard(g, scheme);
    }

    public void displayBoard(Graphics g, ColorScheme scheme) {
        switch (view) {
            case BLACK -> {
                for (int row = 0; row < 8; row++) {
                    for (int col = 7; col >= 0; col--) {
                        drawBoardTile(g, scheme, row, col);
                        g.setColor(((col + row) % 2 == 0 ? scheme.dark() : scheme.light()));
                    }
                }
            }

            case WHITE -> {
                for (int row = 7; row >= 0; row--) {
                    for (int col = 0; col < 8; col++) {
                        drawBoardTile(g, scheme, row, col);
                        g.setColor(((col + row) % 2 == 0 ? scheme.dark() : scheme.light()));
                    }
                }
            }
        }
    }

    public void drawBoardTile(Graphics g, ColorScheme scheme, int row, int col) {
        g.setColor(((col + row) % 2 == 0 ? scheme.light() : scheme.dark()));

        // todo fix highlighting

        if (useOnline) {
            if (highlightedSquares[row][col] != null) {
                g.setColor(scheme.getHighlight(highlightedSquares[row][col]));
                g.fillRect((col) * pieceSize, (row) * pieceSize, pieceSize, pieceSize);
            }
        } else {
            g.fillRect((col) * pieceSize, (row) * pieceSize, pieceSize, pieceSize);
            if (highlightedSquares[row][col] != null) {
                Move.Highlights type = highlightedSquares[row][col];
                Graphics2D g2d = (Graphics2D) g;
                g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, scheme.getHighlight(type).getAlpha() / 255f));
                g.setColor(scheme.getHighlight(type));
                g.fillRect(col * pieceSize, row * pieceSize, pieceSize, pieceSize);
                g2d.setComposite(AlphaComposite.SrcOver);
            }
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

    public void movePiece(RawMove rawMove) {
        if (!rawMove.isSimilar()) {
            Piece temp = gamePieces[rawMove.getFrom().getRow()][rawMove.getFrom().getCol()];
            Piece takes = gamePieces[rawMove.getTo().getRow()][rawMove.getTo().getCol()];

            gamePieces[rawMove.getTo().getRow()][rawMove.getTo().getCol()] = temp;
            gamePieces[rawMove.getFrom().getRow()][rawMove.getFrom().getCol()] = null;

            halfMoveClock++;
            if (!whiteTurn) {
                fullMoveCount++;
            }
            whiteTurn = !whiteTurn;

            Move move = new Move(rawMove, gamePieces[rawMove.getTo().getRow()][rawMove.getTo().getCol()], null /*todo*/, false /*todo*/, takes, null /*todo*/, null /*todo*/);

            history.add(move);

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
        }

    }

    public List<Move> getHistory() {
        return history;
    }
}
