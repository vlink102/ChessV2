package me.vlink102.personal.chess.internal.networking.packets;

import nl.andrewl.record_net.Message;
import org.json.JSONObject;

public record OfferDraw(String UUID) implements Message {
    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        o.put("draw_uuid", UUID);
        return o.toString();
    }
}
