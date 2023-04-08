package me.vlink102.personal.chess.ui.sidepanel;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Chess;
import me.vlink102.personal.chess.internal.Move;
import me.vlink102.personal.chess.ui.CoordinateGUI;

import javax.swing.*;
import java.awt.*;
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
        updateFonts();
    }

    public void updateFonts() {
        this.moveNumberFont = chess.getDef().deriveFont((float) 12);
        this.moveFont = chess.getDef().deriveFont((float) 14);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<Move> moveList = new ArrayList<>(boardGUI.getHistory());

        setPreferredSize(new Dimension((int) getBounds().getSize().getWidth(), ((moveList.size() / 2) * 25) + 50));

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
            g.setColor(Chess.menuScheme.moveNumberColor());
            CoordinateGUI.drawLeftString(g, i + ".", moveRect, moveNumberFont);

            if (j == moveList.size()) {
                g.setColor(boardGUI.getBoardTheme().getScheme().getMoved());
            }
            CoordinateGUI.drawLeftString(g, move.toString(), rectangle, moveFont);

            if (!(j + 1 > moveList.size())) {
                if (j + 1 == moveList.size()) {
                    g.setColor(boardGUI.getBoardTheme().getScheme().getMoved());
                }
                Move move2 = moveList.get(j);
                CoordinateGUI.drawLeftString(g, move2.toString(), rectangle1, moveFont);
            }
        }

        boardGUI.getSidePanelGUI().revalidate();
        boardGUI.getSidePanelGUI().repaint();
    }

}
