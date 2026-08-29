# Java networking experiments

Client/server programs using sockets, plus Maven/JavaFX scaffolding used while building the tournament system.

## What's inside

| Project | What it does |
|---|---|
| [`networked-tournament`](networked-tournament/) | Server-side tournament scaffolding |
| [`bracket-tournament`](bracket-tournament/) | Bracket coordinator, game server and database manager |
| [`mm-app`](mm-app/) | JavaFX module experiment |
| [`mavenproject1`](mavenproject1/) | Maven + FXML starter |

## Running these

Most are plain Java — open the folder in IntelliJ IDEA or compile directly:

```bash
javac src/*.java && java -cp src Main
```

Folders containing a `pom.xml` are Maven projects:

```bash
mvn clean javafx:run
```

---

_Coursework from my Computer Science degree. Kept public as a record of progress._
