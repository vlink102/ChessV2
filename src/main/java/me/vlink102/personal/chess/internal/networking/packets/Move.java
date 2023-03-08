package me.vlink102.personal.chess.internal.networking.packets;

import nl.andrewl.record_net.Message;
import org.json.JSONObject;

public record Move(String blackUUID, String whiteUUID, me.vlink102.personal.chess.internal.Move move) implements Message {
    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        o.put("black_uuid", blackUUID);
        o.put("white_uuid", whiteUUID);
        JSONObject moveObj = new JSONObject();
        moveObj.put("from", move.getFrom().toString());
        moveObj.put("to", move.getTo().toString());
        moveObj.put("piece", move.getPiece().toString());
        o.put("move", moveObj);
        return o.toString();
    }
}
