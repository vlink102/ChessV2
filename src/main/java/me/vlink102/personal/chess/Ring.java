package me.vlink102.personal.chess;

import java.awt.*;

class Ring {
    private int x, y, width, height, thickness;
    private Color color;

    public Ring(int x, int y, int width, int height, int thickness, Color color) {
        setX(x);
        setY(y);
        setWidth(width);
        setHeight(height);
        setThickness(thickness);
        setColor(color);
    }

    public Ring(int x, int y, int radius, int thickness, Color color) {
        this(x, y, radius * 2, radius * 2, thickness, color);
    }

    public void draw(Graphics2D gg, Color color) {
        Stroke oldStroke = gg.getStroke();
        Color oldColor = gg.getColor();

        gg.setColor(color);
        gg.setStroke(new BasicStroke(getThickness()));
        gg.drawOval(getX() + getThickness() / 2, getY() + getThickness() / 2, getWidth() - getThickness(),
                getHeight() - getThickness());
        gg.setColor(getColor());
        gg.setStroke(new BasicStroke(getThickness() - 2));
        gg.drawOval(getX() + getThickness() / 2, getY() + getThickness() / 2, getWidth() - getThickness(),
                getHeight() - getThickness());

        gg.setStroke(oldStroke);
        gg.setColor(oldColor);
    }

    public int getX() {
        return x;
    }

    public void setX(int x) {
        this.x = x;
    }

    public int getY() {
        return y;
    }

    public void setY(int y) {
        this.y = y;
    }

    public int getWidth() {
        return width;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public int getHeight() {
        return height;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public int getThickness() {
        return thickness;
    }

    public void setThickness(int thickness) {
        this.thickness = thickness;
    }

    public Color getColor() {
        return color;
    }

    public void setColor(Color color) {
        this.color = color;
    }

}