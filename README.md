# Java networking experiments

Java socket programs related to
[tictactoe-tournament](https://github.com/MAGDY0-0-0/tictactoe-tournament): a
complete two-player networked Tic-Tac-Toe, an unfinished tournament-server
prototype, and two Maven/JavaFX starter projects.

## What's inside

| Project | What it is | State |
|---|---|---|
| [`mm-app`](mm-app/) | Two-player Tic-Tac-Toe over TCP in one JavaFX file (about 600 lines). One machine clicks "Start as Server" and shows its IP. The other connects to it on port 5555 using a plain-text protocol | Complete |
| [`bracket-tournament`](bracket-tournament/) | Tournament server prototype: a socket server with a thread per client, a bracket coordinator that pairs players as they join, a 5×5 board with five-in-a-row checks, a SQLite match log, and a JavaFX client | Prototype: moves aren't wired up to the match handler yet |
| [`networked-tournament`](networked-tournament/) | The NetBeans JavaFX starter ("Hello, JavaFX … running on Java …") | Template |
| [`mavenproject1`](mavenproject1/) | The NetBeans Maven + FXML starter with two switchable views | Template |

The finished, working version of the tournament idea is
[tictactoe-tournament](https://github.com/MAGDY0-0-0/tictactoe-tournament).

## Running these

All four are Maven projects that use the `javafx-maven-plugin`. From inside a project folder:

```bash
mvn clean javafx:run
```

For `mm-app`, start two instances: one as the server and one as the client.
They need to be on the same network, or both on one machine using `localhost`.

---

_Imported to GitHub in August 2026 as a single commit._
