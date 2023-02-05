package me.vlink102.personal.chess;

import javax.swing.*;
import java.awt.*;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.*;

public class Chess extends JLayeredPane {
    private static BoardGUI board;

    public Chess(int initialPieceSize) {
        board = new BoardGUI(this, BoardGUI.PieceDesign.NEO, BoardGUI.Colours.GREEN,initialPieceSize, true,true);

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

        settings.add(generalMenu);
        settings.add(boardMenu);
        settings.add(pieceMenu);
        return settings;
    }

    public static void main(String[] args) {
        EventQueue.invokeLater(() -> Chess.initUI(100));
    }
}