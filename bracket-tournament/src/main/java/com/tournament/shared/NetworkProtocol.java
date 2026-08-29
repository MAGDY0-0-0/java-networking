package com.tournament.shared;

import java.io.Serializable;
import java.util.List;

public class NetworkProtocol {
    public enum PacketType {
        LOGIN, JOIN_TOURNAMENT, MOVE, BRACKET_UPDATE, GAME_START, GAME_OVER, CHAT, SPECTATE
    }

    public static class Packet implements Serializable {
        public PacketType type;
        public String sender;
        public String payload; // JSON or simple string
        public Object data;

        public Packet(PacketType type, String sender, String payload) {
            this.type = type;
            this.sender = sender;
            this.payload = payload;
        }
    }

    public static class GameMove implements Serializable {
        public int row;
        public int col;
        public String player;
        public int matchId;
    }

    public static class BracketData implements Serializable {
        public List<MatchInfo> matches;
        public int totalPlayers;
    }

    public static class MatchInfo implements Serializable {
        public int matchId;
        public String player1;
        public String player2;
        public String winner;
        public int round;
        public boolean isActive;
    }
}
