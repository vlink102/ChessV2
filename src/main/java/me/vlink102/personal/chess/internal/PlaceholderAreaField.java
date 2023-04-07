package me.vlink102.personal.chess.internal;

import javax.swing.*;
import java.awt.*;

public class PlaceholderAreaField extends JTextArea {
    private String placeholder;

    public PlaceholderAreaField(String text, int rows, int columns) {
        super(text, rows, columns);
        setLineWrap(true);
        setWrapStyleWord(true);
    }

    @Override
    protected void paintComponent(final Graphics pG) {
        super.paintComponent(pG);

        if (placeholder == null || placeholder.length() == 0 || getText().length() > 0) {
            return;
        }

        final Graphics2D g = (Graphics2D) pG;
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
        g.setColor(getDisabledTextColor());
        g.drawString(placeholder, getInsets().left, pG.getFontMetrics().getMaxAscent() + getInsets().top);
    }

    public void setPlaceholder(final String s) {
        placeholder = s;
    }
}
