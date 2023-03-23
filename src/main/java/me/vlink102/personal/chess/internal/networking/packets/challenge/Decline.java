package me.vlink102.personal.chess.internal.networking.packets.challenge;

import org.json.JSONObject;

public record Decline(Long challengeID, String opponent) {
    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        object.put("declined-challenge", challengeID);
        object.put("opponent", opponent);
        return object.toString();
    }
}
