package me.vlink102.personal.chess.internal.networking.packets.draw;

import org.json.JSONObject;

public record Accept(String uuid, long gameID) {
    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        o.put("accepted-draw-uuid", uuid);
        o.put("accepted-draw-game-id", gameID);
        return o.toString();
    }
}
