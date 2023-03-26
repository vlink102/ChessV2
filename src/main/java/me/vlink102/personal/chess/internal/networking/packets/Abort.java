package me.vlink102.personal.chess.internal.networking.packets;


import org.json.JSONObject;

public record Abort(String UUID, long gameID) {
    @Override
    public String toString() {
        JSONObject object = new JSONObject();
        object.put("abort_uuid", UUID);
        object.put("abort-game-id", gameID);
        return object.toString();
    }
}
