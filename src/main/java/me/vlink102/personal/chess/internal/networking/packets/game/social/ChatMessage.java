package me.vlink102.personal.chess.internal.networking.packets.game.social;

import org.json.JSONObject;

public record ChatMessage(String uuid, String contents, long gameID) {
    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        o.put("chat-uuid", uuid);
        o.put("chat-message", contents);
        o.put("chat-game-id", gameID);
        return o.toString();
    }
}
