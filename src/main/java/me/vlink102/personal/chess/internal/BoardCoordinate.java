package me.vlink102.personal.chess.internal;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.classroom.ClassroomGUI;
import org.json.JSONObject;

public class BoardCoordinate {
    private final int row;
    private final int col;

    public int col() {
        return col;
    }

    public int row() {
        return row;
    }

    public BoardCoordinate(int row, int col) {
        this.row = Math.max(0, Math.min(BoardGUI.decBoardSize, row));
        this.col = Math.max(0, Math.min(BoardGUI.decBoardSize, col));
    }

    public BoardCoordinate(int row, int col, ClassroomGUI gui) {
        this.row = Math.max(0, Math.min(gui.getBoardSize() - 1, row));
        this.col = Math.max(0, Math.min(gui.getBoardSize() - 1, col));
    }

    public String getRowString() {
        return getRowString(row);
    }

    public String getColString() {
        return getColString(col);
    }

    public static String getRowString(int row) {
        return String.valueOf(row + 1);
    }

    public static String getColString(int col) {
        return String.valueOf(Character.toChars(col + 'a')[0]);
    }

    public String toNotation() {
        return Character.toChars(col + 'a')[0] + String.valueOf(row + 1);
    }

    public static int parseRow(String row) {
        return Integer.parseInt(row) - 1;
    }

    public static int parseCol(String col) {
        return col.toCharArray()[0] - 'a';
    }

    public static boolean isValidTile(int boardSize, String tile) {
        if (tile.equals("")) {
            return false;
        }
        String letterComponent = tile.substring(0, 1);
        String numberComponent = tile.substring(1);
        if (!letterComponent.matches("[a-z]")) return false;
        return Integer.parseInt(numberComponent) <= boardSize && Integer.parseInt(numberComponent) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardCoordinate that = (BoardCoordinate) o;
        return row == that.row && col == that.col;
    }

    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        o.put("row", row);
        o.put("col", col);
        return o.toString();
    }
}
