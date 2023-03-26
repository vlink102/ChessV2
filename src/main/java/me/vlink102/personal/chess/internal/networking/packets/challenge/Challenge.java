package me.vlink102.personal.chess.internal.networking.packets.challenge;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Chess;

import org.json.JSONObject;

import java.util.Random;

public class Challenge {

    private final String from;
    private final String to;
    private final int boardSize;
    private final boolean playAsWhite;
    private final BoardGUI.GameType gameType;
    private final Chess.BoardLayout layout;
    private final String startFEN;

    private final long ID;

    public Challenge(String from, String to, int boardSize, boolean playAsWhite, BoardGUI.GameType gameType, Chess.BoardLayout layout, String startFEN) {
        Random random = new Random();
        this.ID = Math.abs(random.nextLong());
        this.from = from;
        this.to = to;
        this.boardSize = boardSize;
        this.playAsWhite = playAsWhite;
        this.gameType = gameType;
        this.layout = layout;
        this.startFEN = startFEN;
    }

    public JSONObject toJSON() {
        JSONObject object = new JSONObject();
        object.put("challenge-id", ID);
        object.put("challenger", from);
        object.put("challenged", to);

        JSONObject challengeData = new JSONObject();
        challengeData.put("board-size", boardSize);
        challengeData.put("white", playAsWhite);
        challengeData.put("game-type", gameType.toString());
        challengeData.put("layout", layout.toString());
        challengeData.put("board", startFEN);
        object.put("data", challengeData);
        return object;
    }

    @Override
    public String toString() {
        return toJSON().toString();
    }

    public long getID() {
        return ID;
    }
}
