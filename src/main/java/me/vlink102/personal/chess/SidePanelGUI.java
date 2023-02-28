package me.vlink102.personal.chess;

import javax.swing.*;
import java.awt.*;

public class SidePanelGUI extends JPanel {
    private final BoardGUI boardGUI;

    public SidePanelGUI(BoardGUI boardGUI) {
        this.boardGUI = boardGUI;
        setBackground(Chess.menuScheme.getDarkerBackground());
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        g.setColor(Color.white);
        g.fillRect(0, 0, 100, 100);
    }
}
