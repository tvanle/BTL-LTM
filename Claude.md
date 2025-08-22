# Crossword Duel Pro - Advanced Project Structure

## Overview
Dự án game crossword online multiplayer real-time hoành tráng với Java - Kiến trúc Enterprise-level với Microservices, AI, và Cloud deployment.

## Architecture Diagram

```
┌─────────────────────────────────────────────────────────────┐
│                    CROSSWORD DUEL                           │
│                 Online Multiplayer Game                     │
└─────────────────────────────────────────────────────────────┘

┌─────────────────┐           WebSocket            ┌─────────────────┐
│     CLIENT      │ ◄──────────────────────────── │     SERVER      │
│                 │                                │                 │
│  ┌───────────┐  │                                │  ┌───────────┐  │
│  │    UI     │  │           JSON Messages        │  │   Game    │  │
│  │ (Swing)   │  │                                │  │ Manager   │  │
│  └───────────┘  │                                │  └───────────┘  │
│  ┌───────────┐  │                                │  ┌───────────┐  │
│  │  Network  │  │                                │  │ Dictionary│  │
│  │ WebSocket │  │                                │  │ Service   │  │
│  └───────────┘  │                                │  └───────────┘  │
│  ┌───────────┐  │                                │  ┌───────────┐  │
│  │   Game    │  │                                │  │  Network  │  │
│  │   State   │  │                                │  │ WebSocket │  │
│  └───────────┘  │                                │  └───────────┘  │
└─────────────────┘                                └─────────────────┘
         │                                                   │
         └─────────────── SHARED MODELS ────────────────────┘
```

## Directory Structure

```
src/
├── main/
│   ├── java/com/crossword/
│   │   ├── server/                          # SERVER SIDE
│   │   │   ├── CrosswordServer.java         # Main server entry point
│   │   │   ├── game/                        # Game Logic Layer
│   │   │   │   ├── GameManager.java         # Quản lý các game session
│   │   │   │   ├── GameSession.java         # Logic cho 1 trận đấu
│   │   │   │   ├── GameValidator.java       # Validate từ và nước đi
│   │   │   │   └── ScoreCalculator.java     # Tính điểm theo luật
│   │   │   ├── network/                     # Network Layer
│   │   │   │   ├── WebSocketGameServer.java # WebSocket server
│   │   │   │   └── ClientHandler.java       # Xử lý từng client
│   │   │   └── dictionary/                  # Dictionary Service
│   │   │       ├── DictionaryService.java   # Validate từ trong từ điển
│   │   │       └── WordGenerator.java       # Tạo crossword puzzle
│   │   │
│   │   ├── client/                          # CLIENT SIDE
│   │   │   ├── CrosswordClient.java         # Main client entry point
│   │   │   ├── ui/                          # User Interface Layer
│   │   │   │   ├── GameFrame.java           # Cửa sổ chính
│   │   │   │   ├── CrosswordPanel.java      # Hiển thị bảng ô chữ
│   │   │   │   ├── ScorePanel.java          # Panel điểm số
│   │   │   │   └── TimerPanel.java          # Panel đồng hồ đếm ngược
│   │   │   ├── network/                     # Network Layer
│   │   │   │   └── GameWebSocketClient.java # WebSocket client
│   │   │   └── game/                        # Game State Layer
│   │   │       └── ClientGameState.java     # Trạng thái game phía client
│   │   │
│   │   └── shared/                          # SHARED COMPONENTS
│   │       ├── model/                       # Data Models
│   │       │   ├── GameState.java           # Trạng thái tổng thể game
│   │       │   ├── CrosswordGrid.java       # Ma trận ô chữ
│   │       │   ├── Cell.java                # Một ô trong bảng
│   │       │   ├── CellType.java            # Enum: EMPTY/LOCKED/BLOCKED
│   │       │   ├── CellStatus.java          # Enum: EMPTY/PENDING/CORRECT/INCORRECT
│   │       │   ├── Word.java                # Định nghĩa một từ
│   │       │   ├── Direction.java           # Enum: HORIZONTAL/VERTICAL
│   │       │   └── PlayerScore.java         # Điểm số người chơi
│   │       ├── protocol/                    # Network Protocol
│   │       │   ├── GameMessage.java         # Wrapper cho message
│   │       │   └── MessageType.java         # Enum các loại message
│   │       └── util/                        # Utilities
│   │           ├── JsonUtil.java            # JSON serialization
│   │           └── Constants.java           # Hằng số game
│   │
│   └── resources/                           # RESOURCES
│       ├── dictionaries/                    # Từ điển
│       │   └── words.txt                    # File từ điển tiếng Việt
│       └── logback.xml                      # Cấu hình logging
│
└── test/                                    # UNIT TESTS
    └── java/com/crossword/
        ├── server/                          # Server tests
        ├── client/                          # Client tests  
        └── shared/                          # Shared model tests
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

### Core Technologies
- **Java 11+**: Programming language
- **Maven**: Build tool và dependency management
- **WebSocket**: Real-time communication protocol

### Libraries
- **Java-WebSocket**: WebSocket implementation
- **Gson**: JSON serialization/deserialization  
- **SLF4J + Logback**: Logging framework
- **Swing**: Client GUI framework

### Testing
- **JUnit 5**: Unit testing framework
- **Mockito**: Mocking framework

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

## Build Commands

```bash
# Compile
mvn clean compile

# Run Server  
mvn exec:java@server

# Run Client
mvn exec:java@client  

# Run Tests
mvn test
```