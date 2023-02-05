package me.vlink102.personal.chess;


import java.util.Objects;

public class RawMove {
    private final BoardCoordinate from;
    private final BoardCoordinate to;
    public RawMove(BoardCoordinate from, BoardCoordinate to) {
        this.from = from;
        this.to = to;
    }

    public BoardCoordinate getFrom() {
        return from;
    }

    public BoardCoordinate getTo() {
        return to;
    }

    public boolean isSimilar() {
        return (Objects.equals(from.getCol(), to.getCol()) && Objects.equals(from.getRow(), to.getRow()));
    }
}
