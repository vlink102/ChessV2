package me.vlink102.personal.chess;

import java.awt.*;
import java.util.Objects;

public class BoardCoordinate {
    private CoordinateType type;
    private int rank;
    private int file;

    /**
     * @param file Params from 0-7 (Chess notation)
     * @param rank Params from 0-7 (Chess notation)
     * @param type Modifier
     */
    public BoardCoordinate(int file, int rank, CoordinateType type) {
        this.file = file;
        this.rank = rank;
        this.type = type;
    }

    public CoordinateType getType() {
        return type;
    }



    public BoardCoordinate convert(CoordinateType convertType) {
        BoardCoordinate newCoord = new BoardCoordinate(file, rank, type);
        switch (type) {                    // R (Y), F (X)    R (Y), F (X)
            case RAW -> {                       // 0      0        7      7
                switch (convertType) {
                    case ONE_EIGHT -> {         // 8      1        1      8
                        newCoord.rank = (7 - file) + 1;
                        newCoord.file += 1;
                    }
                    case ZERO_SEVEN -> {        // 7      0        0      7
                        newCoord.rank = 7 - rank;
                    }
                }
            }
            case ONE_EIGHT -> {                 // 8      1        1      8
                switch (convertType) {
                    case RAW -> {               // 0      0        7      7
                        newCoord.rank = (7 - rank) + 1;
                        newCoord.file -= 1;
                    }
                    case ZERO_SEVEN -> {        // 7      0        0      7
                        newCoord.rank -= 1;
                        newCoord.file -= 1;
                    }
                }
            }
            case ZERO_SEVEN -> {                // 7      0        0      7
                switch (convertType) {
                    case RAW -> {               // 0      0        7      7
                        newCoord.rank = 7 - rank;
                    }
                    case ONE_EIGHT -> {         // 8      1        1      8
                        newCoord.rank += 1;
                        newCoord.file += 1;
                    }
                }
            }
        }
        newCoord.type = convertType;
        return newCoord;
    }

    public enum CoordinateType {
        ZERO_SEVEN,
        ONE_EIGHT,
        RAW
    }

    public int getFile() {
        return file;
    }

    public int getRank() {
        return rank;
    }

    @Override
    public String toString() {
        return "BC{File: " + file + ", Rank: " + rank + ", Type: " + type + "}";
    }

    public String toNotation() {
        return Character.toChars(file)[0] + String.valueOf(rank);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BoardCoordinate that = (BoardCoordinate) o;
        return rank == that.rank && file == that.file && type == that.type;
    }

    @Override
    public int hashCode() {
        return Objects.hash(type, rank, file);
    }
}
