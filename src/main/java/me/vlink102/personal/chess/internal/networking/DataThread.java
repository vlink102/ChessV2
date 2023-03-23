package me.vlink102.personal.chess.internal.networking;

import me.vlink102.personal.chess.Chess;
import me.vlink102.personal.chess.ChessMenu;
import me.vlink102.personal.chess.internal.networking.packets.challenge.Accept;
import me.vlink102.personal.chess.internal.networking.packets.challenge.Decline;
import org.json.JSONObject;

import javax.swing.*;
import java.io.*;
import java.net.Socket;
import java.util.*;

public class DataThread extends Thread {
    private final Socket socket;
    private final DataOutputStream outputStream;
    private final BufferedReader bufferedReader;
    private final PrintWriter printWriter;

    public DataThread(Socket socket) {
        this.socket = socket;
        this.pendingChallenges = new HashMap<>();
        try {
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.printWriter = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject onlinePlayers;

    private final HashMap<Long, JSONObject> pendingChallenges;

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
                }
                System.out.println(data);
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void sendPacket(Object packet) {
        printWriter.println(packet.toString());
        printWriter.flush();
    }

    public HashMap<Long, JSONObject> getPendingChallenges() {
        return pendingChallenges;
    }
}
