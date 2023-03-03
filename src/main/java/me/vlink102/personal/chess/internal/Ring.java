package me.vlink102.personal.chess.internal;

import java.awt.*;

public class Ring {
    private final int x, y, width, height, thickness;
    private final Color color;

    public Ring(int x, int y, int width, int height, int thickness, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.thickness = thickness;
        this.color = color;
    }

    public Ring(int x, int y, int radius, int thickness, Color color) {
        this(x, y, radius * 2, radius * 2, thickness, color);
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Stroke oldStroke = g2d.getStroke();

        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(thickness));
        g2d.drawOval(x + thickness / 2, y + thickness / 2, width - thickness,
                height - thickness);
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke(thickness - 2));
        g2d.drawOval(x + thickness / 2, y + thickness / 2, width - thickness,
                height - thickness);

        g2d.setStroke(oldStroke);
    }
}