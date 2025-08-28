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

## 📁 Project Structure

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
│   ├── 📂 static/                              # Frontend files (HTML, CSS, JS)
│   ├── 📂 dictionaries/                        # Word dictionaries
│   ├── 📂 templates/                           # Email templates
│   ├── 📄 application.properties               # Spring configuration
│   ├── 📄 application-dev.properties           # Development config
│   └── 📄 application-prod.properties          # Production config
│
└── 📂 src/test/                                # Unit & integration tests
```

## 🎯 Game Flow

### 1. Room Creation & Join Flow
- Admin creates room with settings (topic, levels, time, boosters)
- Server generates 6-character room code
- Players join using room code
- Admin starts game when ready

### 2. Gameplay Message Flow
- Player submits word via WebSocket
- Server validates (dictionary, path, shape)
- Points calculated and broadcast
- Real-time leaderboard updates
- Booster effects applied and synchronized

### 3. Level Progression Flow
- Level ending countdown (10s)
- Show level results
- Next level preparation (5s)
- New grid and timer start

## 💾 Core Data Models

### Room Model
- Room code, host ID, topic
- Max players (20), level count, duration
- Player list and ready status
- Allowed boosters

### Grid & Shape Model
- Grid dimensions and cell array
- Shape mask for active areas
- Valid word solutions
- Cell count tracking

### Submission & Scoring
- Player ID, cell path, word
- Validation result
- Points calculation
- Streak tracking

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



## 🚀 WebSocket Protocol

### Message Types
- **Room Management**: CREATE_ROOM, JOIN_ROOM, LEAVE_ROOM, PLAYER_READY, START_GAME
- **Game Flow**: GAME_STARTING, LEVEL_START, LEVEL_END, GAME_END
- **Player Actions**: SUBMIT_WORD, USE_BOOSTER, REQUEST_HINT
- **Server Responses**: WORD_ACCEPTED, WORD_REJECTED, BOOSTER_APPLIED, EFFECT_RECEIVED
- **Real-time Updates**: LEADERBOARD_UPDATE, OPPONENT_SCORED, TIMER_UPDATE
- **Connection**: PLAYER_DISCONNECTED, PLAYER_RECONNECTED
- **Errors**: ERROR, INVALID_ACTION, TIMEOUT

### Sample Message Format
```json
{
    "type": "MESSAGE_TYPE",
    "roomCode": "ABC123",
    "playerId": "player_001",
    "data": {
        // Message-specific data
    }
}
```

## 🎮 Features

### Core Gameplay
- Real-time multiplayer word finding
- Drag-and-drop or click selection
- Shape-based grid areas
- Level progression system
- Timer synchronization

### Booster System
- **DoubleUp**: 2x points for next word
- **Freeze**: Freeze opponents for 3s
- **Reveal**: Show hint letter
- **TimeBonus**: +5 seconds
- **Shield**: Block freeze effects
- **StreakSaver**: Preserve streak on mistake
- **SkipHalf**: Skip level with 50% points

### Scoring System
- Base points per word
- Speed multiplier
- Streak bonuses
- Wrong answer penalties
- Booster effects

## 📊 Performance Optimization

### Server-Side
- Connection pooling
- Message batching
- Cache strategy
- Async processing
- Load balancing

### Client-Side
- Canvas optimization
- Input debouncing
- Asset preloading
- Web Workers
- Local storage caching

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
- Touch-optimized grid interaction
- Adaptive layout for different screen sizes
- Virtual keyboard handling
- Gesture support (swipe to select)
- Orientation lock during gameplay
- Progressive Web App support

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