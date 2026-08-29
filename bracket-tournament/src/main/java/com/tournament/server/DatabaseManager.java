package com.tournament.server;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class DatabaseManager {
    private static final String DB_URL = "jdbc:sqlite:mm/src/main/resources/db/tournament.db";

    static {
        try (Connection conn = DriverManager.getConnection(DB_URL)) {
            if (conn != null) {
                Statement stmt = conn.createStatement();
                // TournamentRecord
                stmt.execute("CREATE TABLE IF NOT EXISTS TournamentRecord (" +
                        "id INTEGER PRIMARY KEY AUTOINCREMENT," +
                        "winner TEXT," +
                        "date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                
                // HeadToHead history
                stmt.execute("CREATE TABLE IF NOT EXISTS HeadToHead (" +
                        "player1 TEXT," +
                        "player2 TEXT," +
                        "winner TEXT," +
                        "match_date TIMESTAMP DEFAULT CURRENT_TIMESTAMP)");
                
                // WinRate / Player Stats
                stmt.execute("CREATE TABLE IF NOT EXISTS PlayerStats (" +
                        "username TEXT PRIMARY KEY," +
                        "wins INTEGER DEFAULT 0," +
                        "losses INTEGER DEFAULT 0)");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static void recordMatch(String p1, String p2, String winner) {
        String sql = "INSERT INTO HeadToHead(player1, player2, winner) VALUES(?,?,?)";
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, p1);
            pstmt.setString(2, p2);
            pstmt.setString(3, winner);
            pstmt.executeUpdate();
            updateStats(winner, true);
            updateStats(winner.equals(p1) ? p2 : p1, false);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void updateStats(String player, boolean win) {
        String sql = "INSERT INTO PlayerStats(username, wins, losses) VALUES(?,?,?) " +
                     "ON CONFLICT(username) DO UPDATE SET " +
                     (win ? "wins = wins + 1" : "losses = losses + 1");
        try (Connection conn = DriverManager.getConnection(DB_URL);
             PreparedStatement pstmt = conn.prepareStatement(sql)) {
            pstmt.setString(1, player);
            pstmt.setInt(2, win ? 1 : 0);
            pstmt.setInt(3, win ? 0 : 1);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
