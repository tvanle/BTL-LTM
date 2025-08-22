# Crossword Game Online - Web Structure

## Overview  
Dự án game crossword online multiplayer real-time bằng Java cho web - Client-Server architecture đơn giản.

## Web Architecture

```
┌─────────────────┐           HTTP/WebSocket         ┌─────────────────┐
│   WEB CLIENT    │ ◄─────────────────────────────── │   JAVA SERVER   │
│    (HTML/JS)    │                                  │  (Spring Boot)  │
│                 │                                  │                 │
│  ┌───────────┐  │                                  │  ┌───────────┐  │
│  │Game Board │  │           JSON Messages          │  │Game Logic │  │
│  │ (Canvas)  │  │                                  │  │ Engine    │  │
│  └───────────┘  │                                  │  └───────────┘  │
│  ┌───────────┐  │                                  │  ┌───────────┐  │
│  │WebSocket  │  │                                  │  │Dictionary │  │
│  │Client     │  │                                  │  │Service    │  │
│  └───────────┘  │                                  │  └───────────┘  │
└─────────────────┘                                  └─────────────────┘
```

## Project Structure

```
crossword-game/
├── pom.xml                                  # Maven configuration
├── src/main/
│   ├── java/com/crossword/
│   │   ├── CrosswordApplication.java        # Spring Boot main class
│   │   ├── controller/
│   │   │   ├── GameController.java          # REST endpoints for game
│   │   │   └── WebController.java           # Serve static pages
│   │   ├── websocket/
│   │   │   ├── GameWebSocketHandler.java    # WebSocket message handler
│   │   │   └── WebSocketConfig.java         # WebSocket configuration
│   │   ├── service/
│   │   │   ├── GameService.java             # Game business logic
│   │   │   ├── DictionaryService.java       # Word validation
│   │   │   └── SessionService.java          # Player session management
│   │   ├── model/
│   │   │   ├── GameState.java               # Game state model
│   │   │   ├── Player.java                  # Player information
│   │   │   ├── CrosswordGrid.java           # Grid representation
│   │   │   ├── Cell.java                    # Individual cell
│   │   │   ├── Word.java                    # Word model
│   │   │   └── GameMessage.java             # WebSocket message model
│   │   ├── repository/
│   │   │   └── GameRepository.java          # In-memory game storage
│   │   └── config/
│   │       └── CorsConfig.java              # CORS configuration for web
│   │
│   ├── resources/
│   │   ├── static/                          # Static web files
│   │   │   ├── index.html                   # Main game page
│   │   │   ├── css/
│   │   │   │   └── game.css                 # Game styling
│   │   │   └── js/
│   │   │       ├── game.js                  # Main game logic
│   │   │       ├── websocket.js             # WebSocket client
│   │   │       └── crossword-board.js       # Game board rendering
│   │   ├── dictionaries/
│   │   │   └── words.txt                    # Word dictionary
│   │   └── application.properties           # Spring Boot config
│   │
└── src/test/
    └── java/com/crossword/
        ├── service/
        │   ├── GameServiceTest.java         # Game logic tests
        │   └── DictionaryServiceTest.java   # Dictionary tests
        └── controller/
            └── GameControllerTest.java      # Controller tests
```

## Game Flow Architecture

```
┌─────────────┐    ┌─────────────┐    ┌─────────────┐
│   WAITING   │───▶│ IN_PROGRESS │───▶│  FINISHED   │
│             │    │             │    │             │
│ • Players   │    │ • Real-time │    │ • Final     │
│   join      │    │   validation│    │   scoring   │
│ • Grid      │    │ • Score     │    │ • Winner    │
│   setup     │    │   tracking  │    │   announce  │
└─────────────┘    └─────────────┘    └─────────────┘
```

## Message Flow Protocol

```
CLIENT                               SERVER
  │                                    │
  ├── JOIN_GAME ──────────────────────▶│
  │◄─────────────────── GAME_STATE ────┤
  │                                    │
  ├── PLACE_CHAR ─────────────────────▶│
  │                                    │──┐ Validate
  │                                    │◄─┘ with dictionary  
  │◄─────────── CHAR_VALIDATED ────────┤
  │◄─────────── SCORE_UPDATE ──────────┤
  │                                    │
  │◄─────────── GAME_ENDED ────────────┤ (when time up)
```

## Technology Stack

### Backend
- **Java 11+**: Programming language
- **Spring Boot**: Web framework và dependency injection
- **WebSocket**: Real-time communication
- **Maven**: Build tool

### Frontend  
- **HTML5**: Web structure
- **CSS3**: Styling và layout
- **JavaScript**: Game logic và interaction
- **Canvas API**: Game board rendering

### Dependencies
- **Spring Boot Web**: Web server
- **Spring WebSocket**: WebSocket support
- **Gson**: JSON processing

## Game Rules Implementation

### Scoring System (từ GAMEPLAY.md)
```
Mỗi ô đúng = +1 điểm
Hoàn tất từ = +bonus bằng độ dài từ
Hiển thị real-time: My Correct Cells vs Opponent Correct Cells
```

### Timing System
```
Thời gian mặc định: 180 giây
Đồng hồ đếm ngược cho cả 2 người
Kết thúc khi: hết thời gian HOẶC bảng đầy
```

### Validation Rules
```
Server kiểm tra:
1. Ký tự có nằm trong từ điển không
2. Có khớp với các giao điểm không  
3. Ai gửi hợp lệ trước sẽ thắng ô đó
```

## Build & Run

### Maven Commands
```bash
# Build project
mvn clean compile

# Run server
mvn spring-boot:run

# Run tests  
mvn test

# Package JAR
mvn clean package
```

### Access
- **Game URL**: http://localhost:8080
- **WebSocket**: ws://localhost:8080/game

## Maven Dependencies (pom.xml)

```xml
<dependencies>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-websocket</artifactId>
    </dependency>
    <dependency>
        <groupId>com.google.code.gson</groupId>
        <artifactId>gson</artifactId>
    </dependency>
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-test</artifactId>
        <scope>test</scope>
    </dependency>
</dependencies>
```

## Application Configuration

```properties
# Server port
server.port=8080

# WebSocket settings
spring.websocket.allowed-origins=*

# Game settings
game.max-players=2
game.duration-seconds=180
game.grid-size=15
```