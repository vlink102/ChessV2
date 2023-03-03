package me.vlink102.personal.chess.ui;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Chess;
import me.vlink102.personal.chess.internal.Move;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public class SidePanelGUI extends JPanel {
    private final BoardGUI boardGUI;
    private Font moveNumberFont;
    private Font moveFont;

    public SidePanelGUI(BoardGUI boardGUI) {
        this.boardGUI = boardGUI;
        setBackground(Chess.menuScheme.getDarkerBackground());
        updateFonts();
    }

    public void updateFonts() {
        this.moveNumberFont = Chess.def.deriveFont((float) 12);
        this.moveFont = Chess.def.deriveFont((float) 14);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        List<Move> moveList = boardGUI.getHistory();
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        updateFonts();

        // TODO JSCROLLPANE
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
                g.setColor(Chess.menuScheme.getPieceHistoryAlternateColor());
                g.fillRect(0, i * (25), getWidth(), 25);
            }
            g.setColor(Chess.menuScheme.getMoveNumberColor());
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


    }
}
