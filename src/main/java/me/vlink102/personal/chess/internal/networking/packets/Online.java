package me.vlink102.personal.chess.internal.networking.packets;

import nl.andrewl.record_net.Message;
import org.json.JSONObject;

import java.util.UUID;

public record Online(String uuid) implements Message {
    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        o.put("online_uuid", uuid);
        return o.toString();
    }
}
