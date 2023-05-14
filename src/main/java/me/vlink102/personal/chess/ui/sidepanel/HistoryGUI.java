package me.vlink102.personal.chess.ui.sidepanel;

import com.sun.jdi.NativeMethodException;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Chess;
import me.vlink102.personal.chess.ChessMenu;
import me.vlink102.personal.chess.internal.Move;
import me.vlink102.personal.chess.ui.CoordinateGUI;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.ArrayList;
import java.util.List;

public class HistoryGUI extends JPanel {
    private final BoardGUI boardGUI;
    private final Chess chess;
    private Font moveNumberFont;
    private Font moveFont;

    public HistoryGUI(Chess chess, BoardGUI boardGUI) {
        this.boardGUI = boardGUI;
        this.chess = chess;

        setOpaque(true);
        setBackground(Chess.menuScheme.darkerBackground());
        setBorder(new EmptyBorder(3, 5, 3, 5));
        setPreferredSize(new Dimension((int) getBounds().getSize().getWidth(), (int) getBounds().getSize().getHeight()));

        updateFonts();

        addMouseListener(new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                Component c = getComponentAt(e.getPoint());
                if (c instanceof JLabel label) {
                    try {
                        int val = Integer.parseInt(label.getText());
                        boardGUI.setSelectedMove(val);
                    } catch (NumberFormatException exception) {
                        boardGUI.setSelectedMove(boardGUI.getHistory().size());
                    }
                }
            }
        });

        addMouseMotionListener(new MouseMotionAdapter() {
            @Override
            public void mouseMoved(MouseEvent e) {
                Component c = getComponentAt(e.getPoint());
                if (c instanceof JLabel) {
                    setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                } else {
                    setCursor(Cursor.getDefaultCursor());
                }
            }
        });
    }

    public void updateFonts() {
        this.moveNumberFont = chess.getDef().deriveFont((float) 12);
        this.moveFont = chess.getDef().deriveFont((float) 14);
    }

    /*
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setPreferredSize(new Dimension((int) getBounds().getSize().getWidth(), boardGUI.getHistory().size() * 30));
        revalidate();
    }
     */
    public List<JPanel> getPanels() {
        List<JPanel> panels = new ArrayList<>();
        List<Move> history = boardGUI.getHistory();
        for (int i = 0; i < history.size(); i++) {
            if (i % 2 == 0) {
                if (history.size() > i + 1) {
                    panels.add(ChessMenu.CreateChessGame.getCouplePanel(new JLabel(history.get(i).toString()), new JLabel(history.get(i + 1).toString()), true));
                } else {
                    panels.add(ChessMenu.CreateChessGame.getCouplePanel(new JLabel(history.get(i).toString()), new JLabel(), true));
                }
            }
        }
        return panels;
    }

    public void updateHistory() {
        /*
        removeAll();
        List<JPanel> panels = getPanels();
        for (JPanel panel : panels) {
            panel.setVisible(true);
            add(panel);
        }
        revalidate();

         */
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<Move> moveList = new ArrayList<>(boardGUI.getHistory());

        setPreferredSize(new Dimension((int) getBounds().getSize().getWidth(), ((moveList.size() / 2) * 25) + 50));

        removeAll();

        int i = 0;
        int j = 0;
        for (Move move : moveList) {
            j++;
            if (move.getPiece() != null) {
                if (move.getPiece().isWhite()) {
                    i++;
                } else {
                    continue;
                }
            } else {
                if ((j + 1) % 2 == 0) {
                    i++;
                } else {
                    continue;
                }
            }
            Rectangle moveRect = new Rectangle(25, i * (25), 25, 25);
            Rectangle rectangle = new Rectangle(50, i * (25), getWidth() / 4, 25);
            Rectangle rectangle1 = new Rectangle((getWidth() / 4) + (50), i * (25), getWidth() / 4, 25);

            if (i % 2 == 0) {
                g.setColor(Chess.menuScheme.pieceHistoryAlternateColor());
                g.fillRect(0, i * (25), getWidth(), 25);
            }
            if (j == boardGUI.getSelectedMove()) {
                g.setColor(Chess.menuScheme.selectedMove());
                g.fillRoundRect((int) rectangle.getX() - 10, (int) rectangle.getY(), (int) rectangle.getWidth() - 30, (int) rectangle.getHeight(), 10, 10);
                g.setColor(Chess.menuScheme.moveNumberColor());
            }
            if (j+1 == boardGUI.getSelectedMove()) {
                g.setColor(Chess.menuScheme.selectedMove());
                g.fillRoundRect((int) rectangle1.getX() - 10, (int) rectangle1.getY(), (int) rectangle1.getWidth() - 30, (int) rectangle1.getHeight(), 10, 10);
                g.setColor(Chess.menuScheme.moveNumberColor());
            }
            g.setColor(Chess.menuScheme.moveNumberColor());
            CoordinateGUI.drawLeftString(g, i + ".", moveRect, moveNumberFont);

            Color moved = boardGUI.getBoardTheme().getScheme().getMoved();

            if (j == moveList.size()) {
                g.setColor(moved);
            }

            addButton(rectangle, j);
            CoordinateGUI.drawLeftString(g, move.toString(), rectangle, moveFont);

            if (!(j + 1 > moveList.size())) {
                if (j + 1 == moveList.size()) {
                    g.setColor(moved);
                }
                Move move2 = moveList.get(j);
                addButton(rectangle1, j + 1);
                CoordinateGUI.drawLeftString(g, move2.toString(), rectangle1, moveFont);
            }
        }
    }


    private void addButton(Rectangle rectangle, int moveNo) {
        JLabel b = new JLabel(String.valueOf(moveNo));
        b.setSize((int) rectangle.getWidth(), (int) rectangle.getHeight());
        b.setFont(moveFont);
        add(b);
        b.setVisible(false);
        b.setLocation((int) (rectangle.getX()), (int) rectangle.getY());

    }
}
