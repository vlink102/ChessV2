package me.vlink102.personal.chess.internal.networking;

import me.vlink102.personal.chess.ratings.Rating;
import me.vlink102.personal.chess.ratings.RatingPeriodResults;
import me.vlink102.personal.chess.ratings.Result;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MySQLConnection {
    private static final String V = "ZF16eYBBOuatpyxqOIHg";
    private final String user;
    private final String host;
    private final String dbName;
    private final int port;

    public MySQLConnection(String user, String host, String dbName, int port) {
        this.user = user;
        this.host = host;
        this.dbName = dbName;
        this.port = port;
    }

    public int getPort() {
        return port;
    }

    public String getHost() {
        return host;
    }

    public String getUser() {
        return user;
    }

    public String getDbName() {
        return dbName;
    }

    private static String pws = V;
    public static List<Rating> participants = new ArrayList<>();
    public static List<Result> results = new ArrayList<>();
    public static List<Rating> players = new ArrayList<>();

    public static boolean containsPlayer(List<Rating> players, String uuid) {
        for (Rating player : players) {
            if (player.getUuid().equals(uuid)) {
                return true;
            }
        }
        return false;
    }

    public static Rating getPlayer(List<Rating> players, String uuid) {
        for (Rating player : players) {
            if (player.getUuid().equals(uuid)) {
                return player;
            }
        }
        return null;
    }

    public static void addPlayer(Rating player) {
        if (players.contains(player)) {
            throw new IllegalArgumentException("Tried to add existing player: " + player.getName() + " (" + player.getUuid() + ")");
        } else {
            players.add(player);
        }
    }

    public void savePeriod(RatingPeriodResults results) {
        String url = "jdbc:mysql://" + user + ":" + pws + "@" + host + ":" + port + "/" + dbName;
        try (Connection connection = DriverManager.getConnection(url)) {
            connection.prepareStatement("DELETE FROM `PERIOD_RESULTS`;");
            for (Result periodResult : results.getResults()) {
                connection.prepareStatement("INSERT INTO `PERIOD_RESULTS` VALUES ('" +
                        periodResult.getWinner().getUuid() + "','" + periodResult.getLoser().getUuid() + "','" + periodResult.isDraw() + "');").execute();
            }
            connection.prepareStatement("DELETE FROM `PERIOD_PARTICIPANTS`;");
            for (Rating rating : results.getParticipants()) {
                connection.prepareStatement("INSERT IGNORE INTO `PERIOD_PARTICIPANTS` VALUES ('" + rating.getUuid() + "');").execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void setPassword(String uuid, String pwd) {
        String url = "jdbc:mysql://" + user + ":" + pws + "@" + host + ":" + port + "/" + dbName;
        try (Connection connection = DriverManager.getConnection(url)) {
            connection.prepareStatement("UPDATE `PLAYERS` SET password = '" + pwd + "' WHERE uuid = '" + uuid + "';").execute();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void savePlayers() {
        String url = "jdbc:mysql://" + user + ":" + pws + "@" + host + ":" + port + "/" + dbName;
        try (Connection connection = DriverManager.getConnection(url)) {
            connection.prepareStatement("DELETE FROM `PLAYERS`;");
            for (Rating player : players) {
                connection.prepareStatement("INSERT INTO `PLAYERS` VALUES ('" + player.getUuid() + "'," + player.getRating() + "," + player.getRatingDeviation() + "," + player.getVolatility() + "," + player.getNumberOfResults() + "," + player.getWorkingRating() + "," + player.getWorkingRatingDeviation() + "," + player.getWorkingVolatility() + ",'" + player.getName() + "','') ON DUPLICATE KEY UPDATE " +
                        "name=VALUES(name)," +
                        "rating=VALUES(rating)," +
                        "rating_deviation=VALUES(rating_deviation)," +
                        "result_count=VALUES(result_count)," +
                        "volatility=VALUES(volatility)," +
                        "working_rating=VALUES(working_rating)," +
                        "working_rating_deviation=VALUES(working_rating_deviation)," +
                        "working_volatility=VALUES(working_volatility)," +
                        "password=VALUES(password)" + ";").execute();
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    public void loadData() {
        String url = "jdbc:mysql://" + user + ":" + pws + "@" + host + ":" + port + "/" + dbName;

        try (Connection connection = DriverManager.getConnection(url);
             PreparedStatement ps = connection.prepareStatement("SELECT * FROM `PLAYERS`");
             ResultSet playerSet = ps.executeQuery();
             PreparedStatement periodResults = connection.prepareStatement("SELECT * FROM `PERIOD_RESULTS`");
             ResultSet periodSet = periodResults.executeQuery();
             PreparedStatement periodParticipants = connection.prepareStatement("SELECT * FROM `PERIOD_PARTICIPANTS`");
             ResultSet participantSet = periodParticipants.executeQuery()) {

            while (playerSet.next()) {
                String name = playerSet.getString("name");
                String uuid = playerSet.getString("uuid");
                double ratingDeviation = playerSet.getDouble("rating_deviation");
                double rating = playerSet.getDouble("rating");
                double volatility = playerSet.getDouble("volatility");
                int numberOfGames = playerSet.getInt("result_count");
                double workingRating = playerSet.getDouble("working_rating");
                double workingRatingDeviation = playerSet.getDouble("working_rating_deviation");
                double workingVolatility = playerSet.getDouble("working_volatility");

                addPlayer(new Rating(name, uuid, rating, ratingDeviation, volatility, numberOfGames, workingRating, workingRatingDeviation, workingVolatility));
            }
            while (participantSet.next()) {
                String uuid = participantSet.getString("uuid");
                if (containsPlayer(players, uuid)) {
                    participants.add(getPlayer(players, uuid));
                } else {
                    throw new IllegalArgumentException("Participant set contains unregistered player: " + uuid + ".");
                }
            }
            while (periodSet.next()) {
                boolean draw = periodSet.getBoolean("draw");
                String loserUUID = periodSet.getString("loser_uuid");
                String winnerUUID = periodSet.getString("winner_uuid");
                if (containsPlayer(participants, loserUUID) && containsPlayer(participants, winnerUUID)) {
                    if (!draw) {
                        results.add(new Result(getPlayer(participants, winnerUUID), getPlayer(participants, loserUUID)));
                    } else {
                        results.add(new Result(getPlayer(participants, winnerUUID), getPlayer(participants, loserUUID), true));
                    }
                } else {
                    throw new IllegalArgumentException("Result set contains unregistered players: {" + loserUUID + ", " + winnerUUID + "}.");
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        System.out.println("Successfully connected to database");

    }
}
