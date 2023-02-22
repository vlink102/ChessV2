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

    boolean hasPieceSelected = false;

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (boardGUI.getStyle() == BoardGUI.MoveStyle.BOTH || boardGUI.getStyle() == BoardGUI.MoveStyle.DRAG) {
                movedPiece = null;
                x0 = e.getX();
                y0 = e.getY();

                Component clicked = boardGUI.findComponentAt(x0, y0);

                if (clicked instanceof JPanel) return;

                movedPiece = (JLabel) clicked;
                movedPiece.setLocation(e.getX() - (boardGUI.getPieceSize() / 2), e.getY() - (boardGUI.getPieceSize() / 2));

                chess.add(movedPiece, JLayeredPane.DRAG_LAYER);
                chess.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
            }
            if (hasPieceSelected) {
                hasPieceSelected = false;
            } else {
                if (boardGUI.getStyle() == BoardGUI.MoveStyle.BOTH || boardGUI.getStyle() == BoardGUI.MoveStyle.CLICK) {
                    if (boardGUI.getSelected() != null) {
                        if (boardGUI.getSelected().isWhite() == boardGUI.isWhiteTurn() == boardGUI.isPlayAsWhite()) {
                            hasPieceSelected = true;
                        }
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            if (boardGUI.getStyle() == BoardGUI.MoveStyle.BOTH || boardGUI.getStyle() == BoardGUI.MoveStyle.DRAG) {
                int pieceSize = boardGUI.getPieceSize();

                chess.setCursor(null);

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
                } else {
                    throw new IllegalComponentStateException("Component placed outside board");
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
            if (boardGUI.getStyle() == BoardGUI.MoveStyle.BOTH || boardGUI.getStyle() == BoardGUI.MoveStyle.DRAG) {
                updateComponentDragPosition(e);
            }
        }
        /*
        switch (Chess.getOS()) {
            case MAC -> {
                if (SwingUtilities.isLeftMouseButton(e)) {
                    updateComponentDragPosition(e);
                }
            }
            case WINDOWS -> {
                System.out.println(e.getButton());
                updateComponentDragPosition(e);
            }
        }

         */
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
