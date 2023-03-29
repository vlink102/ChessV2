package me.vlink102.personal.chess.internal.networking.packets.challenge;

import org.json.JSONObject;

public record Accept(Long challengeID, String opponent, JSONObject challengedChoices) {
    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        object.put("accepted-challenge", challengeID);
        object.put("opponent", opponent);
        object.put("challenged-data", challengedChoices);
        return object.toString();
    }
}
