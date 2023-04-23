package me.vlink102.personal.chess.internal.networking.packets;

import me.vlink102.personal.GameSelector;
import org.json.JSONObject;

public record VersionControl(String uuid) {
    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        o.put("version", GameSelector.VERSION);
        o.put("uuid", uuid);
        return o.toString();
    }
}
