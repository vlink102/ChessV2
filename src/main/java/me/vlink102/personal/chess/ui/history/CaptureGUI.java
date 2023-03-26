package me.vlink102.personal.chess.ui.history;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Chess;
import me.vlink102.personal.chess.internal.Move;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

public class CaptureGUI extends JPanel {
    private final BoardGUI boardGUI;
    private static BufferedImage capturedPieces;

    public enum CaptureDisplay {
        W_PAWN_1(1, 1, new Dimension(1, 1)),
        W_PAWN_2(1, 1, new Dimension(1, 1)),
        W_PAWN_3(1, 1, new Dimension(1, 1)),
        W_PAWN_4(1, 1, new Dimension(1, 1)),
        W_PAWN_5(1, 1, new Dimension(1, 1)),
        W_PAWN_6(1, 1, new Dimension(1, 1)),
        W_PAWN_7(1, 1, new Dimension(1, 1)),
        W_PAWN_8(1, 1, new Dimension(1, 1)),
        W_BISHOP_1(1, 1, new Dimension(1, 1)),
        W_BISHOP_2(1, 1, new Dimension(1, 1)),
        W_KNIGHT_1(1, 1, new Dimension(1, 1)),
        W_KNIGHT_2(1, 1, new Dimension(1, 1)),
        W_ROOK_1(1, 1, new Dimension(1, 1)),
        W_ROOK_2(1, 1, new Dimension(1, 1)),
        W_QUEEN_1(1, 1, new Dimension(1, 1)),
        B_PAWN_1(0, 350, new Dimension(26, 40)),
        B_PAWN_2(0, 300, new Dimension(40, 40)),
        B_PAWN_3(0, 250, new Dimension(53, 40)),
        B_PAWN_4(0, 200, new Dimension(69, 40)),
        B_PAWN_5(0, 150, new Dimension(82, 40)),
        B_PAWN_6(0, 100, new Dimension(95, 40)),
        B_PAWN_7(0, 50, new Dimension(110, 40)),
        B_PAWN_8(0, 0, new Dimension(125, 40)),
        B_BISHOP_1(135, 50, new Dimension(28, 40)),
        B_BISHOP_2(135, 0, new Dimension(43, 40)),
        B_KNIGHT_1(190, 50, new Dimension(28, 40)),
        B_KNIGHT_2(190, 0, new Dimension(43, 40)),
        B_ROOK_1(240, 50, new Dimension(30, 40)),
        B_ROOK_2(240, 0, new Dimension(45, 40)),
        B_QUEEN_1(290, 0, new Dimension(33, 40));

        private final int x;
        private final int y;
        private final Dimension dimension;

        CaptureDisplay(int x, int y, Dimension dimension) {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
        }

        public Dimension getDimension() {
            return dimension;
        }

        public int getX() {
            return x;
        }

        public int getY() {
            return y;
        }
    }

    public static Image getSprite(CaptureDisplay piece, int size) {
        return capturedPieces.getSubimage(piece.getX(), piece.getY(), (int) piece.getDimension().getWidth(), (int) piece.getDimension().getHeight()).getScaledInstance(size, -1, Image.SCALE_SMOOTH);
    }

    public CaptureGUI(BoardGUI boardGUI) {
        this.boardGUI = boardGUI;
        capturedPieces = Move.getBufferedResource("/captured-pieces.png");
        setBackground(Chess.menuScheme.darkerBackground());
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.drawImage(boardGUI.getOnlineAssets().getSavedCapturePiece(CaptureDisplay.B_QUEEN_1), 0, 0, this);
    }
}
