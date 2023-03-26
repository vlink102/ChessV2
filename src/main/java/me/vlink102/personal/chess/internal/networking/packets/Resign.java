package me.vlink102.personal.chess.internal.networking.packets;

import org.json.JSONObject;

public record Resign(String UUID, long gameID) {
    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        o.put("resign_uuid", UUID);
        o.put("resign-game-id", gameID);
        return o.toString();
    }
}
