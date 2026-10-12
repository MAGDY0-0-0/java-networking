# Bracket tournament (prototype)

An early, unfinished attempt at a networked tournament server. The finished
four-player version is
[tictactoe-tournament](https://github.com/MAGDY0-0-0/tictactoe-tournament).

## What is here

- `server/GameServer`: a socket server on port 12345 that runs one `ClientHandler` thread per connection.
- `server/BracketCoordinator`: pairs players as they join and broadcasts the
  bracket. Each `MatchHandler` holds a 5×5 board and checks for five in a row.
- `server/DatabaseManager`: records match results in SQLite.
- `shared/NetworkProtocol`: the packet types sent over object streams (login,
  join, move, bracket update, chat, spectate).
- `client/MainApp`: a JavaFX client with a bracket view and a feed panel.

## What doesn't work yet

- `MatchHandler.run()` sends "game start" and then returns, so moves are never
  played through the server.
- The SQLite path is `mm/src/main/resources/db/tournament.db`, which is left over
  from an older folder layout and does not exist in this repo.
- `mavenproject55/pom.xml` is a stray copy of the project's POM.

## Running

```bash
mvn clean javafx:run          # the client
```

Run the server by starting `com.tournament.server.GameServer` from your IDE.
