package me.vlink102.personal.chess.internal.networking.packets.game.draw;

import org.json.JSONObject;

public record Decline(String uuid, long gameID) {
    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        o.put("declined-draw-uuid", uuid);
        o.put("declined-draw-game-id", gameID);
        return o.toString();
    }
}
