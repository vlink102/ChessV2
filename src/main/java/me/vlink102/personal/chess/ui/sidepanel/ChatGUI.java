package me.vlink102.personal.chess.ui.sidepanel;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Chess;

import javax.swing.*;
import java.awt.*;

public class ChatGUI extends JPanel {
    private final BoardGUI boardGUI;

    public ChatGUI(BoardGUI boardGUI) {
        this.boardGUI = boardGUI;
        setBackground(Chess.menuScheme.darkerBackground());
        setOpaque(true);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        setPreferredSize(new Dimension((int) getBounds().getWidth(), (int) getBounds().getHeight()));

    }
}
