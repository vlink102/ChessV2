package me.vlink102.personal.chess.ui.interactive;

import me.vlink102.personal.chess.classroom.Classroom;
import me.vlink102.personal.chess.classroom.ClassroomGUI;
import me.vlink102.personal.chess.internal.BoardCoordinate;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;

public class ClassroomInteraction implements MouseListener, MouseMotionListener {
    private final Classroom classRoom;
    private final ClassroomGUI classroomGUI;

    public ClassroomInteraction(Classroom classRoom, ClassroomGUI classroomGUI) {
        this.classRoom = classRoom;
        this.classroomGUI = classroomGUI;
    }

    private JLabel movedPiece;
    int y0;
    int x0;

    @Override
    public void mouseClicked(MouseEvent e) {

    }

    @Override
    public void mousePressed(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            movedPiece = null;
            addToDragLayer(e);
        }
    }

    private void addToDragLayer(MouseEvent e) {
        x0 = e.getX();
        y0 = e.getY();

        Component componentClicked = classRoom.findComponentAt(x0, y0);

        if (!(componentClicked instanceof JPanel)) {
            movedPiece = (JLabel) componentClicked;
            movedPiece.setLocation(e.getX() - (classroomGUI.getPieceSize() / 2), e.getY() - (classroomGUI.getPieceSize() / 2));

            classRoom.add(movedPiece, JLayeredPane.DRAG_LAYER);
            classRoom.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            classRoom.setCursor(null);

            int pieceSize = classroomGUI.getPieceSize();

            if (movedPiece == null) return;

            movedPiece.setVisible(false);
            JLabel moved = (JLabel) classRoom.getComponentsInLayer(JLayeredPane.DRAG_LAYER)[0];
            classRoom.remove(moved);
            movedPiece.setVisible(true);

            int xMax = classroomGUI.getWidth() - moved.getWidth();
            int x = Math.min(e.getX(), xMax);
            x = Math.max(x, 0);

            int yMax = classroomGUI.getHeight() - moved.getHeight();
            int y = Math.min(e.getY(), yMax);
            y = Math.max(y, 0);

            Component c = classroomGUI.findComponentAt(x, y);

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

            switch (classroomGUI.getView()) {
                case BLACK -> {
                    f0 = (classroomGUI.getBoardSize() - 1) - f0;
                    f1 = (classroomGUI.getBoardSize() - 1) - f1;
                }
                case WHITE -> {
                    r0 = (classroomGUI.getBoardSize() - 1) - r0;
                    r1 = (classroomGUI.getBoardSize() - 1) - r1;
                }
            }

            BoardCoordinate from = new BoardCoordinate(r0, f0, classroomGUI);
            BoardCoordinate to = new BoardCoordinate(r1, f1, classroomGUI);
            classroomGUI.movePiece(from, to);
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
            updateComponentDragPosition(e);
        }
    }
    private void updateComponentDragPosition(MouseEvent e) {
        if (movedPiece == null) return;

        int x = e.getX();
        int xMax = classroomGUI.getWidth();
        x = Math.min(x, xMax);
        x = Math.max(x, 0);

        int y = e.getY();
        int yMax = classroomGUI.getHeight();
        y = Math.min(y, yMax);
        y = Math.max(y, 0);

        movedPiece.setLocation(x - (classroomGUI.getPieceSize() / 2), y - (classroomGUI.getPieceSize() / 2));
    }

    @Override
    public void mouseMoved(MouseEvent e) {

    }
}
