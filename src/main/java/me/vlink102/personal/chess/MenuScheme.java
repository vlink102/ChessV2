package me.vlink102.personal.chess;

import java.awt.*;

public class MenuScheme {
    private final Color background;
    private final Color darkerBackground;
    private final Color buttonColor;
    private final Color pieceHistoryAlternateColor;
    private final Color coordinateBarColor;
    private final Color coordinateBarTextColor;

    public MenuScheme(Color background, Color darkerBackground, Color buttonColor, Color pieceHistoryAlternateColor, Color coordinateBarColor, Color coordinateBarTextColor) {
        this.background = background;
        this.darkerBackground = darkerBackground;
        this.buttonColor = buttonColor;
        this.pieceHistoryAlternateColor = pieceHistoryAlternateColor;
        this.coordinateBarColor = coordinateBarColor;
        this.coordinateBarTextColor = coordinateBarTextColor;
    }

    public Color getBackground() {
        return background;
    }

    public Color getDarkerBackground() {
        return darkerBackground;
    }

    public Color getButtonColor() {
        return buttonColor;
    }

    public Color getPieceHistoryAlternateColor() {
        return pieceHistoryAlternateColor;
    }

    public Color getCoordinateBarColor() {
        return coordinateBarColor;
    }

    public Color getCoordinateBarTextColor() {
        return coordinateBarTextColor;
    }
}
