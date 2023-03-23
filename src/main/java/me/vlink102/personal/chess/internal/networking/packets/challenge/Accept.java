package me.vlink102.personal.chess.internal.networking.packets.challenge;

import org.json.JSONObject;

public record Accept(Long challengeID, String opponent) {
    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        object.put("accepted-challenge", challengeID);
        object.put("opponent", opponent);
        return object.toString();
    }
}
