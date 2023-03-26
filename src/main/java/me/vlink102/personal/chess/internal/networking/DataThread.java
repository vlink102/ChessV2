package me.vlink102.personal.chess.internal.networking;

import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Chess;
import me.vlink102.personal.chess.ChessMenu;
import me.vlink102.personal.chess.internal.BoardCoordinate;
import me.vlink102.personal.chess.internal.Move;
import me.vlink102.personal.chess.internal.networking.packets.challenge.Accept;
import me.vlink102.personal.chess.internal.networking.packets.challenge.Decline;
import me.vlink102.personal.chess.pieces.Piece;
import org.json.JSONObject;

import javax.swing.*;
import javax.swing.text.Highlighter;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class DataThread extends Thread {
    private final BufferedReader bufferedReader;
    private final PrintWriter printWriter;

    public DataThread(Socket socket) {
        this.pendingChallenges = new HashMap<>();
        try {
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.printWriter = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject onlinePlayers;

    private final HashMap<Long, PendingChallenge> pendingChallenges;

    @Override
    public void run() {
        try {
            String data;
            while ((data = bufferedReader.readLine()) != null) {
                if (data.startsWith("{")) {
                    JSONObject object = new JSONObject(data);
                    Set<String> keys = object.keySet();
                    if (keys.contains("online_players")) {
                        onlinePlayers = object.getJSONObject("online_players");
                        onlinePlayers.remove(ChessMenu.IDENTIFIER);
                    }
                    if (keys.contains("challenge-id")) {
                        if (!Objects.equals(object.getString("challenged"), ChessMenu.IDENTIFIER)) {
                            System.out.println("Error: Server sent challenge with unequal UUID");
                            break;
                        }
                        if (Chess.createChallengeAcceptWindow(object, object.getJSONObject("data")) == JOptionPane.OK_OPTION) {
                            sendPacket(new Accept(object.getLong("challenge-id"), object.getString("challenger")));
                        } else {
                            sendPacket(new Decline(object.getLong("challenge-id"), object.getString("challenger")));
                        }
                    }
                    if (keys.contains("accepted-challenge-challenger")) {
                        long challengeID = object.getLong("accepted-challenge-challenger");
                        PendingChallenge o = pendingChallenges.get(challengeID);
                        loadInstanceFromJSON(o);
                        pendingChallenges.remove(challengeID);
                    }
                    if (keys.contains("accepted-challenge-challenged")) {
                        JSONObject challengeObject = object.getJSONObject("accepted-challenge-challenged");
                        loadInstanceFromJSON(challengeObject);
                    }
                    if (keys.contains("declined-challenge")) {
                        long challengeID = object.getLong("declined-challenge");
                        JSONObject declinedChallengeData = pendingChallenges.get(challengeID).challenge();
                        Chess.createChallengeDeclinedWindow(declinedChallengeData);
                        pendingChallenges.remove(challengeID);
                    }
                    if (keys.contains("game-id")) {
                        // Is move
                        long gameID = object.getLong("game-id");
                        for (Chess instance : ChessMenu.getInstances()) {
                            if (instance.getBoard().getGameID() == gameID) {
                                BoardGUI boardGUI = instance.getBoard();
                                Move move = parseMove(object);
                                System.out.println(move);
                                boardGUI.moveHighlight(move.getFrom(), move.getTo());
                                if (move.getCastleType() == null) {
                                    boardGUI.rawMove(move.getPiece(), move.getTakeSquare(), move.getFrom(), move.getTo());
                                } else {
                                    boardGUI.rawCastle(move.getPiece().isWhite(), move.getCastleType());
                                }
                                boardGUI.endComputerTurn();
                            }
                        }
                    }
                    if (keys.contains("resign-game-id")) {
                        long gameID = object.getLong("resign-game-id");
                        for (Chess instance : ChessMenu.getInstances()) {
                            if (instance.getBoard().getGameID() == gameID) {
                                BoardGUI boardGUI = instance.getBoard();
                                boardGUI.oppositionResign();
                            }
                        }
                    }
                    if (keys.contains("abort-game-id")) {
                        long gameID = object.getLong("abort-game-id");
                        for (Chess instance : ChessMenu.getInstances()) {
                            if (instance.getBoard().getGameID() == gameID) {
                                BoardGUI boardGUI = instance.getBoard();
                                boardGUI.oppositionAbort();
                            }
                        }
                    }
                    if (keys.contains("draw-game-id")) {
                        long gameID = object.getLong("draw-game-id");
                        for (Chess instance : ChessMenu.getInstances()) {
                            if (instance.getBoard().getGameID() == gameID) {
                                if (instance.createDrawGameWindow(instance.getBoard(), object) == JOptionPane.OK_OPTION) {
                                    sendPacket(new me.vlink102.personal.chess.internal.networking.packets.draw.Accept(object.getString("draw_uuid"), gameID));
                                    instance.getBoard().draw();
                                } else {
                                    sendPacket(new me.vlink102.personal.chess.internal.networking.packets.draw.Decline(object.getString("draw_uuid"), gameID));
                                }
                            }
                        }
                    }
                    if (keys.contains("accepted-draw-game-id")) {
                        long gameID = object.getLong("accepted-draw-game-id");
                        for (Chess instance : ChessMenu.getInstances()) {
                            if (instance.getBoard().getGameID() == gameID) {
                                BoardGUI boardGUI = instance.getBoard();
                                boardGUI.draw();
                            }
                        }
                    }
                    if (keys.contains("declined-draw-game-id")) {
                        long gameID = object.getLong("declined-draw-game-id");
                        for (Chess instance : ChessMenu.getInstances()) {
                            if (instance.getBoard().getGameID() == gameID) {
                                instance.createPopUp(CommunicationHandler.nameFromUUID(object.getString("declined-draw-game-uuid")) + " declined your draw offer.", "Draw declined", Move.MoveHighlights.FORCED);
                            }
                        }
                    }
                }
                System.out.println(data);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Move parseMove(JSONObject object) {
        long gameID = object.getLong("game-id");
        for (Chess instance : ChessMenu.getInstances()) {
            if (instance.getBoard().getGameID() == gameID) {
                BoardGUI boardGUI = instance.getBoard();
                JSONObject move = object.getJSONObject("move");

                BoardCoordinate from = BoardCoordinate.parse(new JSONObject(move.getString("from")));
                BoardCoordinate to = BoardCoordinate.parse(new JSONObject(move.getString("to")));

                JSONObject pieceJSON = new JSONObject(move.getString("piece"));
                Piece piece;
                if (pieceJSON.getString("abbr").equals("R")) {
                    piece = Piece.parse(pieceJSON, boardGUI, BoardCoordinate.parse(new JSONObject(pieceJSON.getJSONObject("rook-data").getString("initial-square"))));
                } else {
                    piece = Piece.parse(pieceJSON, boardGUI, null);
                }

                String checkString = move.getString("check");
                String takeString = move.getString("take-square");
                String takenString = move.getString("taken");
                String promoteString = move.getString("promotes");
                String castleType = move.getString("castle-type");
                return new Move(
                        piece,
                        from,
                        to,
                        Objects.equals(checkString, "null") ? null : Move.Check.valueOf(checkString),
                        move.getBoolean("en-passant"),
                        Objects.equals(takeString, "null") ? null : BoardCoordinate.parse(new JSONObject(takeString)),
                        Objects.equals(takenString, "null") ? null : Piece.parse(new JSONObject(takenString), boardGUI, null),
                        Objects.equals(promoteString, "null") ? null : Piece.parse(new JSONObject(promoteString), boardGUI, to),
                        Objects.equals(castleType, "null") ? null : Move.CastleType.valueOf(castleType),
                        Move.MoveType.valueOf(move.getString("type"))
                );
            }
        }
        return null;
    }

    /**
     * Always the receiver (hence JSON)
     */
    private void loadInstanceFromJSON(JSONObject challengeObject) {
        JSONObject challengeData = challengeObject.getJSONObject("data");
        ChessMenu.getInstances().add(new Chess(true,
                challengeObject.getLong("challenge-id"),
                challengeObject.getString("challenger"),
                challengeData.getString("board"),
                100,
                challengeData.getInt("board-size"),
                true,
                !challengeData.getBoolean("white"),
                BoardGUI.OpponentType.PLAYER,
                BoardGUI.GameType.valueOf(challengeData.getString("game-type")),
                Chess.BoardLayout.valueOf(challengeData.getString("layout")),
                BoardGUI.PieceDesign.NEO,
                BoardGUI.Colours.GREEN,
                BoardGUI.MoveStyle.BOTH,
                BoardGUI.HintStyle.Move.DOT,
                BoardGUI.HintStyle.Capture.RING,
                BoardGUI.CoordinateDisplayType.INSIDE)
        );
    }

    /**
     * Always the challenger (hence why pending challenge object available)
     */
    private void loadInstanceFromJSON(PendingChallenge challenge) {
        JSONObject challengeObject = challenge.challenge();
        JSONObject challengeData = challengeObject.getJSONObject("data");
        ChessMenu.getInstances().add(new Chess(true,
                challengeObject.getLong("challenge-id"),
                challengeObject.getString("challenged"),
                challengeData.getString("board"),
                100,
                challengeData.getInt("board-size"),
                true,
                challengeData.getBoolean("white"),
                BoardGUI.OpponentType.PLAYER,
                BoardGUI.GameType.valueOf(challengeData.getString("game-type")),
                Chess.BoardLayout.valueOf(challengeData.getString("layout")),
                challenge.pieceDesign(),
                challenge.boardDesign(),
                challenge.moveMethod(),
                challenge.moveStyle(),
                challenge.captureStyle(),
                challenge.coordinateDisplayType())
        );
    }

    public void sendPacket(Object packet) {
        printWriter.println(packet.toString());
        printWriter.flush();
    }

    public HashMap<Long, PendingChallenge> getPendingChallenges() {
        return pendingChallenges;
    }

    public record PendingChallenge(JSONObject challenge, BoardGUI.PieceDesign pieceDesign, BoardGUI.Colours boardDesign, BoardGUI.MoveStyle moveMethod, BoardGUI.HintStyle.Move moveStyle, BoardGUI.HintStyle.Capture captureStyle, BoardGUI.CoordinateDisplayType coordinateDisplayType) {
    }
}
