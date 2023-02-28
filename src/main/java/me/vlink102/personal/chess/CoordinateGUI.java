package me.vlink102.personal.chess;

import javax.swing.*;
import java.awt.*;

public class CoordinateGUI extends JPanel {
    private final BoardGUI boardGUI;

    public CoordinateGUI(BoardGUI boardGUI) {
        this.boardGUI = boardGUI;

        setOpaque(false);
        setFont(Chess.bold.deriveFont( (float) Chess.defaultOffset - 4));
    }

    /**
     * Draw a String centered in the middle of a Rectangle.
     *
     * @param g The Graphics instance.
     * @param text The String to draw.
     * @param rect The Rectangle to center the text in.
     */
    public void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        FontMetrics metrics = g.getFontMetrics(font);
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.setFont(font);
        g.drawString(text, x, y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (boardGUI.getCoordinateDisplayType() == BoardGUI.CoordinateDisplayType.OUTSIDE) {
            g.setColor(Chess.menuScheme.getCoordinateBarColor());
            g.fillRect(0, 0, getWidth(), getHeight());
            for (int i = 0; i < boardGUI.getBoardSize(); i++) {
                int r1 = boardGUI.getView() == BoardGUI.BoardView.WHITE ? BoardGUI.decBoardSize - i : i;
                Rectangle rectangle = new Rectangle(0, i * boardGUI.getPieceSize(), Chess.offSet, boardGUI.getPieceSize());

                g.setColor(Chess.menuScheme.getCoordinateBarTextColor());
                String rowLetter = BoardCoordinate.getRowString(r1);
                drawCenteredString(g, rowLetter, rectangle, getFont());
            }
            for (int i = 0; i < boardGUI.getBoardSize(); i++) {
                int c1 = boardGUI.getView() == BoardGUI.BoardView.WHITE ? i : BoardGUI.decBoardSize - i;
                Rectangle rectangle = new Rectangle((i * boardGUI.getPieceSize()) + Chess.offSet, boardGUI.getPieceSize() * boardGUI.getBoardSize(), boardGUI.getPieceSize(), Chess.offSet);

                g.setColor(Chess.menuScheme.getCoordinateBarTextColor());
                String colLetter = BoardCoordinate.getColString(c1);
                drawCenteredString(g, colLetter, rectangle, getFont());
            }
        }
    }
}
