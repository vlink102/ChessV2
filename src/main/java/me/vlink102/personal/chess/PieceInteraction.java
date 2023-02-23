package me.vlink102.personal.chess;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.List;

public class PieceInteraction implements MouseListener, MouseMotionListener {
    private final Chess chess;
    private final BoardGUI boardGUI;

    public PieceInteraction(Chess chess, BoardGUI boardGUI) {
        this.chess = chess;
        this.boardGUI = boardGUI;
    }
    private JLabel movedPiece;

    int y0;
    int x0;

    BoardCoordinate tileSelected = null;

    public void setTileSelected(BoardCoordinate hasPieceSelected) {
        this.tileSelected = hasPieceSelected;
    }

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            int y = e.getY();
            int x = e.getX();
            int f1 = x / boardGUI.getPieceSize();
            int r1 = y / boardGUI.getPieceSize();

            switch (boardGUI.getView()) {
                case BLACK -> f1 = 7 - f1;
                case WHITE -> r1 = 7 - r1;
            }

            BoardCoordinate clicked = new BoardCoordinate(r1, f1);
            List<Move> moves = boardGUI.availableMoves(boardGUI.getGamePieces(), boardGUI.getSelected(), boardGUI.getTileSelected(), false);

            switch (boardGUI.getMoveMethod()) {
                case BOTH -> {
                    movedPiece = null;
                    if (updateTileSelected(clicked, moves)) return;

                    addToDragLayer(e);
                }
                case DRAG -> {
                    movedPiece = null;
                    addToDragLayer(e);
                }
                case CLICK -> {
                    updateTileSelected(clicked, moves);
                    chess.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
                }
            }
        }
    }

    private void addToDragLayer(MouseEvent e) {
        x0 = e.getX();
        y0 = e.getY();

        Component componentClicked = boardGUI.findComponentAt(x0, y0);

        if (!(componentClicked instanceof JPanel)) {
            movedPiece = (JLabel) componentClicked;
            movedPiece.setLocation(e.getX() - (boardGUI.getPieceSize() / 2), e.getY() - (boardGUI.getPieceSize() / 2));

            chess.add(movedPiece, JLayeredPane.DRAG_LAYER);
            chess.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    private boolean updateTileSelected(BoardCoordinate clicked, List<Move> moves) {
        if (tileSelected == null) {
            if (boardGUI.getSelected() != null) {
                tileSelected = clicked;
            }
        } else {
            if (tileSelected.equals(clicked)) {
                tileSelected = null;
            } else {
                for (Move move : moves) {
                    if (move.getTo().equals(clicked)) {
                        boardGUI.movePiece(boardGUI.getSelected(), tileSelected, clicked);
                        tileSelected = null;

                        return true;
                    }
                }
                if (boardGUI.getGamePieces()[clicked.row()][clicked.col()] == null) {
                    tileSelected = null;
                    boardGUI.deselect();
                } else {
                    if (boardGUI.getSelected() != null) {
                        tileSelected = clicked;
                    }
                }
            }
        }
        return false;
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            chess.setCursor(null);

            int pieceSize = boardGUI.getPieceSize();

            switch (boardGUI.getMoveMethod()) {
                case BOTH, DRAG -> {
                    if (movedPiece == null) return;

                    movedPiece.setVisible(false);
                    JLabel moved = (JLabel) chess.getComponentsInLayer(JLayeredPane.DRAG_LAYER)[0];
                    chess.remove(moved);
                    movedPiece.setVisible(true);

                    int xMax = boardGUI.getWidth() - moved.getWidth();
                    int x = Math.min(e.getX(), xMax);
                    x = Math.max(x, 0);

                    int yMax = boardGUI.getHeight() - moved.getHeight();
                    int y = Math.min(e.getY(), yMax);
                    y = Math.max(y, 0);

                    Component c = boardGUI.findComponentAt(x, y);

                    if (c instanceof JLabel) {
                        Container parent = c.getParent();
                        parent.remove(0);
                        parent.add(moved);
                        parent.validate();
                    } else if (c instanceof JPanel square) {
                        square.add(moved);
                        square.validate();
                    }

                    int y1 = e.getY();
                    int x1 = e.getX();

                    int f0 = x0 / pieceSize;
                    int r0 = y0 / pieceSize;
                    int f1 = x1 / pieceSize;
                    int r1 = y1 / pieceSize;

                    switch (boardGUI.getView()) {
                        case BLACK -> {
                            f0 = 7 - f0;
                            f1 = 7 - f1;
                        }
                        case WHITE -> {
                            r0 = 7 - r0;
                            r1 = 7 - r1;
                        }
                    }

                    BoardCoordinate from = new BoardCoordinate(r0, f0);
                    BoardCoordinate to = new BoardCoordinate(r1, f1);
                    boardGUI.movePiece(boardGUI.getGamePieces()[from.row()][from.col()], from, to);
                }
                case CLICK -> {

                }
            }
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {

    }

    @Override
    public void mouseExited(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (boardGUI.getMoveMethod() == BoardGUI.MoveStyle.BOTH || boardGUI.getMoveMethod() == BoardGUI.MoveStyle.DRAG) {
                updateComponentDragPosition(e);
            }
        }
    }

    private void updateComponentDragPosition(MouseEvent e) {
        if (movedPiece == null) return;

        int x = e.getX();
        int xMax = boardGUI.getWidth();
        x = Math.min(x, xMax);
        x = Math.max(x, 0);

        int y = e.getY();
        int yMax = boardGUI.getHeight();
        y = Math.min(y, yMax);
        y = Math.max(y, 0);

        movedPiece.setLocation(x - (boardGUI.getPieceSize() / 2), y - (boardGUI.getPieceSize() / 2));
    }

    @Override
    public void mouseMoved(MouseEvent e) {
    }
}
