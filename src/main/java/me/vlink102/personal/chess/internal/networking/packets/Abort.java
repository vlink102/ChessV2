package me.vlink102.personal.chess.internal.networking.packets;

import nl.andrewl.record_net.Message;
import org.json.JSONObject;

public record Abort(String UUID) implements Message {
    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        object.put("uuid", UUID);
        return object.toString();
    }
}
