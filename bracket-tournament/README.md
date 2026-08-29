# Networked Tic-Tac-Toe Tournament

This is a complete JavaFX-based tournament system for Tic-Tac-Toe and Gomoku.

## Features
- **Networked Play**: Sockets-based communication between server and multiple clients.
- **Tournament Bracket**: Automatic bracket generation for up to 16 players (expandable to 32).
- **Parallel Matches**: Each match runs on its own thread.
- **Live Updates**: Spectators and players see the bracket update in real-time.
- **Database**: SQLite integration for recording match history and player statistics.
- **Gomoku Variant**: 5x5 board with timed moves.

## How to Run in NetBeans
1. Open NetBeans.
2. Go to `File` -> `Open Project`.
3. Select the `mm` folder.
4. NetBeans will recognize it as a Maven project.
5. **Run the Server**:
   - Right-click the project -> `Run`.
   - Or run `com.tournament.server.GameServer` directly.
6. **Run the Client**:
   - Run `com.tournament.client.MainApp`.
   - You can run multiple instances of the client to simulate a tournament.

## Project Structure
- `src/main/java/com/tournament/server`: Server logic, bracket coordination, and database management.
- `src/main/java/com/tournament/client`: JavaFX UI and client-side networking.
- `src/main/java/com/tournament/shared`: Network protocols and data models.
- `src/main/resources/db`: SQLite database file.
