package me.vlink102.personal.chess.internal;

import java.awt.*;

public class Ring {
    private final double x;
    private final double y;
    private final double width;
    private final double height;
    private final double thickness;
    private final Color color;

    public Ring(double x, double y, double width, double height, double thickness, Color color) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
        this.thickness = thickness;
        this.color = color;
    }

    public void draw(Graphics g) {
        Graphics2D g2d = (Graphics2D) g;
        Stroke oldStroke = g2d.getStroke();

        g2d.setColor(color);
        g2d.setStroke(new BasicStroke((float) thickness));
        g2d.drawOval((int) (x + thickness / 2), (int) (y + thickness / 2), (int) (width - thickness),
                (int) (height - thickness));
        g2d.setColor(color);
        g2d.setStroke(new BasicStroke((float) (thickness - 2)));
        g2d.drawOval((int) (x + thickness / 2), (int) (y + thickness / 2), (int) (width - thickness),
                (int) (height - thickness));

        g2d.setStroke(oldStroke);
    }
}