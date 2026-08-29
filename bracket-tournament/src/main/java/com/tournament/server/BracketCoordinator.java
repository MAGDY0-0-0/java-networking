package com.tournament.server;

import com.tournament.shared.NetworkProtocol.*;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BracketCoordinator {
    private List<String> players = new ArrayList<>();
    private Map<String, ClientHandler> playerHandlers = new ConcurrentHashMap<>();
    private List<MatchInfo> bracket = new ArrayList<>();
    private int matchCounter = 0;

    public synchronized void addPlayer(String name, ClientHandler handler) {
        if (!players.contains(name)) {
            players.add(name);
            playerHandlers.put(name, handler);
            System.out.println("Player added to tournament: " + name);
            
            if (players.size() >= 2 && players.size() % 2 == 0) {
                startNewMatch(players.get(players.size()-2), players.get(players.size()-1));
            }
            broadcastBracket();
        }
    }

    private void startNewMatch(String p1, String p2) {
        MatchInfo match = new MatchInfo();
        match.matchId = ++matchCounter;
        match.player1 = p1;
        match.player2 = p2;
        match.isActive = true;
        match.round = 1;
        bracket.add(match);
        
        GameServer.getGamePool().execute(new MatchHandler(match, playerHandlers.get(p1), playerHandlers.get(p2)));
    }

    public void broadcastBracket() {
        Packet p = new Packet(PacketType.BRACKET_UPDATE, "SERVER", "");
        p.data = bracket;
        GameServer.broadcast(p);
    }
}

class MatchHandler implements Runnable {
    private MatchInfo info;
    private ClientHandler h1, h2;
    private char[][] board = new char[5][5]; // 5x5 Gomoku variant

    public MatchHandler(MatchInfo info, ClientHandler h1, ClientHandler h2) {
        this.info = info;
        this.h1 = h1;
        this.h2 = h2;
        for (int i = 0; i < 5; i++) Arrays.fill(board[i], ' ');
    }

    @Override
    public void run() {
        Packet start = new Packet(PacketType.GAME_START, "SERVER", "Match " + info.matchId);
        start.data = info;
        h1.send(start);
        h2.send(start);
        
        System.out.println("Started match " + info.matchId + " between " + info.player1 + " and " + info.player2);
        // In a real implementation, we would listen for moves here and update the board
    }

    public synchronized boolean makeMove(int r, int c, String player) {
        if (board[r][c] == ' ' && info.isActive) {
            board[r][c] = player.equals(info.player1) ? 'X' : 'O';
            checkWinner();
            return true;
        }
        return false;
    }

    private void checkWinner() {
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                if (board[i][j] == ' ') continue;
                if (checkLine(i, j, 1, 0) || checkLine(i, j, 0, 1) || 
                    checkLine(i, j, 1, 1) || checkLine(i, j, 1, -1)) {
                    info.winner = (board[i][j] == 'X') ? info.player1 : info.player2;
                    info.isActive = false;
                    DatabaseManager.recordMatch(info.player1, info.player2, info.winner);
                    return;
                }
            }
        }
    }

    private boolean checkLine(int r, int c, int dr, int dc) {
        char symbol = board[r][c];
        for (int i = 1; i < 5; i++) {
            int nr = r + i * dr;
            int nc = c + i * dc;
            if (nr < 0 || nr >= 5 || nc < 0 || nc >= 5 || board[nr][nc] != symbol) return false;
        }
        return true;
    }
}
