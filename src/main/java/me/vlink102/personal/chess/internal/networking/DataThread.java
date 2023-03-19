package me.vlink102.personal.chess.internal.networking;

import org.json.JSONObject;

import java.io.*;
import java.net.Socket;

public class DataThread extends Thread {
    private final Socket socket;
    private final DataOutputStream outputStream;
    private final BufferedReader bufferedReader;
    private final PrintWriter printWriter;

    public DataThread(Socket socket) {
        this.socket = socket;
        try {
            this.outputStream = new DataOutputStream(socket.getOutputStream());
            this.bufferedReader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            this.printWriter = new PrintWriter(socket.getOutputStream());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public JSONObject onlinePlayers;

    @Override
    public void run() {
        try {
            String data;
            while ((data = bufferedReader.readLine()) != null) {
                if (data.startsWith("{")) {
                    JSONObject object = new JSONObject(data);
                    if (object.keySet().contains("online_players")) {
                        onlinePlayers = object.getJSONObject("online_players");
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
}
