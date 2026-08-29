package com.mycompany.mm;

// ════════════════════════════════════════════════════════════════
//   NETWORKED TIC-TAC-TOE  —  Full Game in One File
//   Package: com.mycompany.mm
// ════════════════════════════════════════════════════════════════
//
//  HOW TO PLAY ON TWO DIFFERENT COMPUTERS:
//  ────────────────────────────────────────
//  1. Computer A (the "host"):
//       Run the program → click "Start as Server"
//       Note the IP address shown on screen
//
//  2. Computer B (the "guest"):
//       Run the program → click "Connect as Client"
//       Type Computer A's IP address → click Connect
//
//  Both computers must be on the same Wi-Fi network.
//  The server uses port 5555 (no external libraries needed).
//
//
//  CODE STRUCTURE — 4 SECTIONS:
//  ──────────────────────────────
//  [1] Board      → Pure game rules (wins, draws, valid moves)
//  [2] GameServer → Runs on the host; manages both players
//  [3] GameClient → The visual window each player sees
//  [4] App (Main) → Entry point; shows the startup menu
//
//
//  NETWORK PROTOCOL (plain text messages over TCP):
//  ──────────────────────────────────────────────────
//  Client ──► Server :  "MOVE 1 2"        player clicked row 1, col 2
//  Server ──► Client :  "WELCOME X"       you are player X
//  Server ──► Client :  "BOARD XO-X--O--" board state (- = empty)
//  Server ──► Client :  "YOUR_TURN"       it is your turn now
//  Server ──► Client :  "WAIT"            wait for the opponent
//  Server ──► Client :  "WIN YOU"         you won!
//  Server ──► Client :  "WIN OPPONENT"    opponent won
//  Server ──► Client :  "DRAW"            it's a draw
//
// ════════════════════════════════════════════════════════════════

import javafx.application.Application;
import javafx.application.Platform;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.TextAlignment;
import javafx.stage.Stage;

import java.io.*;
import java.net.*;

// ════════════════════════════════════════════════════════════════
//  SECTION 1 — BOARD
//  Knows only the rules: where pieces are, who won, is it full?
//  No networking, no graphics — just pure game logic.
// ════════════════════════════════════════════════════════════════
class Board {

    // The 3×3 grid. Each cell holds 'X', 'O', or '-' (empty).
    char[][] grid = new char[3][3];

    // Constructor: fill every cell with '-' (empty)
    Board() {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                grid[r][c] = '-';
    }

    // Try to place a symbol ('X' or 'O') at (row, col).
    // Returns true if the cell was empty and the move succeeded.
    boolean makeMove(int row, int col, char symbol) {
        if (grid[row][col] != '-') return false; // cell is occupied
        grid[row][col] = symbol;
        return true;
    }

    // Check if the given symbol has three in a row anywhere.
    boolean hasWon(char s) {
        // Check all 3 rows
        for (int r = 0; r < 3; r++)
            if (grid[r][0] == s && grid[r][1] == s && grid[r][2] == s) return true;

        // Check all 3 columns
        for (int c = 0; c < 3; c++)
            if (grid[0][c] == s && grid[1][c] == s && grid[2][c] == s) return true;

        // Check the two diagonals
        if (grid[0][0] == s && grid[1][1] == s && grid[2][2] == s) return true;
        if (grid[0][2] == s && grid[1][1] == s && grid[2][0] == s) return true;

        return false; // no winner yet
    }

    // Check if every cell is occupied (no moves left → draw)
    boolean isFull() {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                if (grid[r][c] == '-') return false;
        return true;
    }

    // Encode the board as a 9-character string for sending over the network.
    // Example: "XO-X--O--"  (row by row, '-' for empty)
    String encode() {
        StringBuilder sb = new StringBuilder();
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                sb.append(grid[r][c]);
        return sb.toString();
    }
}

// ════════════════════════════════════════════════════════════════
//  SECTION 2 — GAME SERVER
//
//  Runs on the host computer. Steps:
//   1. Open a TCP server socket on port 5555
//   2. Wait for Player 1 (X) to connect
//   3. Wait for Player 2 (O) to connect
//   4. Run the game loop: receive moves, check win/draw,
//      and send updates back to both players
// ════════════════════════════════════════════════════════════════
class GameServer implements Runnable {

    static final int PORT = 5555;

    // Called from the UI to show status messages in the server window
    interface Logger { void log(String msg); }
    private final Logger logger;

    GameServer(Logger logger) {
        this.logger = logger;
    }

    @Override
    public void run() {
        try {
            // Open the server port — this is what other computers connect to
            ServerSocket serverSocket = new ServerSocket(PORT);
            logger.log("✅ Server started on port " + PORT);
            logger.log("Waiting for Player 1 (X)...\n");

            // --- Accept Player 1 ---
            Socket p1Socket = serverSocket.accept();
            PrintWriter  out1 = new PrintWriter(p1Socket.getOutputStream(), true);
            BufferedReader in1 = new BufferedReader(new InputStreamReader(p1Socket.getInputStream()));
            logger.log("Player 1 (X) connected!\nWaiting for Player 2 (O)...\n");

            // Tell Player 1 who they are and to wait
            out1.println("WELCOME X");
            out1.println("WAIT");

            // --- Accept Player 2 ---
            Socket p2Socket = serverSocket.accept();
            PrintWriter  out2 = new PrintWriter(p2Socket.getOutputStream(), true);
            BufferedReader in2 = new BufferedReader(new InputStreamReader(p2Socket.getInputStream()));
            logger.log("Player 2 (O) connected!\n🎮 Game starting!\n");

            // Tell Player 2 who they are
            out2.println("WELCOME O");

            // --- Game Setup ---
            Board board = new Board();

            // Send the empty board to both players
            String boardMsg = "BOARD " + board.encode();
            out1.println(boardMsg);
            out2.println(boardMsg);

            // X goes first
            out1.println("YOUR_TURN");
            out2.println("WAIT");

            // --- Main Game Loop ---
            // Alternates between X and O until someone wins or it's a draw
            while (true) {

                // ── Player 1's turn (X) ──
                String msg1 = in1.readLine();           // e.g. "MOVE 0 2"
                if (msg1 == null) { logger.log("Player 1 disconnected."); break; }

                int[] pos1 = parseMove(msg1);           // extract row and col
                board.makeMove(pos1[0], pos1[1], 'X');  // place X on the board
                logger.log("X played: row=" + pos1[0] + " col=" + pos1[1]);

                // Send updated board to everyone
                String update1 = "BOARD " + board.encode();
                out1.println(update1);
                out2.println(update1);

                // Check if X won
                if (board.hasWon('X')) {
                    out1.println("WIN YOU");
                    out2.println("WIN OPPONENT");
                    logger.log("🏆 X wins!");
                    break;
                }
                // Check if it's a draw
                if (board.isFull()) {
                    out1.println("DRAW");
                    out2.println("DRAW");
                    logger.log("🤝 Draw!");
                    break;
                }

                // Switch: now it's O's turn
                out1.println("WAIT");
                out2.println("YOUR_TURN");

                // ── Player 2's turn (O) ──
                String msg2 = in2.readLine();
                if (msg2 == null) { logger.log("Player 2 disconnected."); break; }

                int[] pos2 = parseMove(msg2);
                board.makeMove(pos2[0], pos2[1], 'O');
                logger.log("O played: row=" + pos2[0] + " col=" + pos2[1]);

                String update2 = "BOARD " + board.encode();
                out1.println(update2);
                out2.println(update2);

                if (board.hasWon('O')) {
                    out1.println("WIN OPPONENT");
                    out2.println("WIN YOU");
                    logger.log("🏆 O wins!");
                    break;
                }
                if (board.isFull()) {
                    out1.println("DRAW");
                    out2.println("DRAW");
                    logger.log("🤝 Draw!");
                    break;
                }

                // Switch back to X
                out1.println("YOUR_TURN");
                out2.println("WAIT");
            }

            // Clean up — close all connections
            p1Socket.close();
            p2Socket.close();
            serverSocket.close();
            logger.log("Game over. Server closed.");

        } catch (IOException e) {
            logger.log("Server error: " + e.getMessage());
        }
    }

    // Parse "MOVE 1 2" → int[] {1, 2}
    private int[] parseMove(String msg) {
        String[] parts = msg.split(" "); // ["MOVE", "1", "2"]
        return new int[]{ Integer.parseInt(parts[1]), Integer.parseInt(parts[2]) };
    }
}

// ════════════════════════════════════════════════════════════════
//  SECTION 3 — GAME CLIENT (The Visual Window)
//
//  This is what each player sees on their screen.
//  It contains:
//   • A 3×3 grid of buttons for the board
//   • A status label (whose turn it is, win/draw result)
//   • Networking: connects to the server, sends moves, receives updates
//
//  KEY CONCEPT — Background Thread:
//   JavaFX UI must only be updated on the JavaFX thread.
//   But we wait for server messages on a SEPARATE thread (so the
//   UI doesn't freeze). When a message arrives, we use
//   Platform.runLater() to safely update the UI.
// ════════════════════════════════════════════════════════════════
class GameClient {

    // ── UI components ──
    private final Button[][] cells = new Button[3][3]; // the 9 board cells
    private Label statusLabel;   // "Your turn", "Waiting...", etc.
    private Label symbolLabel;   // "You are: X"

    // ── Network components ──
    private PrintWriter out;     // sends messages TO the server
    private BufferedReader in;   // receives messages FROM the server

    // ── Game state ──
    private char mySymbol;       // 'X' or 'O'
    private boolean myTurn;      // true = I can click, false = wait

    // Build and show the game window
    void start(Stage stage, String serverIP) {
        stage.setTitle("Tic-Tac-Toe — Networked");

        // ── Top bar (player info + status) ──
        symbolLabel = new Label("Connecting to server...");
        symbolLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        symbolLabel.setTextFill(Color.WHITE);

        statusLabel = new Label("Please wait...");
        statusLabel.setFont(Font.font("Arial", FontWeight.NORMAL, 15));
        statusLabel.setTextFill(Color.LIGHTBLUE);

        VBox topBar = new VBox(6, symbolLabel, statusLabel);
        topBar.setAlignment(Pos.CENTER);
        topBar.setPadding(new Insets(18, 10, 10, 10));
        topBar.setStyle("-fx-background-color: #1a1a2e;");

        // ── Game grid (3×3 buttons) ──
        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(8);
        grid.setPadding(new Insets(15));
        grid.setAlignment(Pos.CENTER);
        grid.setStyle("-fx-background-color: #0f0f1f;");

        for (int r = 0; r < 3; r++) {
            for (int c = 0; c < 3; c++) {
                Button btn = new Button(" ");
                btn.setPrefSize(110, 110);
                btn.setFont(Font.font("Arial", FontWeight.BOLD, 52));
                btn.setStyle(idleStyle());
                btn.setDisable(true); // disabled until it's our turn

                final int row = r, col = c; // must be final for lambda

                // When clicked: send the move to the server
                btn.setOnAction(e -> {
                    if (myTurn) {
                        out.println("MOVE " + row + " " + col); // tell server
                        myTurn = false;
                        statusLabel.setText("⏳ Waiting for opponent...");
                        statusLabel.setTextFill(Color.LIGHTBLUE);
                        disableAllCells();
                    }
                });

                cells[r][c] = btn;
                grid.add(btn, c, r); // note: GridPane.add(node, col, row)
            }
        }

        // ── Assemble the full layout ──
        BorderPane root = new BorderPane();
        root.setTop(topBar);
        root.setCenter(grid);
        root.setStyle("-fx-background-color: #0f0f1f;");

        Scene scene = new Scene(root, 380, 460);
        stage.setScene(scene);
        stage.show();

        // ── Connect to the server and start listening ──
        connectAndListen(serverIP);
    }

    // Open a TCP connection to the server and start a background listener thread
    private void connectAndListen(String serverIP) {
        new Thread(() -> {
            try {
                Socket socket = new Socket(serverIP, GameServer.PORT);
                out = new PrintWriter(socket.getOutputStream(), true);
                in  = new BufferedReader(new InputStreamReader(socket.getInputStream()));

                // Continuously read server messages in this background thread
                String line;
                while ((line = in.readLine()) != null) {
                    final String msg = line;
                    // Platform.runLater → safely update the JavaFX UI from a background thread
                    Platform.runLater(() -> handleMessage(msg));
                }

            } catch (IOException e) {
                Platform.runLater(() -> statusLabel.setText("❌ Cannot connect: " + e.getMessage()));
            }
        }).start(); // start the background thread
    }

    // Decide what to do with each message received from the server
    private void handleMessage(String msg) {

        if (msg.startsWith("WELCOME")) {
            // "WELCOME X" or "WELCOME O"
            mySymbol = msg.charAt(8);
            symbolLabel.setText("You are: " + mySymbol);

        } else if (msg.startsWith("BOARD")) {
            // "BOARD XO-X--O--"  → decode and refresh all 9 buttons
            String state = msg.substring(6); // the 9-character board string
            int i = 0;
            for (int r = 0; r < 3; r++) {
                for (int c = 0; c < 3; c++) {
                    char ch = state.charAt(i++);
                    if (ch == '-') {
                        cells[r][c].setText(" ");
                        cells[r][c].setStyle(idleStyle());
                    } else if (ch == 'X') {
                        cells[r][c].setText("X");
                        cells[r][c].setStyle(xStyle());
                    } else {
                        cells[r][c].setText("O");
                        cells[r][c].setStyle(oStyle());
                    }
                }
            }

        } else if (msg.equals("YOUR_TURN")) {
            myTurn = true;
            statusLabel.setText("⭐ Your turn! Click a cell.");
            statusLabel.setTextFill(Color.LIGHTGREEN);
            enableEmptyCells(); // allow clicking only empty cells

        } else if (msg.equals("WAIT")) {
            myTurn = false;
            statusLabel.setText("⏳ Waiting for opponent...");
            statusLabel.setTextFill(Color.LIGHTBLUE);
            disableAllCells();

        } else if (msg.equals("WIN YOU")) {
            statusLabel.setText("🎉 You WIN! Congratulations!");
            statusLabel.setTextFill(Color.LIGHTGREEN);
            disableAllCells();

        } else if (msg.equals("WIN OPPONENT")) {
            statusLabel.setText("😞 Opponent wins. Better luck!");
            statusLabel.setTextFill(Color.SALMON);
            disableAllCells();

        } else if (msg.equals("DRAW")) {
            statusLabel.setText("🤝 It's a DRAW!");
            statusLabel.setTextFill(Color.LIGHTYELLOW);
            disableAllCells();
        }
    }

    // Enable only the cells that are still empty (show as '-')
    private void enableEmptyCells() {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                cells[r][c].setDisable(!cells[r][c].getText().equals(" "));
    }

    // Disable all cells so the player cannot click anything
    private void disableAllCells() {
        for (int r = 0; r < 3; r++)
            for (int c = 0; c < 3; c++)
                cells[r][c].setDisable(true);
    }

    // ── CSS style strings for the buttons ──
    private String idleStyle() {
        return "-fx-background-color: #2a2a4a; -fx-background-radius: 10; " +
               "-fx-border-color: #4a4a7a; -fx-border-radius: 10; -fx-text-fill: white;";
    }
    private String xStyle() {
        return "-fx-background-color: #1a3a6a; -fx-background-radius: 10; " +
               "-fx-border-color: #5599ff; -fx-border-radius: 10; -fx-text-fill: #5599ff;";
    }
    private String oStyle() {
        return "-fx-background-color: #5a1a1a; -fx-background-radius: 10; " +
               "-fx-border-color: #ff6655; -fx-border-radius: 10; -fx-text-fill: #ff6655;";
    }
}

// ════════════════════════════════════════════════════════════════
//  SECTION 4 — APP (Main Entry Point)
//
//  This is where Java starts. It shows a startup menu:
//   • "Start as Server" → launches the server and shows the IP
//   • "Connect as Client" → asks for IP and opens the game window
// ════════════════════════════════════════════════════════════════
public class App extends Application {

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("Tic-Tac-Toe — Networked");

        // ── Title ──
        Label title = new Label("Tic-Tac-Toe");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 32));
        title.setTextFill(Color.WHITE);

        Label subtitle = new Label("Multiplayer over Network");
        subtitle.setFont(Font.font("Arial", 14));
        subtitle.setTextFill(Color.LIGHTBLUE);

        // ── Server Button ──
        Button serverBtn = new Button("🖥️  Start as Server (Host)");
        serverBtn.setPrefWidth(260);
        serverBtn.setPrefHeight(50);
        serverBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        serverBtn.setStyle("-fx-background-color: #2255aa; -fx-text-fill: white; " +
                           "-fx-background-radius: 10;");

        // ── Client Button ──
        Button clientBtn = new Button("🔗  Connect as Client (Guest)");
        clientBtn.setPrefWidth(260);
        clientBtn.setPrefHeight(50);
        clientBtn.setFont(Font.font("Arial", FontWeight.BOLD, 14));
        clientBtn.setStyle("-fx-background-color: #225533; -fx-text-fill: white; " +
                           "-fx-background-radius: 10;");

        // ── Instructions label ──
        Label hint = new Label(
            "Same Wi-Fi network required.\n" +
            "Server starts first, then Client connects."
        );
        hint.setFont(Font.font("Arial", 12));
        hint.setTextFill(Color.GRAY);
        hint.setTextAlignment(TextAlignment.CENTER);
        hint.setWrapText(true);

        // ── Layout ──
        VBox layout = new VBox(18, title, subtitle, serverBtn, clientBtn, hint);
        layout.setAlignment(Pos.CENTER);
        layout.setPadding(new Insets(40));
        layout.setStyle("-fx-background-color: #0f0f1f;");

        Scene scene = new Scene(layout, 360, 380);
        primaryStage.setScene(scene);
        primaryStage.show();

        // ══════════════════════════════════════
        //  SERVER button action
        // ══════════════════════════════════════
        serverBtn.setOnAction(e -> {

            // Find this machine's IP address to display to the user
            String myIP = "unknown";
            try {
                myIP = InetAddress.getLocalHost().getHostAddress();
            } catch (UnknownHostException ignored) {}

            // Build the server info window
            Stage serverStage = new Stage();
            serverStage.setTitle("Server — Waiting for players");

            TextArea log = new TextArea();
            log.setEditable(false);
            log.setFont(Font.font("Monospaced", 13));
            log.setStyle("-fx-control-inner-background: #0a0a1a; -fx-text-fill: #00ff88;");

            log.appendText("═══════════════════════════\n");
            log.appendText("  SERVER MODE ACTIVE\n");
            log.appendText("═══════════════════════════\n");
            log.appendText("Your IP address: " + myIP + "\n");
            log.appendText("Port: " + GameServer.PORT + "\n\n");
            log.appendText("Share your IP with your friend.\n");
            log.appendText("Waiting for 2 players to connect...\n\n");

            VBox serverLayout = new VBox(log);
            serverLayout.setPadding(new Insets(10));
            serverLayout.setStyle("-fx-background-color: #0a0a1a;");
            VBox.setVgrow(log, Priority.ALWAYS);

            serverStage.setScene(new Scene(serverLayout, 380, 300));
            serverStage.show();
            primaryStage.close();

            // Start the GameServer in a background thread
            // (so the UI window doesn't freeze while waiting)
            GameServer server = new GameServer(msg -> Platform.runLater(() -> log.appendText(msg + "\n")));
            Thread serverThread = new Thread(server);
            serverThread.setDaemon(true); // closes when the window closes
            serverThread.start();
        });

        // ══════════════════════════════════════
        //  CLIENT button action
        // ══════════════════════════════════════
        clientBtn.setOnAction(e -> {

            // Ask the user for the server's IP address
            TextInputDialog dialog = new TextInputDialog("192.168.1.1");
            dialog.setTitle("Connect to Server");
            dialog.setHeaderText("Enter the Server's IP address");
            dialog.setContentText("IP:");
            dialog.getDialogPane().setStyle("-fx-background-color: #1a1a2e;");

            dialog.showAndWait().ifPresent(ip -> {
                if (!ip.isBlank()) {
                    // Open a new stage for the game window
                    Stage gameStage = new Stage();
                    new GameClient().start(gameStage, ip.trim());
                    primaryStage.close();
                }
            });
        });
    }

    public static void main(String[] args) {
        launch(args); // JavaFX entry point
    }
}