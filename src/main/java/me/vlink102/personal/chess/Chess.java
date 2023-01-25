package me.vlink102.personal.chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

public class Chess {
    private static BoardGUI board;

    public static void main(String[] args) {
        board = new BoardGUI(BoardGUI.PieceDesign.NEO, BoardGUI.Colours.GREEN,100, true,true);
        board.setupBoard(true);
        board.setupBoard(false);

        JFrame frame = new JFrame("Chess - vlink102 - Prototype v3");
        Dimension size = new Dimension(board.getDimension() + 100, board.getDimension());

        frame.getContentPane().setPreferredSize(size);
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);
        frame.setResizable(true);

        frame.setMinimumSize(new Dimension(32 * 8, 32 * 8));
        frame.setMaximumSize(Toolkit.getDefaultToolkit().getScreenSize());

        frame.getContentPane().addComponentListener(new ComponentAdapter() {
            @Override
            public void componentResized(ComponentEvent e) {
                board.setPieceSize(frame.getContentPane().getHeight() / 8);

                //frame.getContentPane().setSize(/* todo*/);

                board.repaint();
            }
        });

        frame.setSize(size);
        board.addMouseListener(board.highlightListener());
        board.addKeyListener(board.boardViewListener());

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

        frame.add(board);
        frame.setJMenuBar(settings);


        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
        frame.toFront();
        board.requestFocus();
    }

}