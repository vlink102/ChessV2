package me.vlink102.personal.chess.internal.networking.packets.game;

import org.json.JSONObject;

public record Move(long gameID, String blackUUID, String whiteUUID, me.vlink102.personal.chess.internal.Move move) {
    @Override
    public String toString() {
        JSONObject o = new JSONObject();
        o.put("game-id", gameID);
        o.put("black_uuid", blackUUID);
        o.put("white_uuid", whiteUUID);
        JSONObject moveObj = new JSONObject();
        moveObj.put("from", move.getFrom() == null ? "null" : move.getFrom().toString());
        moveObj.put("to", move.getTo() == null ? "null" : move.getTo().toString());
        moveObj.put("piece", move.getPiece() == null ? "null" : move.getPiece().toString());
        moveObj.put("check", move.getCheck() == null ? "null" : move.getCheck().toString());
        moveObj.put("castle-type", move.getCastleType() == null ? "null" : move.getCastleType().toString());
        moveObj.put("promotes", move.getPromotes() == null ? "null" : move.getPromotes().toString());
        moveObj.put("take-square", move.getTakeSquare() == null ? "null" : move.getTakeSquare().toString());
        moveObj.put("taken", move.getTaken() == null ? "null" : move.getTaken().toString());
        moveObj.put("type", move.getType() == null ? "null" : move.getType().toString());
        moveObj.put("en-passant", move.isEnPassant());
        o.put("move", moveObj);
        return o.toString();
    }
}
