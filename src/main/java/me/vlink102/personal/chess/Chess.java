package me.vlink102.personal.chess;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.net.URL;

public class Chess extends JLayeredPane implements MouseListener, MouseMotionListener {
    private static BoardGUI board;
    public static Image getPiece() {
        try {
            return ImageIO.read(new URL("https://www.chess.com/chess-themes/pieces/neo/64/wp.png"));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public Chess(int initialPieceSize) {
        board = new BoardGUI(BoardGUI.PieceDesign.NEO, BoardGUI.Colours.GREEN,initialPieceSize, true,true);
        Dimension initialSize = new Dimension(initialPieceSize * 8, initialPieceSize * 8);

        board.setLayout(new GridLayout(8, 8));
        board.setPreferredSize(initialSize);
        board.setBounds(0, 0, initialSize.width, initialSize.height);
        board.addMouseListener(this);
        board.addMouseMotionListener(this);
        add(board, JLayeredPane.DEFAULT_LAYER);

        for (int i = 0; i < 8; i++) {
            for (int j = 0; j < 8; j++) {
                JPanel square = new JPanel(new BorderLayout());
                board.add(square);
                square.setOpaque(false);
            }
        }

        board.requestFocus();
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

                //frame.getContentPane().setSize(/* todo*/);

                board.repaint();
            }
        });

        frame.setJMenuBar(getMenu());

        frame.setSize(initialSize);
        frame.pack();
        frame.setLocationRelativeTo(null);
        frame.setVisible(true);
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

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {

    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {

    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}