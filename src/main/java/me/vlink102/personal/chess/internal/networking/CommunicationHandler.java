package me.vlink102.personal.chess.internal.networking;

import me.vlink102.personal.chess.internal.networking.packets.Abort;
import me.vlink102.personal.chess.internal.networking.packets.Online;
import nl.andrewl.record_net.Message;
import nl.andrewl.record_net.Serializer;
import nl.andrewl.record_net.util.ExtendedDataInputStream;
import nl.andrewl.record_net.util.ExtendedDataOutputStream;

import java.io.*;
import java.net.*;


public class CommunicationHandler {
    private static Socket socket;
    private DatagramSocket datagramSocket;
    private ExtendedDataInputStream inputStream;
    private ExtendedDataOutputStream outputStream;

    public static void establishConnection(String uuid) {
        try {
            socket = new Socket("127.0.0.1", 55285);

            sendPacket(new Online(uuid));

            InputStreamReader in = new InputStreamReader(socket.getInputStream());
            BufferedReader bf = new BufferedReader(in);

            String str = bf.readLine();
            System.out.println("Server [" + socket.getInetAddress() + ":" + socket.getPort() + "]: " + str);
        } catch (ConnectException e) {
            System.out.println("Connection refused: " + e.getCause());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void sendPacket(Object packet) {
        try {
            PrintWriter printWriter = new PrintWriter(socket.getOutputStream());
            printWriter.println(packet.toString());
            printWriter.flush();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
