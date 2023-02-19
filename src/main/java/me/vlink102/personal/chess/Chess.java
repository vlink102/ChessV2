package me.vlink102.personal.chess;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.IOException;

public class Chess extends JLayeredPane {
    private static BoardGUI board;

    public static boolean shouldRelocateBackline = true;
    public static boolean shouldShowAvailableSquares = true;

    public enum BoardLayout {
        CHESS960,
        DEFAULT
    }

    public Chess(int initialPieceSize) {
        board = new BoardGUI(this, BoardGUI.PieceDesign.NEO, BoardGUI.Colours.GREEN,initialPieceSize, false, true, BoardLayout.CHESS960);

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
        generalMenu.add(getFEN);

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
        generalMenu.add(loadFEN);

        JMenuItem gameOptions = new JMenu("Game");
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

        generalMenu.add(gameOptions);

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

        JMenuItem switchSides = new JMenuItem("Switch Sides");
        switchSides.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_SPACE, 0));
        switchSides.getAccessibleContext().setAccessibleDescription("Switches sides from black to white and vice versa");
        switchSides.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.swapPlaySide();
            }
        });
        generalMenu.add(switchSides);

        JMenuItem item = new JMenuItem("Quit Game", KeyEvent.VK_ESCAPE);
        item.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, InputEvent.ALT_MASK));
        item.getAccessibleContext().setAccessibleDescription("Exits the application");
        item.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                System.exit(0);
            }
        });
        generalMenu.add(item);


        JMenu boardMenu = new JMenu("Boards");
        for (BoardGUI.Colours colours : BoardGUI.Colours.values()) {
            JMenuItem theme = new JMenuItem(colours.getName());
            theme.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    board.setBoardTheme(colours);
                }
            });
            boardMenu.add(theme);
        }

        JMenu pieceMenu = new JMenu("Pieces");
        for (BoardGUI.PieceDesign designs : BoardGUI.PieceDesign.values()) {
            JMenuItem design = new JMenuItem(designs.getName());
            design.addActionListener(new AbstractAction() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    board.setPieceTheme(designs);
                }
            });
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

        JMenuItem showAvailableSquares = new JRadioButtonMenuItem("Show Moves", shouldShowAvailableSquares);
        showAvailableSquares.getAccessibleContext().setAccessibleDescription("Displays possible moves when selecting a piece");
        showAvailableSquares.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                shouldShowAvailableSquares = !shouldShowAvailableSquares;
            }
        });
        options.add(showAvailableSquares);

        settings.add(generalMenu);
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

}