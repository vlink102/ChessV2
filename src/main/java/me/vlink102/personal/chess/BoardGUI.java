package me.vlink102.personal.chess;

import me.vlink102.personal.chess.pieces.*;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.lang.reflect.Array;
import java.util.Arrays;

public class BoardGUI extends JComponent {
    private final OnlineAssets onlineAssets;
    private boolean useOnline;
    private PieceDesign pieceTheme;
    private Colours boardTheme;
    private int pieceSize;
    private final Piece[][] gamePieces; // In raw format
    private final HighlightType[][] highlights;
    private final boolean playAsWhite;
    private BoardView view;
    private int dimension;

    public enum BoardView {
        BLACK,
        WHITE,
        SIDE,
        SIDE2
    }
    public enum HighlightType {
        MOVE,
        HIGHLIGHT
    }

    public BoardGUI(PieceDesign pieceTheme, Colours boardTheme, int pSz, boolean useOnline, boolean playAsWhite) {
        this.pieceTheme = pieceTheme;
        this.boardTheme = boardTheme;
        this.pieceSize = pSz;
        this.onlineAssets = new OnlineAssets(this);
        this.useOnline = useOnline;
        this.dimension = pieceSize * 8;
        this.playAsWhite = playAsWhite;
        this.view = playAsWhite ? BoardView.WHITE : BoardView.BLACK;
        this.gamePieces = new Piece[8][8];
        this.highlights = new HighlightType[8][8];
    }

    public void setupBoard(boolean white) {
        int backLine = white ? 0 : 7;
        for (int i = 0; i < 8; i++) {
            gamePieces[i][white ? 1 : 6] = new Pawn(this, white);
        }

        gamePieces[0][backLine] = new Rook(this, white);
        gamePieces[7][backLine] = new Rook(this, white);

        gamePieces[1][backLine] = new Knight(this, white);
        gamePieces[6][backLine] = new Knight(this, white);

        gamePieces[2][backLine] = new Bishop(this, white);
        gamePieces[5][backLine] = new Bishop(this, white);

        gamePieces[3][backLine] = new Queen(this, white);
        gamePieces[4][backLine] = new King(this, white);
    }

    public KeyListener boardViewListener() {
        return new KeyListener() {
            @Override
            public void keyTyped(KeyEvent e) {}

            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyChar()) {
                    case '-' -> setView(BoardView.SIDE);
                    case '=' -> setView(BoardView.SIDE2);
                    case '[' -> setView(BoardView.BLACK);
                    case ']' -> setView(BoardView.WHITE);
                }
                repaint();
            }

            @Override
            public void keyReleased(KeyEvent e) {
                setView(playAsWhite ? BoardView.WHITE : BoardView.BLACK);
                repaint();
            }
        };
    }
    public MouseListener highlightListener() {
        return new MouseListener() {
            int x0;
            int y0;

            @Override
            public void mousePressed(MouseEvent e) { // TODO left click highlights square with piece
                if (e.getButton() == MouseEvent.BUTTON3 || e.isControlDown()) {
                    x0 = e.getX();
                    y0 = e.getY();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (e.getButton() == MouseEvent.BUTTON3 || e.isControlDown()) {
                    int x1 = e.getX();
                    int y1 = e.getY();

                    int f0 = x0 / pieceSize;
                    int r0 = y0 / pieceSize;
                    int f1 = x1 / pieceSize;
                    int r1 = y1 / pieceSize;

                    if (f0 == f1 && r0 == r1) { // TODO use this for finding move square
                        BoardCoordinate coordinate = convert(new BoardCoordinate(f1, r1, BoardCoordinate.CoordinateType.RAW), view);
                        highlight(coordinate.getFile(), coordinate.getRank());
                    } else {
                        x0 = 0;
                        y0 = 0;
                    }
                    repaint();
                }
            }

            @Override public void mouseEntered(MouseEvent e) {}
            @Override public void mouseExited(MouseEvent e) {}
            @Override public void mouseClicked(MouseEvent e) {}
        };
    }

    public BoardCoordinate convert(BoardCoordinate coordinate, BoardView view) {
        int newFile = coordinate.getFile();
        int newRank = coordinate.getRank();
        switch (view) {
            case WHITE -> {
                newFile = 7 - coordinate.getFile();
                newRank = 7 - coordinate.getRank();
            }
            case SIDE2 -> {
                newFile = coordinate.getRank();
                newRank = 7 - coordinate.getFile();
            }
            case SIDE -> {
                newFile = 7 - coordinate.getRank();
                newRank = coordinate.getFile();
            }
        }
        return new BoardCoordinate(newFile, newRank, BoardCoordinate.CoordinateType.RAW);
    }

    private void highlight(int file, int rank) {
        highlights[file][rank] = (highlights[file][rank] == null ? HighlightType.HIGHLIGHT : null);
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

    public static class BoardMatrixRotation {

        private static Piece[][] rotatePieceMatrix(Piece[][] board, BoardView view) {
            Piece[][] ret = Arrays.stream(board).map(Piece[]::clone).toArray(Piece[][]::new);

            switch (view) {
                case WHITE -> rotateMatrix180(ret);
                case SIDE -> rotateMatrix90CW(ret);
                case SIDE2 -> rotateMatrix90AC(ret);
            }
            return ret;
        }
        private static HighlightType[][] rotateHighlightMatrix(HighlightType[][] highlightTypes, BoardView view) {
            HighlightType[][] ret = Arrays.stream(highlightTypes).map(HighlightType[]::clone).toArray(HighlightType[][]::new);

            switch (view) {
                case WHITE -> rotateMatrix180(ret);
                case SIDE -> rotateMatrix90CW(ret);
                case SIDE2 -> rotateMatrix90AC(ret);
            }
            return ret;
        }

        private static <T> void transposeMatrix(T[][] board, int rows, int cols) {
            for (int i = 0; i < rows; i++) {
                for (int j = i; j < cols; j++) {
                    T temp = board[j][i];
                    board[j][i] = board[i][j];
                    board[i][j] = temp;
                }
            }
        }

        private static <T> void rotateMatrix180(T[][] board) {
            int rows = board.length;
            int cols = board[0].length;
            for (int i = 0; i <= (rows/2) - 1; i++) {
                for (int j = 0; j < cols; j++) {
                    T temp = board[i][j];
                    board[i][j] = board[rows - i - 1][cols - j - 1];
                    board[rows - i - 1][cols - j - 1] = temp;
                }
            }
        }
        private static <T> void rotateMatrix90CW(T[][] board) {
            int rows = board.length;
            int cols = board[0].length;
            transposeMatrix(board, rows, cols);
            for (int i = 0; i < rows; i++) {
                for (int j = 0; j < rows / 2; j++) {
                    T temp = board[i][j];
                    board[i][j] = board[i][rows - j - 1];
                    board[i][rows - j - 1] = temp;
                }
            }
        }
        private static <T> void rotateMatrix90AC(T[][] board) {
            int rows = board.length;
            int cols = board[0].length;
            transposeMatrix(board, rows, cols);
            for (int i = 0; i < cols; i++) {
                for (int j = 0, k = cols - 1; j < k; j++, k--) {
                    T temp = board[j][i];
                    board[j][i] = board[k][i];
                    board[k][i] = temp;
                }
            }
        }

        private static void printBoard(Piece[][] board) {
            for (Piece[] row: board) {
                for (Piece p: row) {
                    System.out.print( ((p == null) ? "_" : p.getAbbr()) + " ");
                }
                System.out.println();
            }
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        ColorScheme scheme = boardTheme.getScheme();
        Color light = scheme.light();
        Color dark = scheme.dark();
        Piece[][] board = BoardMatrixRotation.rotatePieceMatrix(gamePieces, view);
        HighlightType[][] highlightTypes = BoardMatrixRotation.rotateHighlightMatrix(highlights, view);

        if (useOnline) {
            Image image = OnlineAssets.getSavedBoard().getScaledInstance(pieceSize * 8, pieceSize * 8, Image.SCALE_FAST);
            if (view == BoardView.SIDE || view == BoardView.SIDE2) {
                double degrees = Math.toRadians(view == BoardView.SIDE ? 90 : -90);
                double locX = image.getWidth(null) / 2d;
                double locY = image.getHeight(null) / 2d;
                AffineTransform transform = AffineTransform.getRotateInstance(degrees, locX, locY);
                ((Graphics2D)g).drawImage(image, transform, null);
            } else {
                g.drawImage(image, 0, 0, null);
            }
        }

        for (int rank = 0; rank <= 7; rank++) {
            for (int file = 0; file <= 7; file++) {

                if (view == BoardView.SIDE || view == BoardView.SIDE2) {
                    g.setColor(((file + rank) % 2 == 0 ? dark : light));
                } else {
                    g.setColor(((file + rank) % 2 == 0 ? light : dark));
                }

                if (useOnline) {
                    if (highlightTypes[file][rank] != null) {
                        HighlightType type = highlightTypes[file][rank];
                        g.setColor(type == HighlightType.HIGHLIGHT ? scheme.getHighlighted() : scheme.getMoved());
                        g.fillRect((file) * pieceSize, (rank) * pieceSize, pieceSize, pieceSize);
                    }
                } else {
                    g.fillRect((file) * pieceSize, (rank) * pieceSize, pieceSize, pieceSize);
                    if (highlightTypes[file][rank] != null) {
                        HighlightType type = highlightTypes[file][rank];
                        Graphics2D g2d = (Graphics2D) g;
                        g2d.setComposite(AlphaComposite.getInstance(AlphaComposite.SRC_OVER, type == HighlightType.HIGHLIGHT ? scheme.getHighlighted().getAlpha() / 255f : scheme.getMoved().getAlpha() / 255f));
                        g.setColor(type == HighlightType.HIGHLIGHT ? scheme.getHighlighted() : scheme.getMoved());
                        g.fillRect(file * pieceSize, rank * pieceSize, pieceSize, pieceSize);
                        g2d.setComposite(AlphaComposite.SrcOver);
                    }
                }

                if (board[file][rank] != null) {
                    board[file][rank].paint(g, file * pieceSize, rank * pieceSize, pieceSize);
                }
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
        repaint();
    }

    public Piece[][] getGamePieces() {
        return gamePieces;
    }
}
