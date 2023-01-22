package me.vlink102.personal.chess;

import java.awt.*;

public class ColorScheme {
    private final Color dark;
    private final Color light;
    private final Color movedHighlight;
    private final Color background;

    public ColorScheme(Color light, Color dark, Color movedHighlight, Color background) {
        this.dark = dark;
        this.light = light;
        this.movedHighlight = movedHighlight;
        this.background = background;
    }

    public Color getDark() {
        return dark;
    }

    public Color getLight() {
        return light;
    }

    public Color getHighlighted() {
        return new Color(235, 97, 80, 200);
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

    public Color getMovedHighlight() {
        return movedHighlight;
    }

    public Color getBackground() {
        return background;
    }
}