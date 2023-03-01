package me.vlink102.personal.chess;

import com.github.weisj.darklaf.LafManager;
import com.github.weisj.darklaf.theme.DarculaTheme;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

public class Chess extends JLayeredPane {
    private static BoardGUI board;
    public static JFrame frame;

    public static BufferedImage[] iconSprites = new BufferedImage[75];

    public static Font def;
    public static Font bold;
    public static Font icons;

    public static boolean shouldRelocateBackline = true;
    public static boolean shouldShowAvailableSquares = true;
    public static boolean shouldShowOppositionAvailableSquares = false;

    public static int boardToFrameOffset = 50;
    public static int sidePanelWidth = 400;

    public static final int defaultOffset = 20;
    public static int offSet = 0;
    public static int heightOffSet = 0;

    public static MenuScheme menuScheme = new MenuScheme(new Color(49,46,43), new Color(39,37,34), new Color(31,30,27), new Color(43,41,39), new Color(64,61,57), new Color(152,151,149), new Color(195, 194, 194), new Color(149, 148, 147));

    public enum BoardLayout {
        CHESS960,
        DEFAULT
    }

    public Chess(int initialPieceSize, int initialBoardSize) {
        board = new BoardGUI(this, BoardGUI.PieceDesign.NEO, BoardGUI.Colours.GREEN, initialPieceSize, true, true, BoardLayout.DEFAULT, BoardGUI.OpponentType.AUTO_SWAP, BoardGUI.MoveStyle.BOTH, BoardGUI.HintStyle.Move.DOT, BoardGUI.HintStyle.Capture.RING, BoardGUI.GameType.DEFAULT, BoardGUI.CoordinateDisplayType.OUTSIDE, initialBoardSize);

        add(board, DEFAULT_LAYER);
        add(board.getSidePanelGUI(), DEFAULT_LAYER);
        add(board.getCoordinateGUI(), DEFAULT_LAYER);
        add(board.getIconDisplayGUI(), POPUP_LAYER);
    }

    public static void initUI(int initialPieceSize, int initialBoardSize) {
        LafManager.install(new DarculaTheme());
        LafManager.setDecorationsEnabled(true);

        BufferedImage image = Move.getBufferedResource("/iconnav.png");

        for (int i = 0; i < 75; i++) {
            iconSprites[i] = image.getSubimage(0, i * (image.getHeight() / 75), (image.getHeight() / 75), (image.getHeight() / 75));
        }

        Image imageIcon = Move.getResource("/icon.png");

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream def = classLoader.getResourceAsStream("fonts/montserrat-700.2213e098.ttf");
        InputStream bold = classLoader.getResourceAsStream("fonts/montserrat-800.2d88ac8b.ttf");
        InputStream icons = classLoader.getResourceAsStream("fonts/icons.ttf");
        try {
            Chess.def = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(def));
            Chess.bold = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(bold));
            Chess.icons = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(icons));
            ge.registerFont(Chess.def);
            ge.registerFont(Chess.bold);
            ge.registerFont(Chess.icons);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if ((initialPieceSize * initialBoardSize) + Chess.sidePanelWidth + (Chess.boardToFrameOffset * 3) + offSet > screen.getWidth()) {
            ImageIcon icon = new ImageIcon(Move.getMoveHighlightIcon(Move.MoveHighlights.MISTAKE).getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            JOptionPane.showConfirmDialog(null, initialBoardSize + "*" + initialBoardSize + " board with piece size " + initialPieceSize + " (" + initialBoardSize * initialPieceSize + "*" + initialBoardSize * initialPieceSize + ")" + "\nis larger than the available space (" + screen.width + "*" + screen.height + ")\n\nFix Successful:\n - Piece size: 100\n - Board size: 8\n\nClick OK to continue...", "Board too large, reduced game size",  JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, icon);
            initialPieceSize = 100;
            initialBoardSize = 8;
        }
        Dimension initialSize = new Dimension((initialPieceSize * initialBoardSize) + Chess.sidePanelWidth + (Chess.boardToFrameOffset * 3) + offSet, (initialPieceSize * initialBoardSize) + (Chess.boardToFrameOffset * 2) + heightOffSet);
        frame = new JFrame("Chess [vlink102] Github b13.8.8");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(new Chess(initialPieceSize, initialBoardSize));
        frame.setResizable(true);
        frame.getContentPane().setPreferredSize(initialSize);

        frame.setMinimumSize(new Dimension(32 * initialBoardSize, 32 * initialBoardSize));
        frame.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
        frame.getContentPane().setBackground(menuScheme.getBackground());

        frame.setIconImage(imageIcon);

        frame.getContentPane().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refreshWindow();
            }
        });

        frame.addWindowStateListener(new WindowAdapter() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                if (e.getNewState() == Frame.ICONIFIED || e.getNewState() == Frame.MAXIMIZED_BOTH) {
                    frame.getContentPane().setSize(e.getWindow().getSize());
                    refreshWindow();
                }
            }
        });

        Timer timer = new Timer(250, refreshGUI());
        timer.start();
        lastSize = board.getPieceSize();

        frame.setJMenuBar(getMenu());

        frame.setSize(initialSize);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        board.requestFocus();
    }

    public static void refreshWindow() {
        board.setPieceSize((frame.getContentPane().getHeight() - ((boardToFrameOffset * 2))) / board.getBoardSize());

        Dimension dimension = new Dimension(board.getPieceSize() * board.getBoardSize(), board.getPieceSize() * board.getBoardSize());
        board.setPreferredSize(dimension);
        updateBoardBounds();
        updateSidePanelBounds();
        updateCoordinatePanelBounds();

        OnlineAssets.updateSavedImage(board);
        board.displayPieces();
        board.repaint();
    }

    public static void updateBoardBounds() {
        board.setBounds((boardToFrameOffset + Chess.offSet), boardToFrameOffset - heightOffSet, (board.getPieceSize() * board.getBoardSize()), (board.getPieceSize() * board.getBoardSize()));
        board.getIconDisplayGUI().setBounds(board.getBounds());
    }

    public static void updateSidePanelBounds() {
        board.getSidePanelGUI().setBounds((boardToFrameOffset * 2) + board.getWidth() + Chess.offSet, boardToFrameOffset - heightOffSet, sidePanelWidth - Chess.offSet, board.getHeight() + Chess.offSet);
    }

    public static void updateCoordinatePanelBounds() {
        board.getCoordinateGUI().setBounds(boardToFrameOffset, boardToFrameOffset - heightOffSet, board.getWidth() + Chess.offSet, board.getHeight() + Chess.offSet);
    }

    static int lastSize;

    private static ActionListener refreshGUI() {
        return e -> {
            if (lastSize != board.getPieceSize() ) {
                OnlineAssets.updatePieceDesigns(board);

                Move.loadCachedIcons(board.getPieceSize());
                Move.loadCachedHighlights(board.getPieceSize());

                board.displayPieces();
                board.repaint();
                lastSize = board.getPieceSize();
            }
        };
    }

    public static JMenuBar getMenu() {
        JMenuBar settings = new JMenuBar();
        JMenu generalMenu = new JMenu("General");

        JMenu FEN = new JMenu("FEN");

        JMenuItem getFEN = new JMenuItem("Copy FEN");
        getFEN.getAccessibleContext().setAccessibleDescription("Copies a FEN (Forsyth-Edwards Notation) of the current board state to your clipboard");
        getFEN.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String FEN = board.translateBoardToFEN(board.getGamePieces());
                StringSelection selection = new StringSelection(FEN);
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                clipboard.setContents(selection, null);
            }
        });
        FEN.add(getFEN);

        JMenuItem loadFEN = new JMenu("Load FEN");
        loadFEN.getAccessibleContext().setAccessibleDescription("Loads a FEN");

        JMenuItem clipboardLoad = new JMenuItem("Load from clipboard");
        clipboardLoad.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
                try {
                    Object FEN = clipboard.getData(DataFlavor.stringFlavor);
                    if (FEN != null) {
                        board.loadFEN(FEN.toString());
                    }
                } catch (UnsupportedFlavorException | IOException ex) {
                    throw new RuntimeException(ex);
                }
            }
        });
        loadFEN.add(clipboardLoad);

        JMenuItem inputLoad = new JMenuItem("Manual input");
        inputLoad.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                ImageIcon imageIcon = new ImageIcon(Move.getMoveHighlightIcon(Move.MoveHighlights.BOOK).getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                Object FEN = JOptionPane.showInputDialog(null, "Enter FEN String:", "Load game state", JOptionPane.PLAIN_MESSAGE, imageIcon, null, "");
                if (FEN != null) {
                    board.loadFEN(FEN.toString());
                }
            }
        });
        loadFEN.add(inputLoad);
        FEN.add(loadFEN);

        JMenu gameOptions = new JMenu("Game");
        JMenuItem opponentType = new JMenu("Opponent");

        ButtonGroup opponentGroup = new ButtonGroup();
        JMenuItem botMenu = new JMenu("Computer");
        JMenuItem random = new JRadioButtonMenuItem("Random Moves");
        random.getAccessibleContext().setAccessibleDescription("Play against random-moving bot");
        random.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.setOpponent(BoardGUI.OpponentType.AI_2);
            }
        });
        if (board.getOpponentType() == BoardGUI.OpponentType.AI_2) {
            random.setSelected(true);
        }
        opponentGroup.add(random);
        botMenu.add(random);

        JMenuItem AI = new JRadioButtonMenuItem("AI");
        AI.getAccessibleContext().setAccessibleDescription("Play against an AI");
        AI.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                 board.setOpponent(BoardGUI.OpponentType.AI_1);
            }
        });
        if (board.getOpponentType() == BoardGUI.OpponentType.AI_1) {
            AI.setSelected(true);
        }
        opponentGroup.add(AI);
        botMenu.add(AI);
        opponentType.add(botMenu);

        JMenuItem autoSwap = new JRadioButtonMenuItem("Auto Swap");
        autoSwap.getAccessibleContext().setAccessibleDescription("Will switch sides every move");
        autoSwap.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.setOpponent(BoardGUI.OpponentType.AUTO_SWAP);
            }
        });
        if (board.getOpponentType() == BoardGUI.OpponentType.AUTO_SWAP) {
            autoSwap.setSelected(true);
        }
        opponentGroup.add(autoSwap);
        opponentType.add(autoSwap);

        JMenuItem manual = new JRadioButtonMenuItem("Manual");
        manual.getAccessibleContext().setAccessibleDescription("No opponent: switch sides manually");
        manual.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.setOpponent(BoardGUI.OpponentType.MANUAL);
            }
        });
        if (board.getOpponentType() == BoardGUI.OpponentType.MANUAL) {
            manual.setSelected(true);
        }
        opponentType.add(manual);
        opponentGroup.add(manual);
        gameOptions.add(opponentType);

        JMenuItem resign = new JMenuItem("Resign");
        resign.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.resign();
            }
        });
        gameOptions.add(resign);

        JMenuItem draw = new JMenuItem("Offer Draw");
        draw.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.offerDraw();
            }
        });
        gameOptions.add(draw);

        JMenuItem abort = new JMenuItem("Abort Game");
        abort.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (board.getHistory() == null || board.getHistory().size() <= 2) {
                    board.abort();
                }
            }
        });
        gameOptions.add(abort);

        JMenuItem switchSides = new JMenuItem("Switch Sides");
        switchSides.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
        switchSides.getAccessibleContext().setAccessibleDescription("Switches sides from black to white and vice versa");
        switchSides.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.swapPlaySide();
            }
        });
        gameOptions.add(switchSides);

        JMenuItem resetBoard = new JMenu("Reset Board");
        resetBoard.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.resetBoard();
            }
        });

        JMenuItem defaultLayoutReset = new JMenuItem("Default");
        defaultLayoutReset.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.resetBoard(BoardLayout.DEFAULT);
            }
        });
        resetBoard.add(defaultLayoutReset);

        JMenuItem chess960Reset = new JMenuItem("Chess960/Fischer");
        chess960Reset.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.resetBoard(BoardLayout.CHESS960);
            }
        });
        resetBoard.add(chess960Reset);
        generalMenu.add(resetBoard);

        JMenuItem randomBoard = new JMenu("Random Board");
        JMenuItem randomEarlyGame = new JMenuItem("Early Game");
        randomEarlyGame.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.loadFEN(board.randomFENBoard(BoardGUI.GameStage.EARLY_GAME));
            }
        });
        randomBoard.add(randomEarlyGame);

        JMenuItem randomMidGame = new JMenuItem("Mid Game");
        randomMidGame.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.loadFEN(board.randomFENBoard(BoardGUI.GameStage.MID_GAME));
            }
        });
        randomBoard.add(randomMidGame);

        JMenuItem randomEndGame = new JMenuItem("End Game");
        randomEndGame.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.loadFEN(board.randomFENBoard(BoardGUI.GameStage.MID_GAME));
            }
        });
        randomBoard.add(randomEndGame);

        generalMenu.add(randomBoard);

        JMenuItem printBoard = new JMenu("Print board");
        printBoard.getAccessibleContext().setAccessibleDescription("Outputs the game to the console");

        JMenuItem printBoardSolo = new JMenuItem("Board Only");
        printBoardSolo.getAccessibleContext().setAccessibleDescription("Prints just the board");
        printBoardSolo.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.printGame(false);
            }
        });
        printBoard.add(printBoardSolo);

        JMenuItem printWithInfo = new JMenuItem("Board + Extra");
        printWithInfo.getAccessibleContext().setAccessibleDescription("Prints the board and extra game information");
        printWithInfo.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.printGame(true);
            }
        });
        printBoard.add(printWithInfo);
        generalMenu.add(printBoard);

        JMenuItem moveMethod = new JMenu("Move Method");
        ButtonGroup moveMethodGroup = new ButtonGroup();
        JMenuItem dragMethod = new JRadioButtonMenuItem("Drag Piece");
        dragMethod.getAccessibleContext().setAccessibleDescription("Drag and drop pieces to your desired location");
        dragMethod.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.setMoveMethod(BoardGUI.MoveStyle.DRAG);
            }
        });
        if (board.getMoveMethod() == BoardGUI.MoveStyle.DRAG) {
            dragMethod.setSelected(true);
        }
        moveMethodGroup.add(dragMethod);
        moveMethod.add(dragMethod);

        JMenuItem clickMethod = new JRadioButtonMenuItem("Click Squares");
        clickMethod.getAccessibleContext().setAccessibleDescription("Click a piece and the tile to move to");
        clickMethod.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.setMoveMethod(BoardGUI.MoveStyle.CLICK);
            }
        });
        if (board.getMoveMethod() == BoardGUI.MoveStyle.CLICK) {
            clickMethod.setSelected(true);
        }
        moveMethodGroup.add(clickMethod);
        moveMethod.add(clickMethod);

        JMenuItem bothMethod = new JRadioButtonMenuItem("Both");
        bothMethod.getAccessibleContext().setAccessibleDescription("Allows for both drag and click methods");
        bothMethod.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.setMoveMethod(BoardGUI.MoveStyle.BOTH);
            }
        });
        if (board.getMoveMethod() == BoardGUI.MoveStyle.BOTH) {
            bothMethod.setSelected(true);
        }
        moveMethodGroup.add(bothMethod);
        moveMethod.add(bothMethod);
        generalMenu.add(moveMethod);

        JMenuItem coordinateDisplay = new JMenu("Coordinate Display");
        coordinateDisplay.getAccessibleContext().setAccessibleDescription("Changes the way coordinates are viewed on the board");
        ButtonGroup coordinateGroup = new ButtonGroup();
        JMenuItem insideDisplay = new JRadioButtonMenuItem("Inside");
        insideDisplay.getAccessibleContext().setAccessibleDescription("Coordinates are shown in the corner of the board squares");
        insideDisplay.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.setCoordinateDisplayType(BoardGUI.CoordinateDisplayType.INSIDE);
            }
        });
        if (board.getCoordinateDisplayType() == BoardGUI.CoordinateDisplayType.INSIDE) {
            insideDisplay.setSelected(true);
        }
        coordinateDisplay.add(insideDisplay);
        coordinateGroup.add(insideDisplay);
        JMenuItem outsideDisplay = new JRadioButtonMenuItem("Outside");
        outsideDisplay.getAccessibleContext().setAccessibleDescription("Coordinates are shown outside the board");
        outsideDisplay.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.setCoordinateDisplayType(BoardGUI.CoordinateDisplayType.OUTSIDE);
            }
        });
        if (board.getCoordinateDisplayType() == BoardGUI.CoordinateDisplayType.OUTSIDE) {
            outsideDisplay.setSelected(true);
            board.setCoordinateDisplayType(BoardGUI.CoordinateDisplayType.OUTSIDE);
        }
        coordinateDisplay.add(outsideDisplay);
        coordinateGroup.add(outsideDisplay);
        JMenuItem noneDisplay = new JRadioButtonMenuItem("None");
        noneDisplay.getAccessibleContext().setAccessibleDescription("No coordinates are shown");
        noneDisplay.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.setCoordinateDisplayType(BoardGUI.CoordinateDisplayType.NONE);
            }
        });
        if (board.getCoordinateDisplayType() == BoardGUI.CoordinateDisplayType.NONE) {
            noneDisplay.setSelected(true);
        }
        coordinateDisplay.add(noneDisplay);
        coordinateGroup.add(noneDisplay);

        generalMenu.add(coordinateDisplay);

        JMenuItem item = new JMenuItem("Quit Game", KeyEvent.VK_ESCAPE);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Q, InputEvent.CTRL_DOWN_MASK));
        item.getAccessibleContext().setAccessibleDescription("Exits the application");
        item.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        generalMenu.add(item);


        JMenu boardMenu = new JMenu("Boards");
        ButtonGroup boardGroup = new ButtonGroup();
        for (BoardGUI.Colours colours : BoardGUI.Colours.values()) {
            JMenuItem theme = new JRadioButtonMenuItem(colours.getName());
            theme.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    board.setBoardTheme(colours);
                }
            });
            if (board.getBoardTheme().equals(colours)) {
                theme.setSelected(true);
            }
            boardGroup.add(theme);
            boardMenu.add(theme);
        }

        JMenu pieceMenu = new JMenu("Pieces");
        ButtonGroup pieceGroup = new ButtonGroup();
        for (BoardGUI.PieceDesign pieceTheme : BoardGUI.PieceDesign.values()) {
            JMenuItem design = new JRadioButtonMenuItem(pieceTheme.getName());
            design.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    board.setPieceTheme(pieceTheme);
                }
            });
            if (board.getPieceTheme().equals(pieceTheme)) {
                design.setSelected(true);
            }
            pieceGroup.add(design);
            pieceMenu.add(design);
        }

        JMenu options = new JMenu("Options");
        JMenuItem fenOptions = new JMenu("FEN Repair");
        JMenuItem relocateBackline = new JRadioButtonMenuItem("Relocate Backline", shouldRelocateBackline);
        relocateBackline.getAccessibleContext().setAccessibleDescription("Sets each sides backline (king position) to the default rank");
        relocateBackline.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shouldRelocateBackline = !shouldRelocateBackline;
            }
        });
        fenOptions.add(relocateBackline);
        options.add(fenOptions);

        JMenuItem hintMenu = new JMenu("Hints");
        JMenuItem showAvailableSquares = new JRadioButtonMenuItem("Show Moves", shouldShowAvailableSquares);
        showAvailableSquares.getAccessibleContext().setAccessibleDescription("Displays possible moves when selecting a piece");
        showAvailableSquares.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shouldShowAvailableSquares = !shouldShowAvailableSquares;
            }
        });
        hintMenu.add(showAvailableSquares);

        JMenuItem showOppositionAvailableSquares = new JRadioButtonMenuItem("Show opposition moves", shouldShowOppositionAvailableSquares);
        showOppositionAvailableSquares.getAccessibleContext().setAccessibleDescription("Displays possible moves for the opposition");
        showOppositionAvailableSquares.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shouldShowOppositionAvailableSquares = !shouldShowOppositionAvailableSquares;
            }
        });
        hintMenu.add(showOppositionAvailableSquares);

        JMenuItem hintStyles = new JMenu("Styles");
        JMenuItem captureStyles = new JMenu("Capture");
        ButtonGroup captureGroup = new ButtonGroup();
        JMenuItem captureRect = new JRadioButtonMenuItem("Square");
        captureRect.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.setCaptureStyle(BoardGUI.HintStyle.Capture.SQUARE);
            }
        });
        if (board.getCaptureStyle() == BoardGUI.HintStyle.Capture.SQUARE) {
            captureRect.setSelected(true);
        }
        captureGroup.add(captureRect);
        captureStyles.add(captureRect);
        JMenuItem captureRing = new JRadioButtonMenuItem("Ring");
        captureRing.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.setCaptureStyle(BoardGUI.HintStyle.Capture.RING);
            }
        });
        if (board.getCaptureStyle() == BoardGUI.HintStyle.Capture.RING) {
            captureRing.setSelected(true);
        }
        captureGroup.add(captureRing);
        captureStyles.add(captureRing);
        hintStyles.add(captureStyles);

        JMenuItem moveStyles = new JMenu("Move");
        ButtonGroup moveGroup = new ButtonGroup();
        JMenuItem moveRectangular = new JRadioButtonMenuItem("Square");
        moveRectangular.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.setMoveStyle(BoardGUI.HintStyle.Move.SQUARE);
            }
        });
        if (board.getMoveStyle() == BoardGUI.HintStyle.Move.SQUARE) {
            moveRectangular.setSelected(true);
        }
        moveGroup.add(moveRectangular);
        moveStyles.add(moveRectangular);

        JMenuItem moveDot = new JRadioButtonMenuItem("Dot");
        moveDot.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.setMoveStyle(BoardGUI.HintStyle.Move.DOT);
            }
        });
        if (board.getMoveStyle() == BoardGUI.HintStyle.Move.DOT) {
            moveDot.setSelected(true);
        }
        moveGroup.add(moveDot);
        moveStyles.add(moveDot);
        hintStyles.add(moveStyles);

        hintMenu.add(hintStyles);
        options.add(hintMenu);

        settings.add(generalMenu);
        settings.add(gameOptions);
        settings.add(FEN);
        settings.add(boardMenu);
        settings.add(pieceMenu);
        settings.add(options);

        return settings;
    }

    public void createPopUp(String message, String title, Move.MoveHighlights highlights) {
        ImageIcon imageIcon = new ImageIcon(Move.getMoveHighlightIcon(highlights).getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        JOptionPane.showConfirmDialog(this, message, title,  JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, imageIcon);
        requestFocus();
    }

    public void createPopUp(String message, String title, Move.InfoIcons highlights) {
        ImageIcon imageIcon = new ImageIcon(Move.getInfoIcon(highlights).getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        JOptionPane.showConfirmDialog(this, message, title,  JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, imageIcon);
        requestFocus();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> Chess.initUI(100, 8));
    }

    public enum OS {
        WINDOWS, LINUX, MAC, SOLARIS
    }

    private static OS os = null;

    public static OS getOS() {
        if (os == null) {
            String operSys = System.getProperty("os.name").toLowerCase();
            if (operSys.contains("win")) {
                os = OS.WINDOWS;
            } else if (operSys.contains("nix") || operSys.contains("nux")
                    || operSys.contains("aix")) {
                os = OS.LINUX;
            } else if (operSys.contains("mac")) {
                os = OS.MAC;
            } else if (operSys.contains("sunos")) {
                os = OS.SOLARIS;
            }
        }
        return os;
    }
}