package me.vlink102.personal.chess.internal.networking.packets;

import nl.andrewl.record_net.Message;
import org.json.JSONObject;

public record InitGame(String uuid, String opponent) implements Message {
    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        o.put("uuid", uuid);
        o.put("opponent", opponent);
        return o.toString();
    }
}
