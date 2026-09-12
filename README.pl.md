[![English](https://img.shields.io/badge/lang-English-blue.svg)](README.md)
[![Polish](https://img.shields.io/badge/lang-Polish-red.svg)](README.pl.md)

# Go Game (Java, Client-Server)

Projekt sieciowej implementacji chińskiej gry planszowej **Go** (wersja oparta na architekturze klient-serwer),
napisany w języku **Java** z wykorzystaniem środowiska **Gradle** oraz interfejsu graficznego **JavaFX**.

---

## Wymagania

* **Java JDK** w wersji **21** lub nowej.
* **Gradle** (projekt zawiera skrypt Wrapper, więc można używać poleceń `./gradlew` lub `gradle`).

---

## Start i kompilacja

### Budowanie Projektu

Aby skompilować projekt i wyczyścić poprzednie buildy, wykonaj:
```bash
gradle clean build
```

### Uruchomienie Serwera

Domyślnie serwer nasłuchuje na porcie `1664`.

```bash
gradle :server:run --console=plain --no-configuration-cache
```

### Uruchomienie Klienta

- Tryb konsolowy (CLI):
```bash
gradle :client:run --console=plain --no-configuration-cache
```

- Tryb graficzny:
```bash
gradle :client:run --console=plain --args="gui" --no-configuration-cache
```

### Uruchomienie Testowe
Jeśli chcesz szybko uruchomić serwer oraz dwie instancje klientów GUI w celach testowych, możesz skorzystać z poniższego skryptu bashowego:

```bash
#!/usr/bin/env bash
set -e
gradle :server:run --console=plain --no-configuration-cache &
SERVER_PID=$!
gradle :client:run --console=plain --args="gui" --no-configuration-cache &
CLIENT_1=$!
gradle :client:run --console=plain --args="gui" --no-configuration-cache &
CLIENT_2=$!
trap "kill $SERVER_PID $CLIENT_1 $CLIENT_2" INT TERM EXIT
wait
```

---

## Przebieg rozgrywki

Aby pomyślnie rozpocząć grę między klientami, przejdź przez następującą ścieżkę poleceń/akcji:
* SETNAME - ustaw unikalny nick w grze
* CREATE ROOM / JOIN - stwórz / dołącz do pokoju
* PICK COLOR (BLACK / WHITE) - wybierz kolor
* BEGIN - host rozpoczyna grę gdy wszyscy są gotowi
* ROZGRYWKA

### Format
W trybie konsolowym / wewnątrz logiki ruch definiowany jest jako:
```
move row col
```

---

## Architektura
- `Server` obsługuje połączenia TCP na porcie `1664`, zarządza wielowątkowymi sesjami klientów (`ClientHandler`), pokojami oraz stanem globalnym za pomocą `ClientManager` i `RoomManager`.
- `GameLogic` moduł reguł gry Go. Odpowiada za walidację posunięć, wykrywanie oddechów za pomocą algorytmu DFS, egzekwowanie kluczowych zasad takich jak zasada samobójstwa i Ko.
- `GoFXClient`Interfejs graficzny oparty na JavaFX, zapewniający dynamiczną aktualizację siatki planszy, obsługę zdarzeń sieciowych oraz akcje takie jak pasowanie (Pass), rezygnacja (Resign) czy wymiana kamieni (Swap).
