package me.vlink102.personal.chess;

public record BoardCoordinate(int row, int col) {
    public BoardCoordinate(int row, int col) {
        this.row = Math.max(0, Math.min(BoardGUI.decBoardSize, row));
        this.col = Math.max(0, Math.min(BoardGUI.decBoardSize, col));
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

    public static boolean isValidTile(BoardGUI boardGUI, String tile) {
        if (tile.equals("")) {
            return false;
        }
        String letterComponent = tile.substring(0, 1);
        String numberComponent = tile.substring(1);
        if (!letterComponent.matches("[a-z]")) return false;
        return Integer.parseInt(numberComponent) <= boardGUI.getBoardSize() && Integer.parseInt(numberComponent) > 0;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardCoordinate that = (BoardCoordinate) o;
        return row == that.row && col == that.col;
    }
}
