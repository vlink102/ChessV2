package me.vlink102.personal.chess.ui;

import me.vlink102.personal.chess.internal.BoardCoordinate;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Chess;

import javax.swing.*;
import java.awt.*;

public class CoordinateGUI extends JPanel {
    private final BoardGUI boardGUI;
    private final Chess chess;

    public CoordinateGUI(Chess chess, BoardGUI boardGUI) {
        this.boardGUI = boardGUI;
        this.chess = chess;

        setOpaque(false);
        setFont(chess.getBold().deriveFont( (float) chess.defaultOffset - 4));
    }

    /**
     * Draw a String centered in the middle of a Rectangle.
     *
     * @param g The Graphics instance.
     * @param text The String to draw.
     * @param rect The Rectangle to center the text in.
     */
    public static void drawCenteredString(Graphics g, String text, Rectangle rect, Font font) {
        FontMetrics metrics = g.getFontMetrics(font);
        int x = rect.x + (rect.width - metrics.stringWidth(text)) / 2;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.setFont(font);
        g.drawString(text, x, y);
    }

    public static void drawLeftString(Graphics g, String text, Rectangle rect, Font font) {
        FontMetrics metrics = g.getFontMetrics(font);
        int x = rect.x;
        int y = rect.y + ((rect.height - metrics.getHeight()) / 2) + metrics.getAscent();
        g.setFont(font);
        g.drawString(text, x, y);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        if (boardGUI.getCoordinateDisplayType() == BoardGUI.CoordinateDisplayType.OUTSIDE) {
            g.setColor(Chess.menuScheme.coordinateBarColor());
            g.fillRect(0, 0, getWidth(), getHeight());
            for (int i = 0; i < boardGUI.getBoardSize(); i++) {
                int r1 = boardGUI.getView() == BoardGUI.BoardView.WHITE ? boardGUI.decBoardSize - i : i;
                Rectangle rectangle = new Rectangle(0, i * boardGUI.getPieceSize(), chess.offSet, boardGUI.getPieceSize());

                g.setColor(Chess.menuScheme.coordinateBarTextColor());
                String rowLetter = BoardCoordinate.getRowString(r1);
                drawCenteredString(g, rowLetter, rectangle, getFont());
            }
            for (int i = 0; i < boardGUI.getBoardSize(); i++) {
                int c1 = boardGUI.getView() == BoardGUI.BoardView.WHITE ? i : boardGUI.decBoardSize - i;
                Rectangle rectangle = new Rectangle((i * boardGUI.getPieceSize()) + chess.offSet, boardGUI.getPieceSize() * boardGUI.getBoardSize(), boardGUI.getPieceSize(), chess.offSet);

                g.setColor(Chess.menuScheme.coordinateBarTextColor());
                String colLetter = BoardCoordinate.getColString(c1);
                drawCenteredString(g, colLetter, rectangle, getFont());
            }
        }
    }
}
