package me.vlink102.personal.chess.internal.networking.packets.game.draw;


import org.json.JSONObject;

public record OfferDraw(String UUID, long gameID) {
    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        o.put("draw_uuid", UUID);
        o.put("draw-game-id", gameID);
        return o.toString();
    }
}
