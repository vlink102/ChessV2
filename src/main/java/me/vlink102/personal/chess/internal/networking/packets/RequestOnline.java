package me.vlink102.personal.chess.internal.networking.packets;

import org.json.JSONObject;

public record RequestOnline(String uuid) {
    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        o.put("online_request_uuid", uuid);
        return o.toString();
    }
}
