# BTL-LTM (WordBrain2 Multiplayer)

Online multiplayer word-puzzle game, built as a Network Programming (Lap Trinh Mang) course project at PTIT.

Players join a room by code and find words in a letter grid, racing in real time on a live leaderboard. See `GAMEPLAY.md` for the full game design.

## Features

- Rooms with a 6-character code and a host who starts the game.
- Real-time play over WebSocket, plus REST controllers for game, player, room, topic and leaderboard.
- Scoring, leaderboard and achievements.
- Seven booster types (double up, freeze, reveal, shield, skip half, streak save, time).
- Five word topics: animals, food, science, sports, technology.
- Clients: a static web app (`src/main/resources/static`), Python clients (tkinter and PyQt5) and a Java TCP client example.

## Tech stack

- Java 17, Spring Boot 2.7.14 (Web, Security, Cache, Validation, Actuator)
- WebSocket, Caffeine cache, Gson, Lombok
- Maven; Python for the sample clients

## Getting started

```bash
mvn spring-boot:run
```

The server listens on port 8080 by default (`application.properties`). To expose it publicly with ngrok, follow `setup-ngrok.md`.
