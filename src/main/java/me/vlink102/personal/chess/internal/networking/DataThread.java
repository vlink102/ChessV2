package me.vlink102.personal.chess.internal.networking;

import com.mysql.cj.conf.PropertyDefinitions;
import me.vlink102.personal.GameSelector;
import me.vlink102.personal.chess.BoardGUI;
import me.vlink102.personal.chess.Chess;
import me.vlink102.personal.chess.ChessMenu;
import me.vlink102.personal.chess.internal.BoardCoordinate;
import me.vlink102.personal.chess.internal.Move;
import me.vlink102.personal.chess.internal.SwingLink;
import me.vlink102.personal.chess.internal.networking.packets.challenge.Accept;
import me.vlink102.personal.chess.internal.networking.packets.challenge.Challenge;
import me.vlink102.personal.chess.internal.networking.packets.challenge.Decline;
import me.vlink102.personal.chess.pieces.Piece;
import org.json.JSONObject;

import javax.swing.*;
import java.awt.*;
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
                    if (keys.contains("closed-id")) {
                        String reason = object.getString("reason");
                        String title = object.getString("closed-id");

                        JOptionPane.showMessageDialog(null, reason, "Connection lost: " + title, JOptionPane.INFORMATION_MESSAGE, null);
                        System.exit(0);
                    }
                    if (keys.contains("version-control-result")) {
                        boolean validVersion = object.getBoolean("version-control-result");
                        if (!validVersion) {
                            String correctVersion = object.getString("correct-version");
                            String newLink = object.getString("update-link");
                            JLabel link = new JLabel("You are using an outdated version!\n\nClient: " + GameSelector.VERSION + "\nServer: " + correctVersion + "\n\nUpdate: ");
                            SwingLink swingLink = new SwingLink(" - " + newLink, newLink);
                            JPanel updatePanel = new JPanel();
                            updatePanel.setLayout(new BoxLayout(updatePanel, BoxLayout.Y_AXIS));
                            updatePanel.add(Box.createRigidArea(new Dimension(updatePanel.getWidth(), 20)));
                            updatePanel.add(link);
                            updatePanel.add(swingLink);
                            updatePanel.add(Box.createRigidArea(new Dimension(updatePanel.getWidth(), 20)));
                            JOptionPane.showMessageDialog(null, updatePanel, "Invalid Version", JOptionPane.ERROR_MESSAGE, new ImageIcon(Move.getMoveHighlightIcon(Move.MoveHighlights.MISTAKE).getScaledInstance(50, 50, Image.SCALE_SMOOTH)));
                            System.exit(0);
                        }
                        boolean banned = object.getBoolean("banned");
                        if (banned) {
                            JOptionPane.showMessageDialog(null, "You are banned", "Unable to connect", JOptionPane.INFORMATION_MESSAGE, null);
                            System.exit(0);
                        }
                    }
                    if (keys.contains("online_players")) {
                        onlinePlayers = object.getJSONObject("online_players");
                        onlinePlayers.remove(ChessMenu.IDENTIFIER);
                    }
                    if (keys.contains("challenge-id")) {
                        if (!Objects.equals(object.getString("challenged"), ChessMenu.IDENTIFIER)) {
                            System.out.println("Error: Server sent challenge with unequal UUID");
                            break;
                        }
                        Chess.ChallengeAcceptResult challengeAcceptResult = Chess.createChallengeAcceptWindow(object, object.getJSONObject("data"));
                        if (challengeAcceptResult != null) {
                            JSONObject o = new JSONObject();
                            o.put("piece-size", challengeAcceptResult.pSz());
                            o.put("board-theme", challengeAcceptResult.boardTheme());
                            o.put("piece-theme", challengeAcceptResult.pieceTheme());
                            o.put("move-method", challengeAcceptResult.moveMethod());
                            o.put("move-style", challengeAcceptResult.moveStyle());
                            o.put("capture-style", challengeAcceptResult.captureStyle());
                            o.put("coordinate-display", challengeAcceptResult.coordinateDisplayType());
                            sendPacket(new Accept(object.getLong("challenge-id"), object.getString("challenger"), o));
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
                        loadInstanceFromJSON(challengeObject, object.getJSONObject("challenged-data"));
                    }
                    if (keys.contains("declined-challenge")) {
                        long challengeID = object.getLong("declined-challenge");
                        JSONObject declinedChallengeData = pendingChallenges.get(challengeID).challenge();
                        Chess.createChallengeDeclinedWindow(declinedChallengeData);
                        pendingChallenges.remove(challengeID);
                    }
                    if (keys.contains("chat-uuid")) {
                        long gameID = object.getLong("chat-game-id");
                        for (Chess instance : ChessMenu.getInstances()) {
                            if (instance.getBoard().getGameID() == gameID) {
                                BoardGUI boardGUI = instance.getBoard();
                                String message = object.getString("chat-message");
                                boardGUI.getChatGUI().addMessage(message, false);
                            }
                        }
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
                                    boardGUI.rawMove(move.getPiece(), move.getTakeSquare(), move.getFrom(), move.getTo(), false);
                                } else {
                                    boardGUI.rawCastle(move.getPiece().isWhite(), move.getCastleType());
                                }
                                boardGUI.endComputerTurn();
                            }
                        }
                    }
                    if (keys.contains("end-game-id")) {
                        long gameID = object.getLong("end-game-id");
                        for (Chess instance : ChessMenu.getInstances()) {
                            if (instance.getBoard().getGameID() == gameID) {
                                int oldRating = object.getInt("end-game-rating-old");
                                int newRating = object.getInt("end-game-rating-new");

                                int reason = object.getInt("end-game-reason");

                                BoardGUI boardGUI = instance.getBoard();
                                boardGUI.finaliseGame(convert(reason), oldRating, newRating);
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
                                    sendPacket(new me.vlink102.personal.chess.internal.networking.packets.game.draw.Accept(object.getString("draw_uuid"), gameID));
                                    instance.getBoard().draw();
                                } else {
                                    sendPacket(new me.vlink102.personal.chess.internal.networking.packets.game.draw.Decline(object.getString("draw_uuid"), gameID));
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

    public BoardGUI.GameOverType convert(int gameOverReason) {
        return switch (gameOverReason) {
            case 0 -> BoardGUI.GameOverType.STALEMATE;
            case 1 -> BoardGUI.GameOverType.CHECKMATE_WHITE;
            case 2 -> BoardGUI.GameOverType.CHECKMATE_BLACK;
            case 3 -> BoardGUI.GameOverType.ABORTED_WHITE;
            case 4 -> BoardGUI.GameOverType.ABORTED_BLACK;
            case 5 -> BoardGUI.GameOverType.DRAW_BY_REPETITION;
            case 6 -> BoardGUI.GameOverType.DRAW_BY_AGREEMENT;
            case 7 -> BoardGUI.GameOverType.ABANDONMENT_WHITE;
            case 8 -> BoardGUI.GameOverType.ABANDONMENT_BLACK;
            case 9 -> BoardGUI.GameOverType.TIME_WHITE;
            case 10 -> BoardGUI.GameOverType.TIME_BLACK;
            case 50 -> BoardGUI.GameOverType.FIFTY_MOVE_RULE;
            case 99 -> BoardGUI.GameOverType.INSUFFICIENT_MATERIAL;
            case 100 -> BoardGUI.GameOverType.RESIGNATION_WHITE;
            case 101 -> BoardGUI.GameOverType.RESIGNATION_BLACK;
            case 200 -> BoardGUI.GameOverType.ILLEGAL_POSITION;
            default -> throw new IllegalStateException("Unexpected value: " + gameOverReason);
        };
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
                        boardGUI,
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
    private void loadInstanceFromJSON(JSONObject challengeObject, JSONObject challengedData) {
        JSONObject challengeData = challengeObject.getJSONObject("data");

        ChessMenu.getInstances().add(new Chess(true,
                challengeObject.getLong("challenge-id"),
                challengeObject.getString("challenger"),
                challengeData.getString("board"),
                challengedData.getInt("piece-size"),
                challengeData.getInt("board-size"),
                true,
                !challengeData.getBoolean("white"),
                BoardGUI.OpponentType.PLAYER,
                BoardGUI.GameType.valueOf(challengeData.getString("game-type")),
                Chess.BoardLayout.valueOf(challengeData.getString("layout")),
                BoardGUI.PieceDesign.valueOf(challengedData.getString("piece-theme")),
                BoardGUI.Colours.valueOf(challengedData.getString("board-theme")),
                BoardGUI.MoveStyle.valueOf(challengedData.getString("move-method")),
                BoardGUI.HintStyle.Move.valueOf(challengedData.getString("move-style")),
                BoardGUI.HintStyle.Capture.valueOf(challengedData.getString("capture-style")),
                BoardGUI.CoordinateDisplayType.valueOf(challengedData.getString("coordinate-display")))
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
                challenge.pSz(),
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

    public record PendingChallenge(JSONObject challenge, int pSz, BoardGUI.PieceDesign pieceDesign, BoardGUI.Colours boardDesign, BoardGUI.MoveStyle moveMethod, BoardGUI.HintStyle.Move moveStyle, BoardGUI.HintStyle.Capture captureStyle, BoardGUI.CoordinateDisplayType coordinateDisplayType) {
    }
}
