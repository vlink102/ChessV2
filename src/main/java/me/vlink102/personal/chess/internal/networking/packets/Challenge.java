package me.vlink102.personal.chess.internal.networking.packets;

import nl.andrewl.record_net.Message;
import org.json.JSONObject;

public record Challenge(String from, String to) implements Message {
    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        object.put("challenger", from);
        object.put("challenged", to);
        return object.toString();
    }
}
