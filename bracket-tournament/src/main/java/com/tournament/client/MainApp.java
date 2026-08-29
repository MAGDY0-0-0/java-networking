package com.tournament.client;

import com.tournament.shared.NetworkProtocol.*;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.io.*;
import java.net.Socket;
import java.util.List;

public class MainApp extends Application {
    private ObjectOutputStream out;
    private ObjectInputStream in;
    private String username;
    private Canvas bracketCanvas = new Canvas(600, 400);
    private GridPane gameBoard = new GridPane();
    private TextArea feed = new TextArea();

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage primaryStage) {
        showLoginDialog(primaryStage);
    }

    private void showLoginDialog(Stage stage) {
        TextInputDialog dialog = new TextInputDialog("Player");
        dialog.setTitle("Login");
        dialog.setHeaderText("Enter your username:");
        dialog.showAndWait().ifPresent(name -> {
            this.username = name;
            connectToServer();
            showMainUI(stage);
        });
    }

    private void connectToServer() {
        new Thread(() -> {
            try {
                Socket socket = new Socket("localhost", 12345);
                out = new ObjectOutputStream(socket.getOutputStream());
                in = new ObjectInputStream(socket.getInputStream());

                send(new Packet(PacketType.LOGIN, username, ""));

                while (true) {
                    Packet packet = (Packet) in.readObject();
                    handlePacket(packet);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private void showMainUI(Stage stage) {
        BorderPane root = new BorderPane();
        
        // Left: Bracket
        VBox left = new VBox(new Label("Tournament Bracket"), bracketCanvas);
        left.setPadding(new Insets(10));
        root.setLeft(left);

        // Center: Game Board
        setupBoard();
        VBox center = new VBox(new Label("Game Board"), gameBoard);
        center.setPadding(new Insets(10));
        root.setCenter(center);

        // Right: Feed
        feed.setEditable(false);
        feed.setPrefWidth(200);
        VBox right = new VBox(new Label("Spectator Feed"), feed);
        right.setPadding(new Insets(10));
        root.setRight(right);

        // Bottom: Controls
        Button joinBtn = new Button("Join Tournament");
        joinBtn.setOnAction(e -> send(new Packet(PacketType.JOIN_TOURNAMENT, username, "")));
        root.setBottom(joinBtn);

        Scene scene = new Scene(root, 1000, 600);
        stage.setTitle("Tic-Tac-Toe Tournament - " + username);
        stage.setScene(scene);
        stage.show();
    }

    private void setupBoard() {
        gameBoard.setHgap(5);
        gameBoard.setVgap(5);
        for (int i = 0; i < 5; i++) {
            for (int j = 0; j < 5; j++) {
                Button btn = new Button("");
                btn.setPrefSize(60, 60);
                int r = i, c = j;
                btn.setOnAction(e -> makeMove(r, c));
                gameBoard.add(btn, j, i);
            }
        }
    }

    private void makeMove(int r, int c) {
        // Send move to server
    }

    private void handlePacket(Packet packet) {
        Platform.runLater(() -> {
            switch (packet.type) {
                case BRACKET_UPDATE:
                    drawBracket((List<MatchInfo>) packet.data);
                    break;
                case GAME_START:
                    feed.appendText("Game started: " + packet.payload + "\n");
                    break;
                case CHAT:
                    feed.appendText(packet.sender + ": " + packet.payload + "\n");
                    break;
            }
        });
    }

    private void drawBracket(List<MatchInfo> matches) {
        GraphicsContext gc = bracketCanvas.getGraphicsContext2D();
        gc.clearRect(0, 0, 600, 400);
        gc.setStroke(Color.BLACK);
        int y = 50;
        for (MatchInfo m : matches) {
            gc.strokeRect(50, y, 150, 40);
            gc.fillText(m.player1 + " vs " + m.player2, 60, y + 25);
            y += 60;
        }
    }

    private void send(Packet p) {
        try {
            out.writeObject(p);
            out.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
