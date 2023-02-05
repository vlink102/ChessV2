package me.vlink102.personal.chess;

public class BoardCoordinate {
    private final int row;
    private final int col;

    public BoardCoordinate(int row, int col) {
        this.row = row;
        this.col = col;
    }

    public int getRow() {
        return row;
    }

    public int getCol() {
        return col;
    }

    public String getRowString() {
        return String.valueOf(row + 1);
    }

    public String getColString() {
        return String.valueOf(Character.toChars(col + 'a')[0]);
    }

    public String toNotation() {
        return Character.toChars(col + 'a')[0] + String.valueOf(row + 1);
    }
}
