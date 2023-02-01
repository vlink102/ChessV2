package me.vlink102.personal.chess;

import java.awt.*;
import java.util.List;

public abstract class Piece {
    private final BoardGUI board;
    private Image icon;
    private final String abbr;
    private boolean white;
    private int moves;
    private List<Move> history;

    public Piece(BoardGUI board, String abbr, boolean white) {
        this.board = board;
        this.abbr = abbr;
        this.white = white;
        this.moves = 0;
        this.icon = OnlineAssets.getPiece(board, this);
    }

    public void addMove(Move move) {
        history.add(move);
    }

    /**
     * All parameters in raw format
     * @param from
     * @param to
     * @param capture
     * @return
     */
    public abstract boolean validMove(BoardCoordinate from, BoardCoordinate to, boolean capture);

    public void paint(Graphics g, int x, int y, int pieceSize) {
        g.drawImage(icon.getScaledInstance(pieceSize, pieceSize, Image.SCALE_SMOOTH), x, y, null);
    }

    public boolean isDiagonal(BoardCoordinate from, BoardCoordinate to) {
        return Math.abs(from.getFile() - to.getFile()) == Math.abs(from.getRank() - to.getRank());
    }
    public boolean isStraight(BoardCoordinate from, BoardCoordinate to) {
        return (from.getFile() == to.getFile() && from.getRank() != to.getRank()) || (from.getFile() != to.getFile() && from.getRank() == to.getRank());
    }

    public String getAbbr() {
        return abbr;
    }
    public Image getIcon(int pieceSize) {
        return icon.getScaledInstance(pieceSize, pieceSize, Image.SCALE_SMOOTH);
    }
    public boolean isWhite() {
        return white;
    }

    public void incrementMoves() {
        this.moves ++;
    }

    public List<Move> getHistory() {
        return history;
    }

    public void setIcon(Image icon) {
        this.icon = icon;
    }




}
