package me.vlink102.personal.chess.ui.sidepanel;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Chess;
import me.vlink102.personal.chess.internal.CapturedPieces;
import me.vlink102.personal.chess.internal.Move;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class CaptureGUI extends JPanel {
    private final BoardGUI boardGUI;
    private final BufferedImage capturedPieces;

    public enum CaptureDisplay {
        W_PAWN_1(360, 594, new Dimension(13, 17)),
        W_PAWN_2(360, 569, new Dimension(20, 17)),
        W_PAWN_3(360, 544, new Dimension(27, 17)),
        W_PAWN_4(360, 519, new Dimension(34, 17)),
        W_PAWN_5(360, 494, new Dimension(41, 17)),
        W_PAWN_6(360, 469, new Dimension(48, 17)),
        W_PAWN_7(360, 444, new Dimension(55, 17)),
        W_PAWN_8(360, 419, new Dimension(62, 17)),
        W_BISHOP_1(427, 443, new Dimension(15, 18)),
        W_BISHOP_2(427, 417, new Dimension(23, 19)),
        W_KNIGHT_1(454, 442, new Dimension(16, 19)),
        W_KNIGHT_2(454, 417, new Dimension(23, 19)),
        W_ROOK_1(480, 444, new Dimension(15, 17)),
        W_ROOK_2(480, 419, new Dimension(23, 17)),
        W_QUEEN_1(504, 417, new Dimension(18, 19)),
        B_PAWN_1(0, 594, new Dimension(13, 17)),
        B_PAWN_2(0, 569, new Dimension(20, 17)),
        B_PAWN_3(0, 544, new Dimension(27, 17)),
        B_PAWN_4(0, 519, new Dimension(34, 17)),
        B_PAWN_5(0, 494, new Dimension(41, 17)),
        B_PAWN_6(0, 469, new Dimension(48, 17)),
        B_PAWN_7(0, 444, new Dimension(55, 17)),
        B_PAWN_8(0, 419, new Dimension(62, 17)),
        B_BISHOP_1(67, 443, new Dimension(15, 18)),
        B_BISHOP_2(67, 417, new Dimension(23, 19)),
        B_KNIGHT_1(95, 442, new Dimension(16, 19)),
        B_KNIGHT_2(95, 417, new Dimension(23, 19)),
        B_ROOK_1(120, 444, new Dimension(15, 17)),
        B_ROOK_2(120, 419, new Dimension(23, 17)),
        B_QUEEN_1(145, 417, new Dimension(18, 19));

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

    public Image getSprite(CaptureDisplay piece, int size) {
        return capturedPieces.getSubimage(piece.getX(), piece.getY(), (int) piece.getDimension().getWidth(), (int) piece.getDimension().getHeight()).getScaledInstance(-1, size, Image.SCALE_SMOOTH);
    }

    public CaptureGUI(BoardGUI boardGUI) {
        this.boardGUI = boardGUI;
        capturedPieces = Move.getBufferedResource("/captured-pieces.png");
        setOpaque(true);
        setBackground(Chess.menuScheme.darkerBackground());
        setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setPreferredSize(new Dimension((int) getBounds().getWidth(), (int) getBounds().getHeight()));
    }

    public void updatePanel() {
        removeAll();
        for (JPanel panel : getCapturePanel(boardGUI.getCapturedPieces())) {
            add(panel);
        }
        revalidate();
    }

    public List<JPanel> getCapturePanel(CapturedPieces pieces) {
        List<JPanel> bruh = new ArrayList<>();
        bruh.add(heheheha(true, pieces));
        bruh.add(heheheha(false, pieces));
        return bruh;
    }

    private JPanel heheheha(boolean white, CapturedPieces pieces) {
        JPanel panel = new JPanel();
        panel.setLayout(new FlowLayout());
        HashMap<Boolean, List<Image>> display2 = pieces.getDisplays(boardGUI);
        for (Image image : display2.get(white)) {
            panel.add(new JLabel(new ImageIcon(image)));
        }
        return panel;
    }
}
