# WordBrain2 - Multiplayer Word Puzzle Game

## 🎮 Game Overview
**WordBrain2** là game ghép chữ real-time multiplayer theo phong cách Quizizz. Người chơi kéo-thả hoặc chọn ký tự trong grid để tạo từ có nghĩa, thi đua tốc độ và độ chính xác với nhiều người chơi khác trong cùng phòng.

## 🏗️ System Architecture

```
┌─────────────────────────────────────────────────────────────────────┐
│                           CLIENTS LAYER                              │
├────────────┬────────────┬────────────┬────────────┬────────────────┤
│  Player 1  │  Player 2  │  Player 3  │  Player N  │  Admin/Host    │
│  (WebApp)  │  (WebApp)  │  (WebApp)  │  (WebApp)  │  (WebApp)      │
└─────┬──────┴─────┬──────┴─────┬──────┴─────┬──────┴──────┬─────────┘
      │            │            │            │             │
      └────────────┴────────────┴────────────┴─────────────┘
                                │
                        WebSocket/HTTP
                                │
┌───────────────────────────────▼─────────────────────────────────────┐
│                          SPRING BOOT SERVER                         │
├──────────────────────────────────────────────────────────────────────┤
│  ┌────────────────┐  ┌─────────────────┐  ┌──────────────────┐    │
│  │  WebSocket     │  │  REST API       │  │  Event Bus       │    │
│  │  Handler       │  │  Controllers    │  │  (Real-time)     │    │
│  └────────┬───────┘  └────────┬────────┘  └────────┬─────────┘    │
│           │                   │                     │               │
│  ┌────────▼───────────────────▼─────────────────────▼─────────┐    │
│  │                    GAME ENGINE CORE                         │    │
│  ├──────────────────────────────────────────────────────────────┤    │
│  │  • Room Management    • Timer Service    • Scoring Engine   │    │
│  │  • Grid Generator     • Booster System   • Level Manager    │    │
│  │  • Word Validator     • Player States    • Leaderboard      │    │
│  └──────────────────────────────────────────────────────────────┘    │
│                                                                      │
│  ┌──────────────────────────────────────────────────────────────┐    │
│  │                    DATA LAYER                                │    │
│  ├──────────────────────────────────────────────────────────────┤    │
│  │  • In-Memory Cache    • Dictionary DB    • Game History     │    │
│  │  • Player Sessions    • Room Storage     • Statistics       │    │
│  └──────────────────────────────────────────────────────────────┘    │
└──────────────────────────────────────────────────────────────────────┘
```

## 📁 Detailed Project Structure

```
wordbrain2-multiplayer/
│
├── 📄 pom.xml                                   # Maven configuration
├── 📄 README.md                                 # Project documentation
├── 📄 .gitignore                               
├── 📄 Dockerfile                                # Container setup
│
├── 📂 src/main/java/com/wordbrain2/
│   │
│   ├── 📄 WordBrain2Application.java           # Spring Boot entry point
│   │
│   ├── 📂 config/
│   │   ├── 📄 WebSocketConfig.java             # WebSocket configuration
│   │   ├── 📄 CorsConfig.java                  # CORS settings
│   │   ├── 📄 GameConfig.java                  # Game constants & settings
│   │   ├── 📄 SecurityConfig.java              # Security configuration
│   │   └── 📄 CacheConfig.java                 # Cache configuration
│   │
│   ├── 📂 controller/
│   │   ├── 📂 api/
│   │   │   ├── 📄 RoomController.java          # Room management endpoints
│   │   │   ├── 📄 PlayerController.java        # Player profile & stats
│   │   │   ├── 📄 GameController.java          # Game state endpoints
│   │   │   ├── 📄 LeaderboardController.java   # Rankings & scores
│   │   │   └── 📄 TopicController.java         # Topic/theme management
│   │   └── 📂 websocket/
│   │       ├── 📄 GameWebSocketHandler.java    # Main WebSocket handler
│   │       ├── 📄 MessageRouter.java           # Route messages to handlers
│   │       └── 📄 ConnectionManager.java       # Manage player connections
│   │
│   ├── 📂 service/
│   │   ├── 📂 core/
│   │   │   ├── 📄 GameEngine.java              # Core game logic
│   │   │   ├── 📄 RoomService.java             # Room lifecycle management
│   │   │   ├── 📄 PlayerService.java           # Player management
│   │   │   ├── 📄 SessionService.java          # Session handling
│   │   │   └── 📄 MatchmakingService.java      # Player matching logic
│   │   │
│   │   ├── 📂 game/
│   │   │   ├── 📄 GridGeneratorService.java    # Generate word grids
│   │   │   ├── 📄 WordValidationService.java   # Validate word submissions
│   │   │   ├── 📄 DictionaryService.java       # Dictionary management
│   │   │   ├── 📄 LevelProgressionService.java # Level management
│   │   │   ├── 📄 TimerService.java            # Game timer & sync
│   │   │   └── 📄 PathValidatorService.java    # Validate cell paths
│   │   │
│   │   ├── 📂 scoring/
│   │   │   ├── 📄 ScoreCalculator.java         # Score calculation logic
│   │   │   ├── 📄 StreakManager.java           # Streak tracking
│   │   │   ├── 📄 LeaderboardService.java      # Real-time rankings
│   │   │   └── 📄 StatisticsService.java       # Player statistics
│   │   │
│   │   ├── 📂 booster/
│   │   │   ├── 📄 BoosterService.java          # Booster management
│   │   │   ├── 📄 BoosterValidator.java        # Validate booster usage
│   │   │   └── 📄 BoosterEffectApplier.java    # Apply booster effects
│   │   │
│   │   └── 📂 event/
│   │       ├── 📄 EventBusService.java         # Event broadcasting
│   │       ├── 📄 GameEventPublisher.java      # Publish game events
│   │       └── 📄 NotificationService.java     # Player notifications
│   │
│   ├── 📂 model/
│   │   ├── 📂 entity/
│   │   │   ├── 📄 Player.java                  # Player entity
│   │   │   ├── 📄 Room.java                    # Game room entity
│   │   │   ├── 📄 GameSession.java             # Active game session
│   │   │   ├── 📄 Topic.java                   # Game topic/theme
│   │   │   └── 📄 Dictionary.java              # Word dictionary entity
│   │   │
│   │   ├── 📂 game/
│   │   │   ├── 📄 GameState.java               # Current game state
│   │   │   ├── 📄 Level.java                   # Level configuration
│   │   │   ├── 📄 Grid.java                    # Grid structure
│   │   │   ├── 📄 Cell.java                    # Grid cell
│   │   │   ├── 📄 Shape.java                   # Grid shape/mask
│   │   │   ├── 📄 Word.java                    # Word in grid
│   │   │   ├── 📄 Path.java                    # Cell path for word
│   │   │   └── 📄 Submission.java              # Player submission
│   │   │
│   │   ├── 📂 scoring/
│   │   │   ├── 📄 Score.java                   # Score model
│   │   │   ├── 📄 Leaderboard.java             # Leaderboard data
│   │   │   ├── 📄 Streak.java                  # Streak information
│   │   │   └── 📄 Achievement.java             # Player achievements
│   │   │
│   │   ├── 📂 booster/
│   │   │   ├── 📄 Booster.java                 # Base booster class
│   │   │   ├── 📄 DoubleUpBooster.java         # 2x points booster
│   │   │   ├── 📄 FreezeBooster.java           # Freeze opponents
│   │   │   ├── 📄 RevealBooster.java           # Reveal hint
│   │   │   ├── 📄 TimeBooster.java             # Extra time
│   │   │   ├── 📄 ShieldBooster.java           # Protection shield
│   │   │   ├── 📄 StreakSaveBooster.java       # Save streak
│   │   │   └── 📄 SkipHalfBooster.java         # Skip with 50% points
│   │   │
│   │   ├── 📂 dto/
│   │   │   ├── 📂 request/
│   │   │   │   ├── 📄 CreateRoomRequest.java   
│   │   │   │   ├── 📄 JoinRoomRequest.java     
│   │   │   │   ├── 📄 SubmitWordRequest.java   
│   │   │   │   ├── 📄 UseBoosterRequest.java   
│   │   │   │   └── 📄 StartGameRequest.java    
│   │   │   └── 📂 response/
│   │   │       ├── 📄 RoomResponse.java        
│   │   │       ├── 📄 GameStateResponse.java   
│   │   │       ├── 📄 LeaderboardResponse.java 
│   │   │       └── 📄 ValidationResponse.java  
│   │   │
│   │   └── 📂 enums/
│   │       ├── 📄 GamePhase.java               # LOBBY|PLAYING|LEVEL_END|FINISHED
│   │       ├── 📄 RoomStatus.java              # WAITING|READY|IN_GAME|CLOSED
│   │       ├── 📄 PlayerStatus.java            # IDLE|READY|PLAYING|DISCONNECTED
│   │       ├── 📄 CellType.java                # EMPTY|FILLED|BLOCKED
│   │       ├── 📄 MessageType.java             # WebSocket message types
│   │       ├── 📄 BoosterType.java             # All booster types
│   │       └── 📄 SubmissionResult.java        # CORRECT|INCORRECT|PARTIAL
│   │
│   ├── 📂 repository/
│   │   ├── 📄 RoomRepository.java              # Room data access
│   │   ├── 📄 PlayerRepository.java            # Player data access
│   │   ├── 📄 GameSessionRepository.java       # Game session storage
│   │   ├── 📄 DictionaryRepository.java        # Dictionary storage
│   │   └── 📄 LeaderboardRepository.java       # Leaderboard storage
│   │
│   ├── 📂 websocket/
│   │   ├── 📂 handler/
│   │   │   ├── 📄 RoomMessageHandler.java      # Room-related messages
│   │   │   ├── 📄 GameMessageHandler.java      # Game action messages
│   │   │   ├── 📄 BoosterMessageHandler.java   # Booster usage messages
│   │   │   └── 📄 ChatMessageHandler.java      # In-game chat messages
│   │   │
│   │   └── 📂 message/
│   │       ├── 📄 BaseMessage.java             # Base WebSocket message
│   │       ├── 📄 RoomMessage.java             # Room-related messages
│   │       ├── 📄 GameMessage.java             # Game action messages
│   │       ├── 📄 BoosterMessage.java          # Booster messages
│   │       └── 📄 BroadcastMessage.java        # Broadcast to all players
│   │
│   ├── 📂 exception/
│   │   ├── 📄 GameException.java               # Base game exception
│   │   ├── 📄 RoomNotFoundException.java       
│   │   ├── 📄 InvalidSubmissionException.java  
│   │   ├── 📄 BoosterNotAvailableException.java
│   │   └── 📄 PlayerNotFoundException.java     
│   │
│   └── 📂 util/
│       ├── 📄 GridGenerator.java               # Grid generation utilities
│       ├── 📄 WordShuffler.java                # Word shuffling logic
│       ├── 📄 PathValidator.java               # Path validation utilities
│       ├── 📄 TimeSync.java                    # Time synchronization
│       └── 📄 RandomUtils.java                 # Random utilities
│
├── 📂 src/main/resources/
│   │
│   ├── 📂 static/
│   │   ├── 📄 index.html                       # Landing page
│   │   ├── 📄 room.html                        # Room/lobby page
│   │   ├── 📄 game.html                        # Main game page
│   │   │
│   │   ├── 📂 css/
│   │   │   ├── 📄 main.css                     # Main styles
│   │   │   ├── 📄 game.css                     # Game-specific styles
│   │   │   ├── 📄 animations.css               # Animations & effects
│   │   │   └── 📄 responsive.css               # Mobile responsive
│   │   │
│   │   ├── 📂 js/
│   │   │   ├── 📂 core/
│   │   │   │   ├── 📄 app.js                   # Main application
│   │   │   │   ├── 📄 websocket-client.js      # WebSocket connection
│   │   │   │   ├── 📄 event-manager.js         # Event handling
│   │   │   │   └── 📄 state-manager.js         # Client state management
│   │   │   │
│   │   │   ├── 📂 game/
│   │   │   │   ├── 📄 game-controller.js       # Game logic controller
│   │   │   │   ├── 📄 grid-renderer.js         # Grid drawing & interaction
│   │   │   │   ├── 📄 word-selector.js         # Word selection logic
│   │   │   │   ├── 📄 drag-handler.js          # Drag & drop handling
│   │   │   │   ├── 📄 timer-manager.js         # Timer display & sync
│   │   │   │   └── 📄 animation-controller.js  # Game animations
│   │   │   │
│   │   │   ├── 📂 ui/
│   │   │   │   ├── 📄 room-ui.js               # Room/lobby UI
│   │   │   │   ├── 📄 leaderboard-ui.js        # Leaderboard display
│   │   │   │   ├── 📄 booster-ui.js            # Booster buttons & effects
│   │   │   │   ├── 📄 score-display.js         # Score & streak display
│   │   │   │   ├── 📄 notification-ui.js       # Pop-up notifications
│   │   │   │   └── 📄 modal-manager.js         # Modal dialogs
│   │   │   │
│   │   │   └── 📂 utils/
│   │   │       ├── 📄 constants.js             # Client constants
│   │   │       ├── 📄 helpers.js               # Helper functions
│   │   │       └── 📄 sound-manager.js         # Sound effects
│   │   │
│   │   ├── 📂 img/
│   │   │   ├── 📂 boosters/                    # Booster icons
│   │   │   ├── 📂 avatars/                     # Player avatars
│   │   │   └── 📂 ui/                          # UI elements
│   │   │
│   │   └── 📂 sounds/
│   │       ├── 📄 correct.mp3                  # Correct answer sound
│   │       ├── 📄 incorrect.mp3                # Wrong answer sound
│   │       ├── 📄 levelup.mp3                  # Level complete sound
│   │       └── 📄 countdown.mp3                # Timer warning sound
│   │
│   ├── 📂 dictionaries/
│   │   ├── 📄 vietnamese.txt                   # Vietnamese words
│   │   ├── 📄 english.txt                      # English words
│   │   └── 📂 topics/
│   │       ├── 📄 animals.txt                  # Animal words
│   │       ├── 📄 technology.txt               # Tech words
│   │       ├── 📄 food.txt                     # Food words
│   │       └── 📄 science.txt                  # Science words
│   │
│   ├── 📂 templates/                           # Email templates (if needed)
│   │
│   ├── 📄 application.properties               # Spring configuration
│   ├── 📄 application-dev.properties           # Development config
│   └── 📄 application-prod.properties          # Production config
│
└── 📂 src/test/java/com/wordbrain2/
    ├── 📂 unit/
    │   ├── 📂 service/
    │   │   ├── 📄 GameEngineTest.java
    │   │   ├── 📄 WordValidationServiceTest.java
    │   │   ├── 📄 ScoreCalculatorTest.java
    │   │   └── 📄 BoosterServiceTest.java
    │   └── 📂 util/
    │       ├── 📄 GridGeneratorTest.java
    │       └── 📄 PathValidatorTest.java
    │
    └── 📂 integration/
        ├── 📄 GameFlowIntegrationTest.java
        ├── 📄 WebSocketIntegrationTest.java
        └── 📄 MultiplayerSyncTest.java
```

## 🎯 Game Flow Implementation

### 1. Room Creation & Join Flow
```
Admin/Host                  Server                    Players
    │                         │                          │
    ├── CREATE_ROOM ─────────►│                          │
    │   (topic, levels,        │                          │
    │    time, boosters)       │                          │
    │◄── ROOM_CREATED ─────────┤                          │
    │    (roomCode: ABC123)    │                          │
    │                         │                          │
    │                         │◄──── JOIN_ROOM ──────────┤
    │                         │      (roomCode)          │
    │                         ├──── PLAYER_JOINED ──────►│
    │◄── PLAYER_JOINED ────────┤                          │
    │                         │                          │
    │── START_GAME ──────────►│                          │
    │                         ├──── GAME_STARTING ──────►│
    │◄── GAME_STARTING ────────┤      (countdown)        │
    │                         │                          │
    │◄── LEVEL_START ─────────┼──── LEVEL_START ────────►│
        (grid, timer, shape)                (synchronized)
```

### 2. Gameplay Message Flow
```
Player A                    Server                    Player B
    │                         │                          │
    ├── SUBMIT_WORD ─────────►│                          │
    │   (path, word)          │──┐                       │
    │                         │  │ Validate:             │
    │                         │  │ • Dictionary check    │
    │                         │  │ • Path validity       │
    │                         │  │ • Shape coverage      │
    │                         │◄─┘                       │
    │◄── WORD_ACCEPTED ────────┤                          │
    │    (+points, streak)    │                          │
    │                         ├──── OPPONENT_SCORED ────►│
    │                         │      (playerA, points)   │
    │                         │                          │
    │                         │◄──── USE_BOOSTER ────────┤
    │                         │      (FREEZE)            │
    │◄── EFFECT_APPLIED ───────┤                          │
    │    (frozen: 3s)         │                          │
    │                         ├──── EFFECT_APPLIED ─────►│
    │                         │                          │
    │◄── LEADERBOARD_UPDATE ───┼── LEADERBOARD_UPDATE ──►│
         (real-time rankings)
```

### 3. Level Progression Flow
```
All Players                 Server
    │                         │
    │◄── LEVEL_ENDING ─────────┤ (10s countdown)
    │                         │
    │◄── LEVEL_RESULTS ────────┤
    │    • Correct/Wrong       │
    │    • Points earned        │
    │    • Current rankings     │
    │                         │
    │◄── NEXT_LEVEL_STARTING ──┤ (5s preparation)
    │                         │
    │◄── LEVEL_START ──────────┤
         (new grid, timer)
```

## 💾 Data Models

### Room Model
```java
public class Room {
    private String roomCode;        // 6-character code
    private String hostId;          // Admin player ID
    private String topic;           // Selected topic
    private int maxPlayers;         // Max 20 players
    private int levelCount;         // Number of levels (default: 10)
    private int levelDuration;      // Seconds per level (default: 30)
    private List<String> playerIds; // Connected players
    private RoomStatus status;      // WAITING|READY|IN_GAME|CLOSED
    private GameSession gameSession;// Active game session
    private Map<String, Boolean> playerReady; // Ready status
    private List<BoosterType> allowedBoosters; // Enabled boosters
}
```

### Grid & Shape Model
```java
public class Grid {
    private int rows;               // Grid height (e.g., 5)
    private int cols;               // Grid width (e.g., 5)
    private Cell[][] cells;         // 2D grid array
    private Shape shape;            // Active area shape
    private List<Word> solutions;   // Possible valid words
    private int totalCells;         // Count of usable cells
}

public class Shape {
    private boolean[][] mask;       // true = usable, false = blocked
    private int cellCount;          // Total usable cells
    private ShapeType type;         // SQUARE|CIRCLE|DIAMOND|CUSTOM
}
```

### Submission & Validation Model
```java
public class Submission {
    private String playerId;
    private List<Cell> path;        // Selected cells in order
    private String word;            // Formed word
    private long timestamp;         // Submit time
    private SubmissionResult result;// CORRECT|INCORRECT|PARTIAL
    private int pointsEarned;      // Points for this submission
    private boolean usedBooster;   // If booster was active
}
```

### Score & Leaderboard Model
```java
public class PlayerScore {
    private String playerId;
    private int totalPoints;        // Current total score
    private int correctWords;       // Words found
    private int currentStreak;      // Current correct streak
    private int maxStreak;          // Best streak
    private double avgResponseTime; // Average solve time
    private int boostersUsed;      // Boosters consumed
    private int rank;               // Current position
}
```

## 🔧 Configuration Files

### application.properties
```properties
# Server Configuration
server.port=8080
server.servlet.context-path=/

# WebSocket Configuration
spring.websocket.max-text-message-size=65536
spring.websocket.max-binary-message-size=65536
spring.websocket.send-time-limit=20000
spring.websocket.send-buffer-size-limit=512000

# Game Configuration
game.room.code-length=6
game.room.max-players=20
game.room.idle-timeout=300000
game.level.default-duration=30
game.level.default-count=10
game.level.transition-time=5
game.grid.min-size=3
game.grid.max-size=8
game.word.min-length=3
game.word.max-solutions=5

# Scoring Configuration
score.base-points=1000
score.speed-multiplier=0.5-1.0
score.streak-bonus=0.1
score.streak-max-multiplier=1.5
score.penalty-wrong=-150
score.penalty-max=2

# Booster Configuration
booster.doubleup.cooldown=3
booster.freeze.duration=3000
booster.freeze.shield-blocks=true
booster.reveal.cost=-100
booster.timeplus.seconds=5
booster.timeplus.max-uses=2
booster.skiphalf.points-ratio=0.5

# Dictionary Configuration
dictionary.path=classpath:dictionaries/
dictionary.default-language=vietnamese
dictionary.cache-size=10000
dictionary.preload=true

# Session Configuration
session.timeout=1800000
session.heartbeat-interval=5000
session.reconnect-window=30000

# Cache Configuration
spring.cache.type=caffeine
spring.cache.caffeine.spec=maximumSize=1000,expireAfterWrite=30m

# Monitoring
management.endpoints.web.exposure.include=health,metrics,info
management.endpoint.health.show-details=always
```

### pom.xml Dependencies
```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0">
    <modelVersion>4.0.0</modelVersion>
    
    <groupId>com.wordbrain2</groupId>
    <artifactId>wordbrain2-multiplayer</artifactId>
    <version>1.0.0</version>
    <packaging>jar</packaging>
    
    <parent>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-parent</artifactId>
        <version>2.7.14</version>
    </parent>
    
    <properties>
        <java.version>11</java.version>
        <maven.compiler.source>11</maven.compiler.source>
        <maven.compiler.target>11</maven.compiler.target>
    </properties>
    
    <dependencies>
        <!-- Spring Boot Core -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-web</artifactId>
        </dependency>
        
        <!-- WebSocket Support -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-websocket</artifactId>
        </dependency>
        
        <!-- Security -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-security</artifactId>
        </dependency>
        
        <!-- Cache -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-cache</artifactId>
        </dependency>
        <dependency>
            <groupId>com.github.ben-manes.caffeine</groupId>
            <artifactId>caffeine</artifactId>
        </dependency>
        
        <!-- JSON Processing -->
        <dependency>
            <groupId>com.google.code.gson</groupId>
            <artifactId>gson</artifactId>
            <version>2.10.1</version>
        </dependency>
        
        <!-- Validation -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-validation</artifactId>
        </dependency>
        
        <!-- Lombok -->
        <dependency>
            <groupId>org.projectlombok</groupId>
            <artifactId>lombok</artifactId>
            <optional>true</optional>
        </dependency>
        
        <!-- Apache Commons -->
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-lang3</artifactId>
            <version>3.12.0</version>
        </dependency>
        <dependency>
            <groupId>org.apache.commons</groupId>
            <artifactId>commons-collections4</artifactId>
            <version>4.4</version>
        </dependency>
        
        <!-- Monitoring -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-actuator</artifactId>
        </dependency>
        <dependency>
            <groupId>io.micrometer</groupId>
            <artifactId>micrometer-registry-prometheus</artifactId>
        </dependency>
        
        <!-- Testing -->
        <dependency>
            <groupId>org.springframework.boot</groupId>
            <artifactId>spring-boot-starter-test</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.mockito</groupId>
            <artifactId>mockito-core</artifactId>
            <scope>test</scope>
        </dependency>
        <dependency>
            <groupId>org.junit.jupiter</groupId>
            <artifactId>junit-jupiter</artifactId>
            <scope>test</scope>
        </dependency>
    </dependencies>
    
    <build>
        <plugins>
            <plugin>
                <groupId>org.springframework.boot</groupId>
                <artifactId>spring-boot-maven-plugin</artifactId>
            </plugin>
        </plugins>
    </build>
</project>
```

## 🚀 WebSocket Protocol Details

### Message Types
```java
public enum MessageType {
    // Room Management
    CREATE_ROOM,
    JOIN_ROOM,
    LEAVE_ROOM,
    PLAYER_READY,
    START_GAME,
    
    // Game Flow
    GAME_STARTING,
    LEVEL_START,
    LEVEL_END,
    GAME_END,
    
    // Player Actions
    SUBMIT_WORD,
    USE_BOOSTER,
    REQUEST_HINT,
    
    // Server Responses
    WORD_ACCEPTED,
    WORD_REJECTED,
    BOOSTER_APPLIED,
    EFFECT_RECEIVED,
    
    // Real-time Updates
    LEADERBOARD_UPDATE,
    OPPONENT_SCORED,
    TIMER_UPDATE,
    PLAYER_DISCONNECTED,
    PLAYER_RECONNECTED,
    
    // Error Messages
    ERROR,
    INVALID_ACTION,
    TIMEOUT
}
```

### Sample WebSocket Messages

#### Client → Server: Submit Word
```json
{
    "type": "SUBMIT_WORD",
    "roomCode": "ABC123",
    "playerId": "player_001",
    "data": {
        "path": [
            {"row": 0, "col": 0, "char": "H"},
            {"row": 0, "col": 1, "char": "E"},
            {"row": 1, "col": 1, "char": "L"},
            {"row": 2, "col": 1, "char": "L"},
            {"row": 2, "col": 2, "char": "O"}
        ],
        "word": "HELLO",
        "timestamp": 1634567890123
    }
}
```

#### Server → Client: Level Start
```json
{
    "type": "LEVEL_START",
    "data": {
        "level": 3,
        "grid": {
            "rows": 5,
            "cols": 5,
            "cells": [
                ["H", "E", "L", "L", "O"],
                ["A", "P", "P", "L", "E"],
                ["P", "I", "Z", "Z", "A"],
                ["P", "E", "A", "R", "S"],
                ["Y", "E", "S", "N", "O"]
            ],
            "shape": {
                "mask": [
                    [1, 1, 1, 1, 1],
                    [1, 1, 1, 1, 1],
                    [0, 1, 1, 1, 0],
                    [0, 0, 1, 0, 0],
                    [0, 0, 0, 0, 0]
                ],
                "cellCount": 15
            }
        },
        "duration": 30,
        "serverTime": 1634567890000,
        "targetWords": 2
    }
}
```

#### Server → All Clients: Leaderboard Update
```json
{
    "type": "LEADERBOARD_UPDATE",
    "data": {
        "leaderboard": [
            {
                "rank": 1,
                "playerId": "player_002",
                "name": "Alice",
                "score": 4500,
                "streak": 5,
                "lastAction": "WORD_CORRECT"
            },
            {
                "rank": 2,
                "playerId": "player_001",
                "name": "Bob",
                "score": 3800,
                "streak": 2,
                "lastAction": "BOOSTER_USED"
            }
        ],
        "levelProgress": {
            "current": 3,
            "total": 10,
            "timeRemaining": 18
        }
    }
}
```

## 🎮 Client-Side Implementation

### Grid Rendering & Interaction
```javascript
class GridRenderer {
    constructor(canvas, gridData) {
        this.canvas = canvas;
        this.ctx = canvas.getContext('2d');
        this.grid = gridData;
        this.cellSize = 60;
        this.selectedPath = [];
        this.isDragging = false;
    }
    
    render() {
        // Clear canvas
        this.ctx.clearRect(0, 0, this.canvas.width, this.canvas.height);
        
        // Draw grid cells
        for (let row = 0; row < this.grid.rows; row++) {
            for (let col = 0; col < this.grid.cols; col++) {
                this.drawCell(row, col);
            }
        }
        
        // Draw selection path
        if (this.selectedPath.length > 0) {
            this.drawPath();
        }
    }
    
    drawCell(row, col) {
        const x = col * this.cellSize;
        const y = row * this.cellSize;
        const cell = this.grid.cells[row][col];
        const isActive = this.grid.shape.mask[row][col];
        
        // Cell background
        if (!isActive) {
            this.ctx.fillStyle = '#2c3e50'; // Blocked cell
        } else if (this.isInPath(row, col)) {
            this.ctx.fillStyle = '#3498db'; // Selected
        } else {
            this.ctx.fillStyle = '#ecf0f1'; // Normal
        }
        
        this.ctx.fillRect(x, y, this.cellSize - 2, this.cellSize - 2);
        
        // Draw character
        if (cell && isActive) {
            this.ctx.fillStyle = '#2c3e50';
            this.ctx.font = 'bold 24px Arial';
            this.ctx.textAlign = 'center';
            this.ctx.textBaseline = 'middle';
            this.ctx.fillText(
                cell, 
                x + this.cellSize / 2, 
                y + this.cellSize / 2
            );
        }
    }
    
    handleDragStart(x, y) {
        const cell = this.getCellFromCoords(x, y);
        if (cell && this.isValidCell(cell)) {
            this.isDragging = true;
            this.selectedPath = [cell];
            this.render();
        }
    }
    
    handleDragMove(x, y) {
        if (!this.isDragging) return;
        
        const cell = this.getCellFromCoords(x, y);
        if (cell && this.isValidCell(cell) && !this.isInPath(cell.row, cell.col)) {
            // Check if adjacent to last cell
            const lastCell = this.selectedPath[this.selectedPath.length - 1];
            if (this.isAdjacent(lastCell, cell)) {
                this.selectedPath.push(cell);
                this.render();
            }
        }
    }
    
    handleDragEnd() {
        if (this.isDragging && this.selectedPath.length >= 3) {
            this.submitWord();
        }
        this.isDragging = false;
        this.selectedPath = [];
        this.render();
    }
    
    isAdjacent(cell1, cell2) {
        const rowDiff = Math.abs(cell1.row - cell2.row);
        const colDiff = Math.abs(cell1.col - cell2.col);
        return rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff) > 0;
    }
    
    submitWord() {
        const word = this.selectedPath
            .map(cell => this.grid.cells[cell.row][cell.col])
            .join('');
        
        // Send to server
        gameController.submitWord(this.selectedPath, word);
    }
}
```

### Booster System Implementation
```javascript
class BoosterManager {
    constructor() {
        this.boosters = {
            DOUBLE_UP: { available: 1, cooldown: 0, maxUses: 1 },
            FREEZE: { available: 1, cooldown: 0, maxUses: 1 },
            REVEAL: { available: 2, cooldown: 0, maxUses: 2 },
            TIME_PLUS: { available: 2, cooldown: 0, maxUses: 2 },
            SHIELD: { available: 1, cooldown: 0, maxUses: 1 },
            STREAK_SAVE: { available: 1, cooldown: 0, maxUses: 1 },
            SKIP_HALF: { available: 1, cooldown: 0, maxUses: 1 }
        };
        
        this.activeEffects = [];
        this.initUI();
    }
    
    useBooster(type) {
        const booster = this.boosters[type];
        
        if (!booster || booster.available <= 0 || booster.cooldown > 0) {
            this.showError('Booster not available');
            return;
        }
        
        // Send to server
        websocket.send({
            type: 'USE_BOOSTER',
            data: { boosterType: type }
        });
        
        // Optimistic UI update
        booster.available--;
        this.updateBoosterUI(type);
        
        // Apply local effects immediately
        this.applyLocalEffect(type);
    }
    
    applyLocalEffect(type) {
        switch(type) {
            case 'DOUBLE_UP':
                this.showEffect('2X POINTS ACTIVE!', 'gold');
                break;
            case 'TIME_PLUS':
                timerManager.addTime(5);
                this.showEffect('+5 SECONDS!', 'green');
                break;
            case 'SHIELD':
                this.activeEffects.push({ type: 'SHIELD', duration: -1 });
                this.showEffect('SHIELD ACTIVE!', 'blue');
                break;
        }
    }
    
    receiveOpponentEffect(effect) {
        if (effect.type === 'FREEZE') {
            // Check for shield
            const shield = this.activeEffects.find(e => e.type === 'SHIELD');
            if (shield) {
                this.activeEffects = this.activeEffects.filter(e => e !== shield);
                this.showEffect('FREEZE BLOCKED!', 'blue');
                return;
            }
            
            // Apply freeze
            gridRenderer.disable();
            this.showEffect('FROZEN!', 'red');
            
            setTimeout(() => {
                gridRenderer.enable();
                this.showEffect('UNFROZEN!', 'green');
            }, 3000);
        }
    }
}
```

## 🧪 Testing Strategy

### Unit Tests
```java
@Test
public void testWordValidation() {
    // Test valid word paths
    List<Cell> path = Arrays.asList(
        new Cell(0, 0, 'H'),
        new Cell(0, 1, 'E'),
        new Cell(1, 1, 'L'),
        new Cell(2, 1, 'L'),
        new Cell(2, 2, 'O')
    );
    
    assertTrue(validator.isValidWord("HELLO", path));
    assertTrue(validator.coversShape(path, shape));
    assertEquals(5, scoreCalculator.calculatePoints("HELLO", path, 0.8));
}

@Test
public void testBoosterEffects() {
    player.useBooster(BoosterType.DOUBLE_UP);
    int baseScore = 1000;
    int boostedScore = scoreCalculator.applyBoosters(baseScore, player);
    assertEquals(2000, boostedScore);
}
```

### Integration Tests
```java
@Test
public void testMultiplayerSync() {
    // Create room
    Room room = roomService.createRoom(host, config);
    
    // Join players
    roomService.joinRoom(room.getCode(), player1);
    roomService.joinRoom(room.getCode(), player2);
    
    // Start game
    gameEngine.startGame(room);
    
    // Submit words simultaneously
    CompletableFuture<SubmissionResult> future1 = 
        gameEngine.submitWord(player1, word1);
    CompletableFuture<SubmissionResult> future2 = 
        gameEngine.submitWord(player2, word2);
    
    // Verify only one wins
    SubmissionResult result1 = future1.get();
    SubmissionResult result2 = future2.get();
    
    assertTrue(result1.isSuccess() ^ result2.isSuccess());
}
```

## 📊 Performance Optimization

### Server-Side Optimizations
- **Connection Pooling**: Reuse WebSocket connections
- **Message Batching**: Group updates within 50ms window
- **Cache Strategy**: Cache dictionary lookups, grid templates
- **Async Processing**: Non-blocking message handling
- **Load Balancing**: Distribute rooms across server instances

### Client-Side Optimizations
- **Canvas Optimization**: Dirty rectangle rendering
- **Debouncing**: Throttle rapid user inputs
- **Preloading**: Load next level assets during transition
- **Web Workers**: Offload validation to background thread
- **Local Storage**: Cache player preferences and stats

## 🚢 Deployment

### Docker Configuration
```dockerfile
FROM openjdk:11-jre-slim
VOLUME /tmp
COPY target/wordbrain2-multiplayer-1.0.0.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

### Docker Compose
```yaml
version: '3.8'
services:
  app:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - SERVER_PORT=8080
    volumes:
      - ./dictionaries:/app/dictionaries
    restart: unless-stopped
    
  nginx:
    image: nginx:alpine
    ports:
      - "80:80"
      - "443:443"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
      - ./ssl:/etc/nginx/ssl
    depends_on:
      - app
```

### Production Checklist
- [ ] Enable HTTPS with SSL certificates
- [ ] Configure CORS for production domain
- [ ] Set up monitoring (Prometheus + Grafana)
- [ ] Configure log aggregation (ELK stack)
- [ ] Database backup strategy (if persistent)
- [ ] Rate limiting for API endpoints
- [ ] WebSocket connection limits
- [ ] CDN for static assets
- [ ] Health check endpoints
- [ ] Graceful shutdown handling

## 📱 Mobile Support

### Responsive Design
- Touch-optimized grid interaction
- Adaptive layout for different screen sizes
- Virtual keyboard handling
- Gesture support (swipe to select)
- Orientation lock during gameplay

### Progressive Web App
```json
{
  "name": "WordBrain2 Multiplayer",
  "short_name": "WordBrain2",
  "display": "fullscreen",
  "orientation": "portrait",
  "theme_color": "#3498db",
  "background_color": "#ffffff",
  "icons": [
    {
      "src": "/img/icon-192.png",
      "sizes": "192x192",
      "type": "image/png"
    }
  ]
}
```

## 🔒 Security Considerations

### Authentication & Authorization
- Player ID validation
- Room code verification
- Rate limiting per IP
- WebSocket origin validation
- Input sanitization

### Anti-Cheat Measures
- Server-side validation for all moves
- Timestamp verification
- Action rate limiting
- Pattern detection for bot behavior
- Encrypted WebSocket messages

## 📈 Monitoring & Analytics

### Key Metrics
- Active rooms count
- Player concurrency
- Average game duration
- Word validation success rate
- Booster usage statistics
- Network latency percentiles
- Error rates by endpoint

### Logging Strategy
```properties
logging.level.root=INFO
logging.level.com.wordbrain2=DEBUG
logging.pattern.console=%d{HH:mm:ss.SSS} [%thread] %-5level %logger{36} - %msg%n
logging.file.name=logs/wordbrain2.log
logging.file.max-size=10MB
logging.file.max-history=30
```

## 🛠️ Development Setup

### Prerequisites
- Java 11+
- Maven 3.6+
- Node.js 14+ (for frontend tooling)
- Git

### Quick Start
```bash
# Clone repository
git clone https://github.com/yourusername/wordbrain2-multiplayer.git
cd wordbrain2-multiplayer

# Install dependencies
mvn clean install

# Run development server
mvn spring-boot:run

# Access game
open http://localhost:8080
```

### Development Tools
- **IDE**: IntelliJ IDEA / Eclipse / VS Code
- **API Testing**: Postman / Insomnia
- **WebSocket Testing**: wscat / WebSocket King
- **Load Testing**: JMeter / k6
- **Monitoring**: Spring Boot Actuator

## 📝 API Documentation

### REST Endpoints

#### Create Room
```http
POST /api/rooms/create
Content-Type: application/json

{
    "hostName": "Player1",
    "topic": "animals",
    "levelCount": 10,
    "levelDuration": 30,
    "maxPlayers": 10,
    "enabledBoosters": ["DOUBLE_UP", "FREEZE", "REVEAL"]
}

Response:
{
    "roomCode": "ABC123",
    "status": "WAITING",
    "hostId": "player_001"
}
```

#### Join Room
```http
POST /api/rooms/join
Content-Type: application/json

{
    "roomCode": "ABC123",
    "playerName": "Player2"
}

Response:
{
    "success": true,
    "playerId": "player_002",
    "roomDetails": {...}
}
```

#### Get Leaderboard
```http
GET /api/leaderboard?type=room&roomCode=ABC123

Response:
{
    "leaderboard": [
        {
            "rank": 1,
            "playerId": "player_001",
            "name": "Player1",
            "score": 5000,
            "gamesPlayed": 10,
            "winRate": 0.7
        }
    ]
}
```

## 🎯 Roadmap

### Version 1.0 (MVP)
- [x] Basic multiplayer gameplay
- [x] Room management
- [x] Real-time synchronization
- [x] Score tracking
- [x] Basic boosters

### Version 1.1
- [ ] Tournament mode
- [ ] Custom word lists
- [ ] Player profiles
- [ ] Achievement system
- [ ] Sound effects

### Version 1.2
- [ ] Mobile apps (iOS/Android)
- [ ] Social features
- [ ] Daily challenges
- [ ] Replay system
- [ ] Advanced statistics

### Version 2.0
- [ ] AI opponents
- [ ] Voice chat
- [ ] Custom themes
- [ ] Monetization (ads/premium)
- [ ] Global tournaments

---

## 📞 Support & Contact

- **Documentation**: [/docs](http://localhost:8080/docs)
- **Issue Tracker**: GitHub Issues
- **Community**: Discord Server
- **Email**: support@wordbrain2.game

## 📄 License

This project is licensed under the MIT License - see the LICENSE file for details.

---

*Last Updated: December 2024*
*Version: 1.0.0*