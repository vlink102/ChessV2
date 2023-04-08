package me.vlink102.personal.chess;

import me.vlink102.personal.GameSelector;
import me.vlink102.personal.chess.classroom.Classroom;
import me.vlink102.personal.chess.internal.MenuScheme;
import me.vlink102.personal.chess.internal.Move;
import me.vlink102.personal.chess.internal.networking.CommunicationHandler;
import me.vlink102.personal.chess.internal.networking.packets.RequestOnline;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

public class Chess extends JLayeredPane {
    private final BoardGUI board;

    public BoardGUI getBoard() {
        return board;
    }

    private JScrollPane scrollPane;

    public JScrollPane getScrollPane() {
        return scrollPane;
    }

    public JFrame frame;

    public final BufferedImage[] iconSprites = new BufferedImage[75];

    private final Font def;
    private final Font bold;
    private final Font icons;

    public boolean shouldRelocateBackline = true;
    public boolean shouldShowAvailableSquares = true;
    public boolean shouldShowOppositionAvailableSquares = false;

    public int boardToFrameOffset = 100;
    public int sidePanelWidth = 400;

    public final int defaultOffset = 20;
    public int offSet = 0;
    public int heightOffSet = 0;

    public static final MenuScheme menuScheme = new MenuScheme(new Color(49,46,43), new Color(39,37,34), new Color(31,30,27), new Color(43,41,39), new Color(64,61,57), new Color(152,151,149), new Color(195, 194, 194), new Color(149, 148, 147));

    public enum BoardLayout {
        CHESS960,
        DEFAULT
    }

    public Chess(boolean challenge, long gameID, String opponentUUID, String precreatedFEN, int initialPieceSize, int initialBoardSize, boolean useOnline, boolean playAsWhite, BoardGUI.OpponentType type, BoardGUI.GameType gameType, Chess.BoardLayout layout, BoardGUI.PieceDesign pieceTheme, BoardGUI.Colours boardTheme, BoardGUI.MoveStyle moveMethod, BoardGUI.HintStyle.Move moveStyle, BoardGUI.HintStyle.Capture captureStyle, BoardGUI.CoordinateDisplayType coordinateDisplayType) {
        BufferedImage image = Move.getBufferedResource("/iconnav.png");

        for (int i = 0; i < 75; i++) {
            iconSprites[i] = image.getSubimage(0, i * (image.getHeight() / 75), (image.getHeight() / 75), (image.getHeight() / 75));
        }

        GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream def = classLoader.getResourceAsStream("fonts/montserrat-700.2213e098.ttf");
        InputStream bold = classLoader.getResourceAsStream("fonts/montserrat-800.2d88ac8b.ttf");
        InputStream icons = classLoader.getResourceAsStream("fonts/icons.ttf");
        try {
            this.def = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(def));
            this.bold = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(bold));
            this.icons = Font.createFont(Font.TRUETYPE_FONT, Objects.requireNonNull(icons));
            ge.registerFont(this.def);
            ge.registerFont(this.bold);
            ge.registerFont(this.icons);
        } catch (FontFormatException | IOException e) {
            throw new RuntimeException(e);
        }

        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if ((initialPieceSize * initialBoardSize) + sidePanelWidth + (boardToFrameOffset * 3) + offSet > screen.getWidth()) {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(Move.getMoveHighlightIcon(Move.MoveHighlights.MISTAKE)).getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            JOptionPane.showConfirmDialog(null, initialBoardSize + "*" + initialBoardSize + " board with piece size " + initialPieceSize + " (" + initialBoardSize * initialPieceSize + "*" + initialBoardSize * initialPieceSize + ")" + "\nis larger than the available space (" + screen.width + "*" + screen.height + ")\n\nFix Successful:\n - Piece size: 100\n - Board size: 8\n\nClick OK to continue...", "Board too large, reduced game size",  JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, icon);
            initialPieceSize = 100;
            initialBoardSize = 8;
        }
        Dimension initialSize = new Dimension((initialPieceSize * initialBoardSize) + sidePanelWidth + (boardToFrameOffset * 3) + offSet, (initialPieceSize * initialBoardSize) + (boardToFrameOffset * 2) + heightOffSet);
        frame = new JFrame("Chess [vlink102] Github b16.6.3");
        frame.setLayout(new BorderLayout());
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

        board = new BoardGUI(challenge, gameID, opponentUUID, precreatedFEN, this, initialPieceSize, initialBoardSize, useOnline, playAsWhite, type, gameType, layout, pieceTheme, boardTheme, moveMethod, moveStyle, captureStyle, coordinateDisplayType);

        scrollPane = new JScrollPane();
        scrollPane.setViewportView(board.getSidePanelGUI());
        scrollPane.setPreferredSize(new Dimension(2000, 2000));
        scrollPane.setSize(sidePanelWidth, 400);
        scrollPane.setWheelScrollingEnabled(true);
        scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        scrollPane.setRequestFocusEnabled(true);

        // TODO DO this for chat panel ( scroll )
        // TODO Add birthdate and phone to profile + db
        // TODO add register to login page
        // TODO do premove logic against other player (packets)
        // TODO Condense profile request to 1 request (quicker)


        add(board, DEFAULT_LAYER);
        add(scrollPane, DEFAULT_LAYER);
        add(board.getCaptureGUI(), DEFAULT_LAYER);
        add(board.getCoordinateGUI(), DEFAULT_LAYER);
        add(board.getIconDisplayGUI(), POPUP_LAYER);
        add(board.getChatGUI(), DEFAULT_LAYER);

        updateSidePanelBounds();
        updateChatPanelBounds();
        updateBoardBounds();
        updateCoordinatePanelBounds();
        updateCapturePanelBounds();

        frame.getContentPane().add(this);
        frame.setResizable(true);
        frame.getContentPane().setPreferredSize(initialSize);

        frame.setMinimumSize(new Dimension(32 * initialBoardSize, 32 * initialBoardSize));
        frame.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
        frame.getContentPane().setBackground(menuScheme.background());

        frame.setIconImage(GameSelector.getImageIcon());

        frame.getContentPane().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                refreshWindow();
            }
        });

        frame.addWindowStateListener(new WindowAdapter() {
            @Override
            public void windowStateChanged(WindowEvent e) {
                if (e.getNewState() == Frame.ICONIFIED || e.getNewState() == Frame.MAXIMIZED_BOTH || e.getNewState() == Frame.MAXIMIZED_HORIZ || e.getNewState() == Frame.MAXIMIZED_VERT) {
                    frame.getContentPane().setSize(e.getWindow().getSize());
                    refreshWindow();
                }
            }
        });

        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                board.resign();
                ChessMenu.getInstances().remove(Chess.this);
            }
        });

        Timer timer = new Timer(250, refreshGUIListener());
        timer.start();
        lastSize = board.getPieceSize();

        frame.setJMenuBar(getMenu(challenge));

        frame.setSize(initialSize);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        board.requestFocus();
        refreshWindow();
        refreshGUI();
    }

    public void refreshWindow() {
        board.setPieceSize((frame.getContentPane().getHeight() - ((boardToFrameOffset * 2))) / board.getBoardSize());

        Dimension dimension = new Dimension(board.getPieceSize() * board.getBoardSize(), board.getPieceSize() * board.getBoardSize());
        board.setPreferredSize(dimension);

        updateBoardBounds();
        updateSidePanelBounds();
        updateCoordinatePanelBounds();
        updateCapturePanelBounds();
        updateChatPanelBounds();

        board.getOnlineAssets().updateSavedImage(board);

        board.repaint();
        board.displayPieces();
    }

    public void updateBoardBounds() {
        board.setBounds((boardToFrameOffset + offSet), boardToFrameOffset - heightOffSet, (board.getPieceSize() * board.getBoardSize()), (board.getPieceSize() * board.getBoardSize()));
        board.getIconDisplayGUI().setBounds(board.getBounds());
    }

    public void updateSidePanelBounds() {
        scrollPane.setBounds((boardToFrameOffset * 2) + board.getWidth() + offSet, (boardToFrameOffset - heightOffSet) + 10 + (board.getHeight() / 5), Math.min(sidePanelWidth - offSet, frame.getContentPane().getWidth() - ((3 * boardToFrameOffset) + board.getWidth() + offSet)), (int) (board.getHeight() * (3f/5f)) - 20);
    }

    public void updateCapturePanelBounds() {
        board.getCaptureGUI().setBounds((boardToFrameOffset * 2) + board.getWidth() + offSet, (boardToFrameOffset - heightOffSet), Math.min(sidePanelWidth - offSet, frame.getContentPane().getWidth() - ((3 * boardToFrameOffset) + board.getWidth() + offSet)), board.getHeight() / 5);
    }

    public void updateCoordinatePanelBounds() {
        board.getCoordinateGUI().setBounds(boardToFrameOffset, boardToFrameOffset - heightOffSet, board.getWidth() + offSet, board.getHeight() + offSet);
    }

    public void updateChatPanelBounds() {
        board.getChatGUI().setBounds((boardToFrameOffset * 2) + board.getWidth() + offSet, frame.getContentPane().getHeight() - (boardToFrameOffset - heightOffSet) - (board.getHeight() / 5), Math.min(sidePanelWidth - offSet, frame.getContentPane().getWidth() - ((3 * boardToFrameOffset) + board.getWidth() + offSet)), board.getHeight() / 5);
    }

    private double lastSize;

    public void refreshGUI() {
        boardToFrameOffset = board.getPieceSize();
        sidePanelWidth = Math.min(board.getWidth() / 2, frame.getContentPane().getWidth() - ((boardToFrameOffset * 3) + board.getWidth() + offSet));
        board.setFont(def.deriveFont((float) board.getPieceSize() / 6));
        board.getSidePanelGUI().updateFonts();
        board.getCoordinateGUI().updateFonts();
        board.getOnlineAssets().updatePieceDesigns(board);
        board.getOnlineAssets().loadCapturedPieces(board);

        Move.loadCachedIcons(board.getPieceSize());
        Move.loadCachedHighlights(board.getPieceSize());

        lastSize = board.getWidth() / (double) board.getBoardSize();

        board.repaint();
        board.getSidePanelGUI().repaint();
        board.getCaptureGUI().repaint();
        board.displayPieces();
    }

    private ActionListener refreshGUIListener() {
        return e -> {
            if (lastSize != board.getWidth() / (float) board.getBoardSize()) {
                refreshGUI();
            }
        };
    }

    public JMenuItem getCopyFENItem() {
        return Classroom.getCopyFENItem(board.translateBoardToFEN(board.getGamePieces()));
    }

    public JMenuItem getLoadFENItem() {
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
                ImageIcon imageIcon = new ImageIcon(Objects.requireNonNull(Move.getMoveHighlightIcon(Move.MoveHighlights.BOOK)).getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                Object FEN = JOptionPane.showInputDialog(null, "Enter FEN String:", "Load game state", JOptionPane.PLAIN_MESSAGE, imageIcon, null, "");
                if (FEN != null) {
                    board.loadFEN(FEN.toString());
                }
            }
        });
        loadFEN.add(inputLoad);
        return loadFEN;
    }

    public JMenuItem getOpponentMenuItem() {
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
        return opponentType;
    }

    public JMenuItem getResignItem() {
        JMenuItem resign = new JMenuItem("Resign");
        resign.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.resign();
            }
        });
        return resign;
    }

    public JMenuItem getDrawItem() {
        JMenuItem draw = new JMenuItem("Offer Draw");
        draw.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.offerDraw();
            }
        });
        return draw;
    }

    public JMenuItem getAbortItem() {
        JMenuItem abort = new JMenuItem("Abort Game");
        abort.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (board.getHistory() == null || board.getHistory().size() <= 2) {
                    board.abort();
                }
            }
        });
        return abort;
    }

    public JMenuItem getSwapSidesItem() {
        JMenuItem switchSides = new JMenuItem("Switch Sides");
        switchSides.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, 0));
        switchSides.getAccessibleContext().setAccessibleDescription("Switches sides from black to white and vice versa");
        switchSides.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.swapPlaySide();
            }
        });
        return switchSides;
    }

    public JMenuItem getResetBoardItem() {
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
        return resetBoard;
    }

    public JMenuItem getRandomBoardItem() {
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
        return randomBoard;
    }

    public JMenuItem getPrintBoardItem() {
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
        return printBoard;
    }

    public JMenuItem getMoveMethodItem() {
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
        return moveMethod;
    }

    public JMenuItem getCoordinateDisplayItem() {
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

        return coordinateDisplay;
    }

    public JMenuItem getBoardDesignMenuItem() {
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
        return boardMenu;
    }

    public JMenuItem getPieceDesignMenuItem() {
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
        return pieceMenu;
    }

    public JMenuItem getFenOptionsItem() {
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
        return fenOptions;
    }

    public JMenuItem getShowAllyHintItem() {
        JMenuItem showAvailableSquares = new JRadioButtonMenuItem("Show Moves", shouldShowAvailableSquares);
        showAvailableSquares.getAccessibleContext().setAccessibleDescription("Displays possible moves when selecting a piece");
        showAvailableSquares.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shouldShowAvailableSquares = !shouldShowAvailableSquares;
            }
        });
        return showAvailableSquares;
    }

    public JMenuItem getShowEnemyHintItem() {
        JMenuItem showOppositionAvailableSquares = new JRadioButtonMenuItem("Show opposition moves", shouldShowOppositionAvailableSquares);
        showOppositionAvailableSquares.getAccessibleContext().setAccessibleDescription("Displays possible moves for the opposition");
        showOppositionAvailableSquares.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shouldShowOppositionAvailableSquares = !shouldShowOppositionAvailableSquares;
            }
        });
        return showOppositionAvailableSquares;
    }

    public JMenuItem getHintStylesItem() {
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
        return hintStyles;
    }

    public JMenuBar getMenu(boolean challenge) {
        JMenuBar settings = new JMenuBar();
        JMenu generalMenu = new JMenu("General");

        JMenu FEN = new JMenu("FEN");
        if (!challenge) {
            FEN.add(getCopyFENItem());
            FEN.add(getLoadFENItem());
        }

        JMenu gameOptions = new JMenu("Game");
        if (!challenge) {
            gameOptions.add(getOpponentMenuItem());
        }

        gameOptions.add(getResignItem());
        gameOptions.add(getDrawItem());
        gameOptions.add(getAbortItem());

        if (!challenge) {
            gameOptions.add(getSwapSidesItem());
            generalMenu.add(getResetBoardItem());
            generalMenu.add(getRandomBoardItem());
        }

        generalMenu.add(getPrintBoardItem());
        generalMenu.add(getMoveMethodItem());

        generalMenu.add(getCoordinateDisplayItem());

        generalMenu.add(Classroom.getQuitMenuItem());

        JMenu options = new JMenu("Options");
        if (!challenge) {
            options.add(getFenOptionsItem());
        }

        JMenuItem hintMenu = new JMenu("Hints");
        hintMenu.add(getShowAllyHintItem());
        if (!challenge) {
            hintMenu.add(getShowEnemyHintItem());
        }

        hintMenu.add(getHintStylesItem());
        options.add(hintMenu);

        settings.add(generalMenu);
        settings.add(gameOptions);
        settings.add(FEN);
        settings.add(getBoardDesignMenuItem());
        settings.add(getPieceDesignMenuItem());
        settings.add(options);

        return settings;
    }

    public record SocialMenuResult(JPanel panel, JList<String> list) {}

    public static SocialMenuResult createSocialMenu() {
        JPanel panel = new JPanel();
        CommunicationHandler.thread.sendPacket(new RequestOnline(ChessMenu.IDENTIFIER));
        JSONObject object = CommunicationHandler.thread.onlinePlayers;
        if (object == null || object.isEmpty()) return null;
        DefaultListModel<String> model = new DefaultListModel<>();
        JList<String> list = new JList<>(model);
        list.setSelectionMode(DefaultListSelectionModel.SINGLE_SELECTION);
        list.setFixedCellWidth(200);
        list.setFixedCellHeight(40);
        for (String uuid : object.keySet()) {
            model.addElement(object.getString(uuid));
        }
        panel.add(list);

        return new SocialMenuResult(panel, list);
    }

    public record ChallengeAcceptResult(int pSz, BoardGUI.PieceDesign pieceTheme, BoardGUI.Colours boardTheme, BoardGUI.MoveStyle moveMethod, BoardGUI.HintStyle.Move moveStyle, BoardGUI.HintStyle.Capture captureStyle, BoardGUI.CoordinateDisplayType coordinateDisplayType) {}

    public static ChallengeAcceptResult createChallengeAcceptWindow(JSONObject object, JSONObject data) {
        if (object == null) return null;

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        String[][] tableData = {
                {"Opponent:", CommunicationHandler.nameFromUUID(object.getString("challenger"))},
                {"Board Size:", String.valueOf(data.getInt("board-size"))},
                {"Game type:", data.getString("game-type").replaceAll("_", " ")},
                {"Layout:", data.getString("layout")},
                {"Playing as:", (!data.getBoolean("white") ? "White" : "Black")}
        };

        String[] columnNames = {"Label", "Value"};

        JTable table = new JTable(tableData, columnNames);
        table.setTableHeader(null);
        table.setShowGrid(false);

        SpinnerModel model = new SpinnerNumberModel(100, 32, 150, 1);
        JSpinner spinner = new JSpinner(model);
        JLabel label = new JLabel("Piece Size (px): ");
        JPanel pieceSizePanel = ChessMenu.CreateChessGame.getCouplePanel(label, spinner, true);

        JComboBox<BoardGUI.PieceDesign> pieceDesignJComboBox = new JComboBox<>(BoardGUI.PieceDesign.values());
        pieceDesignJComboBox.setSelectedItem(BoardGUI.PieceDesign.NEO);
        JLabel pieceDesignLabel = new JLabel("Piece Theme: ");
        JPanel pieceDesignPanel = ChessMenu.CreateChessGame.getCouplePanel(pieceDesignLabel, pieceDesignJComboBox, true);

        JComboBox<BoardGUI.Colours> boardThemeJComboBox = new JComboBox<>(BoardGUI.Colours.values());
        boardThemeJComboBox.setSelectedItem(BoardGUI.Colours.GREEN);
        JLabel boardThemeLabel = new JLabel("Board Theme: ");
        JPanel boardThemePanel = ChessMenu.CreateChessGame.getCouplePanel(boardThemeLabel, boardThemeJComboBox, true);

        JComboBox<BoardGUI.MoveStyle> moveMethodJComboBox = new JComboBox<>(BoardGUI.MoveStyle.values());
        moveMethodJComboBox.setSelectedItem(BoardGUI.MoveStyle.BOTH);
        JLabel moveMethodLabel = new JLabel("Move Method: ");
        JPanel moveMethodPanel = ChessMenu.CreateChessGame.getCouplePanel(moveMethodLabel, moveMethodJComboBox, true);

        JComboBox<BoardGUI.HintStyle.Move> moveJComboBox = new JComboBox<>(BoardGUI.HintStyle.Move.values());
        moveJComboBox.setSelectedItem(BoardGUI.HintStyle.Move.DOT);
        JLabel moveLabel = new JLabel("Move Style: ");
        JPanel movePanel = ChessMenu.CreateChessGame.getCouplePanel(moveLabel, moveJComboBox, true);

        JComboBox<BoardGUI.HintStyle.Capture> captureJComboBox = new JComboBox<>(BoardGUI.HintStyle.Capture.values());
        captureJComboBox.setSelectedItem(BoardGUI.HintStyle.Capture.RING);
        JLabel captureLabel = new JLabel("Capture Style: ");
        JPanel capturePanel = ChessMenu.CreateChessGame.getCouplePanel(captureLabel, captureJComboBox, true);

        JComboBox<BoardGUI.CoordinateDisplayType> coordinateDisplayTypeJComboBox = new JComboBox<>(BoardGUI.CoordinateDisplayType.values());
        coordinateDisplayTypeJComboBox.setSelectedItem(BoardGUI.CoordinateDisplayType.INSIDE);
        JLabel cdtLabel = new JLabel("Coordinate Display: ");
        JPanel cdtPanel = ChessMenu.CreateChessGame.getCouplePanel(cdtLabel, coordinateDisplayTypeJComboBox, true);

        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 20)));
        panel.add(table);
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 15)));
        panel.add(pieceSizePanel);
        panel.add(new JSeparator());
        panel.add(pieceDesignPanel);
        panel.add(boardThemePanel);
        panel.add(new JSeparator());
        panel.add(moveMethodPanel);
        panel.add(new JSeparator());
        panel.add(movePanel);
        panel.add(capturePanel);
        panel.add(new JSeparator());
        panel.add(cdtPanel);
        panel.add(Box.createRigidArea(new Dimension(panel.getWidth(), 20)));

        Object[] options = {"Accept", "Decline"};

        int result = JOptionPane.showOptionDialog(null, panel, CommunicationHandler.nameFromUUID(object.getString("challenger")) + " sent a challenge!", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);

        if (result != JOptionPane.OK_OPTION) {
            return null;
        }

        return new ChallengeAcceptResult((Integer) spinner.getValue(), (BoardGUI.PieceDesign) pieceDesignJComboBox.getSelectedItem(), (BoardGUI.Colours) boardThemeJComboBox.getSelectedItem(), (BoardGUI.MoveStyle) moveMethodJComboBox.getSelectedItem(), (BoardGUI.HintStyle.Move) moveJComboBox.getSelectedItem(), (BoardGUI.HintStyle.Capture) captureJComboBox.getSelectedItem(), (BoardGUI.CoordinateDisplayType) coordinateDisplayTypeJComboBox.getSelectedItem());
    }

    public int createDrawGameWindow(BoardGUI board, JSONObject object) {
        if (object == null) return JOptionPane.CANCEL_OPTION;
        Object[] options = {"Accept", "Decline"};
        return JOptionPane.showOptionDialog(board, CommunicationHandler.nameFromUUID(object.getString("draw_uuid")) + " offered a draw.", "Incoming draw request", JOptionPane.OK_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE, null, options, options[0]);
    }

    public static void createChallengeDeclinedWindow(JSONObject object) {
        JOptionPane.showMessageDialog(null, CommunicationHandler.nameFromUUID(object.getString("challenged")) + " declined your challenge.", "Challenge aborted", JOptionPane.INFORMATION_MESSAGE, new ImageIcon(Objects.requireNonNull(Move.getInfoIcon(Move.InfoIcons.ABORTED)).getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
    }

    public void createPopUp(String message, String title, Move.MoveHighlights highlights) {
        popUp(this, message, title, highlights);
        requestFocus();
    }

    public void createPopUp(String message, String title, Move.InfoIcons highlights) {
        popUp(this, message, title, highlights);
        requestFocus();
    }
    
    public static JLayeredPane popUp(JLayeredPane parentComponent, String message, String title, Move.InfoIcons highlights) {
        ImageIcon imageIcon = new ImageIcon(Objects.requireNonNull(Move.getInfoIcon(highlights)).getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        JOptionPane.showConfirmDialog(parentComponent, message, title,  JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, imageIcon);
        return parentComponent;
    }

    public static JLayeredPane popUp(JLayeredPane parentComponent, String message, String title, Move.MoveHighlights highlights) {
        ImageIcon imageIcon = new ImageIcon(Objects.requireNonNull(Move.getMoveHighlightIcon(highlights)).getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        JOptionPane.showConfirmDialog(parentComponent, message, title,  JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, imageIcon);
        return parentComponent;
    }

    public Font getDef() {
        return def;
    }

    public Font getBold() {
        return bold;
    }

    public Font getIcons() {
        return icons;
    }
}