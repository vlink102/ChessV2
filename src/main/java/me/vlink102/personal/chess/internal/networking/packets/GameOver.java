package me.vlink102.personal.chess.internal.networking.packets;

import nl.andrewl.record_net.Message;
import org.json.JSONObject;

public record GameOver(String winner, String loser, boolean draw) implements Message {
    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        object.put("winner_uuid", winner);
        object.put("loser_uuid", loser);
        object.put("draw", draw);
        return object.toString();
    }
}
