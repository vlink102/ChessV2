package me.vlink102.personal.chess.classroom;

import me.vlink102.personal.GameSelector;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Chess;
import me.vlink102.personal.chess.internal.BoardMatrixRotation;
import me.vlink102.personal.chess.internal.ClassroomAssets;
import me.vlink102.personal.chess.internal.Move;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.*;
import java.io.IOException;
import java.util.Objects;

public class Classroom extends JLayeredPane {
    private static ClassroomGUI board;
    public static JFrame frame;

    public Classroom(int initialPieceSize, int initialBoardSize, boolean useOnline, boolean playAsWhite, BoardGUI.PieceDesign pieceTheme, BoardGUI.Colours boardTheme) {
        board = new ClassroomGUI(this, initialPieceSize, initialBoardSize, useOnline, playAsWhite, pieceTheme, boardTheme);

        add(board, DEFAULT_LAYER);
    }

    public static Classroom initUI(int initialPieceSize, int initialBoardSize, boolean useOnline, boolean playAsWhite, BoardGUI.PieceDesign pieceTheme, BoardGUI.Colours boardTheme) {
        Dimension screen = Toolkit.getDefaultToolkit().getScreenSize();
        if ((initialPieceSize * initialBoardSize)> screen.getWidth()) {
            ImageIcon icon = new ImageIcon(Objects.requireNonNull(Move.getMoveHighlightIcon(Move.MoveHighlights.MISTAKE)).getScaledInstance(50, 50, Image.SCALE_SMOOTH));
            JOptionPane.showConfirmDialog(null, initialBoardSize + "*" + initialBoardSize + " board with piece size " + initialPieceSize + " (" + initialBoardSize * initialPieceSize + "*" + initialBoardSize * initialPieceSize + ")" + "\nis larger than the available space (" + screen.width + "*" + screen.height + ")\n\nFix Successful:\n - Piece size: 100\n - Board size: 8\n\nClick OK to continue...", "Board too large, reduced game size",  JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, icon);
            initialPieceSize = 100;
            initialBoardSize = 8;
        }

        Dimension initialSize = new Dimension((initialPieceSize * initialBoardSize), (initialPieceSize * initialBoardSize));
        frame = new JFrame("Chess [vlink102] Github b13.8.8");
        frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
        Classroom classroom = new Classroom(initialPieceSize, initialBoardSize, useOnline, playAsWhite, pieceTheme, boardTheme);
        frame.add(classroom);
        frame.setResizable(true);
        frame.getContentPane().setPreferredSize(initialSize);

        frame.setMinimumSize(new Dimension(32 * initialBoardSize, 32 * initialBoardSize));
        frame.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());
        frame.getContentPane().setBackground(Chess.menuScheme.getBackground());

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
        return classroom;
    }

    static int lastSize;

    private static ActionListener refreshGUI() {
        return e -> {
            if (lastSize != board.getPieceSize() ) {
                ClassroomAssets.updatePieceDesigns(board);

                Move.loadCachedIcons(board.getPieceSize());
                Move.loadCachedHighlights(board.getPieceSize());

                board.displayPieces();
                board.repaint();
                lastSize = board.getPieceSize();
            }
        };
    }

    public static void refreshWindow() {
        board.setPieceSize(frame.getContentPane().getHeight() / board.getBoardSize());

        Dimension dimension = new Dimension(board.getPieceSize() * board.getBoardSize(), board.getPieceSize() * board.getBoardSize());
        board.setPreferredSize(dimension);
        board.setBounds(0, 0, (board.getPieceSize() * board.getBoardSize()), (board.getPieceSize() * board.getBoardSize()));

        ClassroomAssets.updateSavedImage(board);
        board.displayPieces();
        board.repaint();
    }

    public static JMenuBar getMenu() {
        JMenuBar settings = new JMenuBar();
        JMenu generalMenu = new JMenu("General");

        JMenuItem resetBoard = new JMenuItem("Reset board");
        resetBoard.getAccessibleContext().setAccessibleDescription("Resets the board to the starting position");
        resetBoard.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                board.resetBoard();
            }
        });
        settings.add(resetBoard);

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
                ImageIcon imageIcon = new ImageIcon(Objects.requireNonNull(Move.getMoveHighlightIcon(Move.MoveHighlights.BOOK)).getScaledInstance(50, 50, Image.SCALE_SMOOTH));
                Object FEN = JOptionPane.showInputDialog(null, "Enter FEN String:", "Load game state", JOptionPane.PLAIN_MESSAGE, imageIcon, null, "");
                if (FEN != null) {
                    board.loadFEN(FEN.toString());
                }
            }
        });
        loadFEN.add(inputLoad);
        FEN.add(loadFEN);

        JMenuItem printBoardSolo = new JMenuItem("Print board");
        printBoardSolo.getAccessibleContext().setAccessibleDescription("Prints just the board");
        printBoardSolo.addActionListener(new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                BoardMatrixRotation.printBoard(board.getGamePieces(), board.getView(), board.getBoardSize());
            }
        });

        generalMenu.add(printBoardSolo);


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

        settings.add(generalMenu);
        settings.add(FEN);
        settings.add(boardMenu);
        settings.add(pieceMenu);
        return settings;
    }
}
