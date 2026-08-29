package com.tournament.server;

import com.tournament.shared.NetworkProtocol.*;
import java.io.*;
import java.net.*;
import java.util.*;
import java.util.concurrent.*;

public class GameServer {
    private static final int PORT = 12345;
    private static List<ClientHandler> clients = new CopyOnWriteArrayList<>();
    private static BracketCoordinator bracketCoordinator = new BracketCoordinator();
    private static ExecutorService gamePool = Executors.newCachedThreadPool();

    public static void main(String[] args) {
        System.out.println("Tournament Server starting on port " + PORT);
        try (ServerSocket serverSocket = new ServerSocket(PORT)) {
            while (true) {
                Socket socket = serverSocket.accept();
                ClientHandler handler = new ClientHandler(socket);
                clients.add(handler);
                new Thread(handler).start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void broadcast(Packet packet) {
        for (ClientHandler client : clients) {
            client.send(packet);
        }
    }

    public static BracketCoordinator getBracketCoordinator() {
        return bracketCoordinator;
    }

    public static ExecutorService getGamePool() {
        return gamePool;
    }
}

class ClientHandler implements Runnable {
    private Socket socket;
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;

    public ClientHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        try {
            out = new ObjectOutputStream(socket.getOutputStream());
            in = new ObjectInputStream(socket.getInputStream());

            while (true) {
                Packet packet = (Packet) in.readObject();
                handlePacket(packet);
            }
        } catch (Exception e) {
            System.out.println("Client disconnected: " + username);
        } finally {
            // Cleanup
        }
    }

    private void handlePacket(Packet packet) {
        switch (packet.type) {
            case LOGIN:
                this.username = packet.sender;
                System.out.println("User logged in: " + username);
                break;
            case JOIN_TOURNAMENT:
                GameServer.getBracketCoordinator().addPlayer(username, this);
                break;
            case MOVE:
                // Forward to specific game handler
                break;
        }
    }

    public void send(Packet packet) {
        try {
            out.writeObject(packet);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
