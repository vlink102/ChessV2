package me.vlink102.personal.chess.internal.networking;

import me.vlink102.personal.chess.ChessMenu;
import me.vlink102.personal.chess.internal.networking.packets.Online;
import me.vlink102.personal.chess.internal.networking.packets.RequestOnline;
import nl.andrewl.record_net.util.ExtendedDataInputStream;
import nl.andrewl.record_net.util.ExtendedDataOutputStream;

import java.io.*;
import java.net.*;
import java.sql.*;
import java.util.Objects;


public class CommunicationHandler {
    private static final String V = "ZF16eYBBOuatpyxqOIHg";
    private static String user;
    private static String host;
    private static String dbName;
    private static int port;

    public static DataThread thread;

    public CommunicationHandler(String user, String host, String dbName, int port) {
        CommunicationHandler.user = user;
        CommunicationHandler.host = host;
        CommunicationHandler.dbName = dbName;
        CommunicationHandler.port = port;
    }

    private static String pws = V;

    private static Socket socket;
    private DatagramSocket datagramSocket;
    private ExtendedDataInputStream inputStream;
    private ExtendedDataOutputStream outputStream;

    public static String nameFromUUID(String uuid) {
        String url = "jdbc:mysql://" + user + ":" + pws + "@" + host + ":" + port + "/" + dbName;
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `PLAYERS` WHERE `uuid`='" + uuid + "';");
             ResultSet set = statement.executeQuery()) {
            if (set.next()) {
                return set.getString("name");
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static String UUIDfromName(String name) {
        String url = "jdbc:mysql://" + user + ":" + pws + "@" + host + ":" + port + "/" + dbName;
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `PLAYERS` WHERE `name`='" + name + "';");
             ResultSet set = statement.executeQuery()) {
            if (set.next()) {
                return set.getString("uuid");
            } else {
                return null;
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public static boolean validateLogin(String name, String password) {
        String url = "jdbc:mysql://" + user + ":" + pws + "@" + host + ":" + port + "/" + dbName;
        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement statement = connection.prepareStatement("SELECT * FROM `PLAYERS` WHERE `name`='" + name + "';");
             ResultSet set = statement.executeQuery()) {
            if (set.next()) {
                if (Objects.equals(set.getString("password"), password)) {
                    ChessMenu.IDENTIFIER = set.getString("uuid");
                    return true;
                }
            }
            return false;
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public static void establishConnection(String uuid) {
        try {
            InetAddress address = InetAddress.getByName("82.69.40.208");
            socket = new Socket(address.getHostName(), 55285);

            thread = new DataThread(socket);
            thread.start();

            thread.sendPacket(new Online(uuid));
            thread.sendPacket(new RequestOnline(uuid));

        } catch (ConnectException e) {
            System.out.println("Connection refused: " + e.getCause());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


    }

}
