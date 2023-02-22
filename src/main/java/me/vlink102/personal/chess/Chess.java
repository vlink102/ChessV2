package me.vlink102.personal.chess;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class Chess extends JLayeredPane {
    private static BoardGUI board;

    public static boolean shouldRelocateBackline = true;
    public static boolean shouldShowAvailableSquares = true;
    public static boolean shouldShowOppositionAvailableSquares = false;

    public enum BoardLayout {
        CHESS960,
        DEFAULT
    }

    public Chess(int initialPieceSize) {
        board = new BoardGUI(this, BoardGUI.PieceDesign.NEO, BoardGUI.Colours.GREEN,initialPieceSize, false, true, BoardLayout.DEFAULT, BoardGUI.OpponentType.AUTO_SWAP, BoardGUI.MoveStyle.BOTH);

        add(board, JLayeredPane.DEFAULT_LAYER);
    }

    public static void initUI(int initialPieceSize) {
        Dimension initialSize = new Dimension(initialPieceSize * 8, initialPieceSize * 8);
        JFrame frame = new JFrame("Chess - vlink102 - Prototype v5");
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.add(new Chess(initialPieceSize));
        frame.setResizable(true);
        frame.getContentPane().setPreferredSize(initialSize);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(true);

        frame.setMinimumSize(new Dimension(32 * 8, 32 * 8));
        frame.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());

        frame.getContentPane().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                board.setPieceSize(frame.getContentPane().getHeight() / 8);

                board.setPreferredSize(new Dimension(board.getDimension(), board.getDimension()));
                board.setBounds(0, 0, board.getPieceSize() * 8, board.getPieceSize() * 8);

                board.repaint();
                board.displayPieces();
            }
        });

        frame.setJMenuBar(getMenu());

        frame.setSize(initialSize);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        board.requestFocus();
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
                ImageIcon imageIcon = new ImageIcon(Move.getHighlightIcon(Move.Highlights.BOOK).getScaledInstance(50, 50, Image.SCALE_SMOOTH));
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
                // TODO Resign
            }
        });
        gameOptions.add(resign);

        JMenuItem draw = new JMenuItem("Offer Draw");
        draw.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Draw
            }
        });
        gameOptions.add(draw);

        JMenuItem abort = new JMenuItem("Abort Game");
        abort.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (board.getHistory() == null || board.getHistory().size() <= 1) {
                    // TODO Abort
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
                board.setStyle(BoardGUI.MoveStyle.DRAG);
            }
        });
        if (board.getStyle() == BoardGUI.MoveStyle.DRAG) {
            dragMethod.setSelected(true);
        }
        moveMethodGroup.add(dragMethod);
        moveMethod.add(dragMethod);

        JMenuItem clickMethod = new JRadioButtonMenuItem("Click Squares");
        clickMethod.getAccessibleContext().setAccessibleDescription("Click a piece and the tile to move to");
        clickMethod.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.setStyle(BoardGUI.MoveStyle.CLICK);
            }
        });
        if (board.getStyle() == BoardGUI.MoveStyle.CLICK) {
            clickMethod.setSelected(true);
        }
        moveMethodGroup.add(clickMethod);
        moveMethod.add(clickMethod);

        JMenuItem bothMethod = new JRadioButtonMenuItem("Both");
        bothMethod.getAccessibleContext().setAccessibleDescription("Allows for both drag and click methods");
        bothMethod.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.setStyle(BoardGUI.MoveStyle.BOTH);
            }
        });
        if (board.getStyle() == BoardGUI.MoveStyle.BOTH) {
            bothMethod.setSelected(true);
        }
        moveMethodGroup.add(bothMethod);
        moveMethod.add(bothMethod);
        generalMenu.add(moveMethod);

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
        options.add(hintMenu);

        settings.add(generalMenu);
        settings.add(gameOptions);
        settings.add(FEN);
        settings.add(boardMenu);
        settings.add(pieceMenu);
        settings.add(options);
        return settings;
    }

    public void createPopUp(String message, String title, Move.Highlights highlights) {
        ImageIcon imageIcon = new ImageIcon(Move.getHighlightIcon(highlights).getScaledInstance(50, 50, Image.SCALE_SMOOTH));
        JOptionPane.showConfirmDialog(this, message, title,  JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, imageIcon);
        requestFocus();
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> Chess.initUI(100));
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