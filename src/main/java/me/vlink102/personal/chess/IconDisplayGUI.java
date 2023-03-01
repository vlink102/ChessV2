package me.vlink102.personal.chess;

import javax.swing.*;
import java.awt.*;

public class IconDisplayGUI extends JPanel {
    private final BoardGUI boardGUI;

    public IconDisplayGUI(BoardGUI boardGUI) {
        this.boardGUI = boardGUI;
        setOpaque(false);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        ((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        for (int i = 0; i < BoardGUI.boardSize; i++) {
            for (int j = 0; j < BoardGUI.boardSize; j++) {
                int r1 = boardGUI.getView() == BoardGUI.BoardView.WHITE ? BoardGUI.decBoardSize - i : i;
                int c1 = boardGUI.getView() == BoardGUI.BoardView.WHITE ? j : BoardGUI.decBoardSize - j;
                if (boardGUI.getGameHighlights()[i][j] != null && Move.cachedIcons.get(boardGUI.getGameHighlights()[i][j]) != null) {
                    displayIcon(g, new BoardCoordinate(i, j), boardGUI.getGameHighlights()[i][j]);
                } else if (boardGUI.getHighlightIconAccompaniment()[i][j] != null && Move.cachedHighlights.get(boardGUI.getHighlightIconAccompaniment()[i][j]) != null) {
                    displayIcon(g, new BoardCoordinate(i, j), boardGUI.getHighlightIconAccompaniment()[i][j]);
                }
            }
        }
    }


    public void displayIcon(Graphics g, BoardCoordinate coordinate, Move.MoveHighlights icon) {
        int r1 = boardGUI.getView() == BoardGUI.BoardView.WHITE ? BoardGUI.decBoardSize - coordinate.row() : coordinate.row();
        int c1 = boardGUI.getView() == BoardGUI.BoardView.WHITE ? coordinate.col() : BoardGUI.decBoardSize - coordinate.col();
        if (Move.cachedHighlights.get(icon) != null) {
            if ((coordinate.row() == 0 && boardGUI.getView() == BoardGUI.BoardView.BLACK) || (coordinate.row() == BoardGUI.decBoardSize && boardGUI.getView() == BoardGUI.BoardView.WHITE)) {
                g.drawImage(Move.cachedHighlights.get(icon), (c1 * boardGUI.getPieceSize()) + (boardGUI.getPieceSize() - (boardGUI.getPieceSize() / 3) + (boardGUI.getPieceSize() / 20)), (r1 * boardGUI.getPieceSize()), this);
            } else {
                g.drawImage(Move.cachedHighlights.get(icon), (c1 * boardGUI.getPieceSize()) + (boardGUI.getPieceSize() - (boardGUI.getPieceSize() / 3) + (boardGUI.getPieceSize() / 20)), (r1 * boardGUI.getPieceSize()) - (boardGUI.getPieceSize() / 20), this);
            }
        }
    }

    public void displayIcon(Graphics g, BoardCoordinate coordinate, Move.InfoIcons icon) {
        int r1 = boardGUI.getView() == BoardGUI.BoardView.WHITE ? BoardGUI.decBoardSize - coordinate.row() : coordinate.row();
        int c1 = boardGUI.getView() == BoardGUI.BoardView.WHITE ? coordinate.col() : BoardGUI.decBoardSize - coordinate.col();
        if (Move.cachedIcons.get(icon) != null) {
            if ((coordinate.row() == 0 && boardGUI.getView() == BoardGUI.BoardView.BLACK) || (coordinate.row() == BoardGUI.decBoardSize && boardGUI.getView() == BoardGUI.BoardView.WHITE)) {
                g.drawImage(Move.cachedIcons.get(icon), (c1 * boardGUI.getPieceSize()) + (boardGUI.getPieceSize() - (boardGUI.getPieceSize() / 3) + (boardGUI.getPieceSize() / 20)), (r1 * boardGUI.getPieceSize()), this);
            } else {
                g.drawImage(Move.cachedIcons.get(icon), (c1 * boardGUI.getPieceSize()) + (boardGUI.getPieceSize() - (boardGUI.getPieceSize() / 3) + (boardGUI.getPieceSize() / 20)), (r1 * boardGUI.getPieceSize()) - (boardGUI.getPieceSize() / 20), this);
            }
        }
    }
}
