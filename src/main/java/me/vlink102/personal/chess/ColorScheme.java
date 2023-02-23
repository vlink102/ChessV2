package me.vlink102.personal.chess;

import java.awt.*;

public record ColorScheme(Color light, Color dark, Color movedHighlight, Color background) {

    public Color getHighlight(Move.MoveHighlights mc) {
        return switch (mc) {
            case BRILLIANT -> new Color(27, 172, 166, 127);
            case GREAT -> new Color(92, 139, 176, 127);
            case BEST -> new Color(158, 186, 90, 127);
            case EXCELLENT -> new Color(150, 188, 75, 127);
            case GOOD -> new Color(150, 175, 139, 127);
            case BOOK -> new Color(168, 136, 101, 127);
            case INACCURACY -> new Color(240, 193, 92, 127);
            case MISTAKE -> new Color(230, 145, 44, 127);
            case BLUNDER -> new Color(179, 52, 48, 127);
            case MISSED_WIN -> new Color(219, 172, 22, 127);
            case FORCED -> new Color(164, 194, 91, 127);

            case CHECKMATE -> new Color(128, 0, 0);
            case CHECK, HIGHLIGHT -> new Color(235, 97, 80, 204);

            case ORANGE_HIGHLIGHT -> new Color(255, 170, 0, 204);
            case BLUE_HIGHLIGHT -> new Color(82, 176, 220, 204);
            case GREEN_HIGHLIGHT -> new Color(172, 206, 89, 204);

            case MOVE, SELECTED -> getMoved();
            case AVAILABLE -> new Color(0, 0, 0, 25);
        };
    }

    public Color getVignette() {
        return new Color(251,251,191);
    }

    public Color getMoved() {
        return movedHighlight;
    }

    public static Color blend(Color... c) {
        if (c == null || c.length <= 0) {
            return null;
        }
        float ratio = 1f / ((float) c.length);

        int a = 0;
        int r = 0;
        int g = 0;
        int b = 0;

        for (int i = 0; i < c.length; i++) {
            int rgb = c[i].getRGB();
            int a1 = (rgb >> 24 & 0xff);
            int r1 = ((rgb & 0xff0000) >> 16);
            int g1 = ((rgb & 0xff00) >> 8);
            int b1 = (rgb & 0xff);
            a += (a1 * ratio);
            r += (r1 * ratio);
            g += (g1 * ratio);
            b += (b1 * ratio);
        }

        return new Color(a << 24 | r << 16 | g << 8 | b);
    }
}