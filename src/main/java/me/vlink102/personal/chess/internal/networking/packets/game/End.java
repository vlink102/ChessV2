package me.vlink102.personal.chess.internal.networking.packets.game;

import org.json.JSONObject;

public record End(long gameID, int reason) {
    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        o.put("end-game-id", gameID);
        o.put("end-game-reason", reason);
        return o.toString();
    }
}
