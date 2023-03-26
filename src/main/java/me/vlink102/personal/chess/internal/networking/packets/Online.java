package me.vlink102.personal.chess.internal.networking.packets;


import org.json.JSONObject;

public record Online(String uuid) {
    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        o.put("online_uuid", uuid);
        return o.toString();
    }
}
