[![English](https://img.shields.io/badge/lang-English-blue.svg)](README.md)
[![Polish](https://img.shields.io/badge/lang-Polish-red.svg)](README.pl.md)

# Go Game (Java, Client-Server)

A networked implementation project of the traditional board game **Go** (based on a client-server architecture),
written in **Java** using the **Gradle** build tool and the **JavaFX** graphical user interface.

---

## Requirements

* **Java JDK** version **21** or newer.
* **Gradle** (the project includes a Wrapper script, so you can use `./gradlew` or `gradle`).

---

## Start and Compilation

### Building the Project

To compile the project and clean previous builds, run:
```bash
gradle clean build
```

### Starting the Server

By default, the server listens on port `1664`.

```bash
gradle :server:run --console=plain --no-configuration-cache
```

### Starting the Client

* Console mode (CLI):
```bash
gradle :client:run --console=plain --no-configuration-cache
```
* Graphical mode:
```bash
gradle :client:run --console=plain --args="gui" --no-configuration-cache
```

### Test Execution

If you want to quickly start the server and two GUI client instances for testing purposes, you can use the following bash script:

```bash
#!/usr/bin/env bash
set -e

gradle :server:run --console=plain --no-configuration-cache > server.log 2>&1 &
SERVER_PID=$!

sleep 1

gradle :client:run --console=plain --args="gui" --no-configuration-cache > client1.log 2>&1 &
CLIENT_1=$!

gradle :client:run --console=plain --args="gui" --no-configuration-cache > client2.log 2>&1 &
CLIENT_2=$!

trap "kill $SERVER_PID $CLIENT_1 $CLIENT_2" INT TERM EXIT
wait
```

---

## Gameplay Flow

To successfully start a game between clients, follow this command/action path:

* SETNAME - set a unique nickname in the game
* CREATE ROOM / JOIN - create or join a room
* PICK COLOR (BLACK / WHITE) - choose a color
* BEGIN - the host starts the game when everyone is ready
* GAMEPLAY

### Format

In console mode / within the logic, a move is defined as:
```
move row col
```

---

## Architecture

* `Server` handles TCP connections on port `1664`, manages multi-threaded client sessions (`ClientHandler`), rooms, and the global state using `ClientManager` and `RoomManager`.
* `GameLogic` module of Go game rules. Responsible for move validation, detecting liberties using the DFS algorithm, and enforcing key rules such as the suicide rule and Ko.
* `GoFXClient` Graphical user interface based on JavaFX, providing dynamic board grid updates, network event handling, and actions such as Pass, Resign, or Swap.
