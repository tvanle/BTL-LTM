package com.wordbrain2.service.core;

import com.wordbrain2.config.GameConfig;
import com.wordbrain2.model.entity.GameSession;
import com.wordbrain2.model.entity.Player;
import com.wordbrain2.model.entity.Room;
import com.wordbrain2.model.enums.GamePhase;
import com.wordbrain2.model.enums.SubmissionResult;
import com.wordbrain2.model.game.*;
import com.wordbrain2.service.game.DictionaryService;
import com.wordbrain2.service.game.GridGeneratorService;
import com.wordbrain2.service.game.WordValidationService;
import com.wordbrain2.service.scoring.ScoreCalculator;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GameEngine {
    
    private final RoomService roomService;
    private final GridGeneratorService gridGenerator;
    private final WordValidationService wordValidator;
    private final DictionaryService dictionaryService;
    private final ScoreCalculator scoreCalculator;
    private final GameConfig gameConfig;
    
    public GameEngine(RoomService roomService, 
                      GridGeneratorService gridGenerator,
                      WordValidationService wordValidator,
                      DictionaryService dictionaryService,
                      ScoreCalculator scoreCalculator,
                      GameConfig gameConfig) {
        this.roomService = roomService;
        this.gridGenerator = gridGenerator;
        this.wordValidator = wordValidator;
        this.dictionaryService = dictionaryService;
        this.scoreCalculator = scoreCalculator;
        this.gameConfig = gameConfig;
    }
    
    public Map<String, Object> startGame(String roomCode) {
        Room room = roomService.getRoom(roomCode);
        if (room == null) {
            return null;
        }
        
        // Create game session
        GameSession session = new GameSession(roomCode, room.getLevelCount());
        room.setGameSession(session);
        session.startGame();
        
        // Initialize player scores
        room.getPlayers().forEach(player -> {
            session.updatePlayerScore(player.getId(), 0);
        });
        
        log.info("Game started for room: {}", roomCode);
        
        Map<String, Object> result = new HashMap<>();
        result.put("roomCode", roomCode);
        result.put("status", "STARTED");
        result.put("levelCount", room.getLevelCount());
        result.put("players", room.getPlayers().size());
        return result;
    }
    
    public Map<String, Object> startLevel(String roomCode, int levelNumber) {
        Room room = roomService.getRoom(roomCode);
        if (room == null || room.getGameSession() == null) {
            return null;
        }
        
        GameSession session = room.getGameSession();
        Level level = session.getCurrentLevel();
        
        if (level == null) {
            return null;
        }
        
        // Generate grid for this level
        int gridSize = calculateGridSize(levelNumber);
        Grid grid = gridGenerator.generateGrid(gridSize, gridSize, levelNumber);
        level.setGrid(grid);
        
        // Generate words for the grid
        List<String> words = dictionaryService.getRandomWords(
            room.getTopic(), 
            level.getTargetWordCount(),
            grid.getTotalCells()
        );
        
        grid.fillWithLetters(words);
        level.setTargetWords(words);
        
        Map<String, Object> result = new HashMap<>();
        result.put("level", levelNumber);
        result.put("grid", convertGridToMap(grid));
        result.put("duration", level.getDuration());
        result.put("serverTime", System.currentTimeMillis());
        result.put("targetWords", level.getTargetWordCount());
        return result;
    }
    
    // Overloaded method for WebSocket handler
    public Map<String, Object> submitWord(String roomCode, String playerId, List<Cell> path, String word) {
        Room room = roomService.getRoom(roomCode);
        if (room == null || room.getGameSession() == null) {
            return null;
        }
        
        GameSession session = room.getGameSession();
        Level level = session.getCurrentLevel();
        Grid grid = level.getGrid();
        
        // Validate the word
        boolean isValid = wordValidator.validateWord(word, path, grid);
        boolean inDictionary = dictionaryService.isValidWord(word);
        
        Map<String, Object> result = new HashMap<>();
        
        if (isValid && inDictionary) {
            // Calculate score
            long timeRemaining = calculateTimeRemaining(session);
            double speedFactor = calculateSpeedFactor(timeRemaining, level.getDuration());
            int basePoints = gameConfig.getScore().getBasePoints();
            // Find player in room
            Player player = room.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElse(null);
            
            if (player == null) {
                result.put("result", SubmissionResult.INCORRECT);
                result.put("reason", "Player not found");
                return result;
            }
            
            int points = scoreCalculator.calculateScore(basePoints, speedFactor, player);
            
            // Update player score
            player.addScore(points);
            player.incrementStreak();
            
            result.put("result", SubmissionResult.CORRECT);
            result.put("points", points);
            result.put("word", word);
        } else {
            // Find and update player
            Player playerToReset = room.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElse(null);
            if (playerToReset != null) {
                playerToReset.resetStreak();
            }
            result.put("result", SubmissionResult.INCORRECT);
            result.put("word", word);
            result.put("reason", !inDictionary ? "Not in dictionary" : "Invalid path");
        }
        
        return result;
    }
    
    public Map<String, Object> submitWord(String roomCode, String playerId, Object data) {
        Room room = roomService.getRoom(roomCode);
        if (room == null || room.getGameSession() == null) {
            return null;
        }
        
        Map<String, Object> submission = (Map<String, Object>) data;
        List<Map<String, Object>> pathData = (List<Map<String, Object>>) submission.get("path");
        String word = (String) submission.get("word");
        
        // Convert path data to Cell objects
        GameSession session = room.getGameSession();
        Level level = session.getCurrentLevel();
        Grid grid = level.getGrid();
        
        List<Cell> path = pathData.stream()
            .map(cellData -> {
                int row = ((Number) cellData.get("row")).intValue();
                int col = ((Number) cellData.get("col")).intValue();
                return grid.getCell(row, col);
            })
            .collect(Collectors.toList());
        
        // Validate the word
        boolean isValid = wordValidator.validateWord(word, path, grid);
        boolean inDictionary = dictionaryService.isValidWord(word);
        
        Map<String, Object> result = new HashMap<>();
        
        if (isValid && inDictionary) {
            // Calculate score
            long timeRemaining = calculateTimeRemaining(session);
            double speedFactor = calculateSpeedFactor(timeRemaining, level.getDuration());
            int basePoints = gameConfig.getScore().getBasePoints();
            // Find player in room
            Player player = room.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElse(null);
            
            if (player == null) {
                result.put("result", SubmissionResult.INCORRECT);
                result.put("reason", "Player not found");
                return result;
            }
            
            int points = scoreCalculator.calculateScore(basePoints, speedFactor, player);
            
            // Update player score
            player.addScore(points);
            player.incrementStreak();
            session.updatePlayerScore(playerId, points);
            
            result.put("correct", true);
            result.put("word", word);
            result.put("points", points);
            result.put("streak", player.getCurrentStreak());
            return result;
        } else {
            // Wrong answer
            // Find and update player
            Player playerToReset = room.getPlayers().stream()
                .filter(p -> p.getId().equals(playerId))
                .findFirst()
                .orElse(null);
            if (playerToReset != null) {
                playerToReset.resetStreak();
            }
            
            return Map.of(
                "correct", false,
                "word", word,
                "reason", !inDictionary ? "Not in dictionary" : "Invalid path"
            );
        }
    }
    
    public Map<String, Object> useBooster(String roomCode, String playerId, Object data) {
        // Implementation for booster usage
        Map<String, Object> boosterData = (Map<String, Object>) data;
        String boosterType = (String) boosterData.get("boosterType");
        
        log.info("Player {} used booster {} in room {}", playerId, boosterType, roomCode);
        
        // Apply booster effects based on type
        // This is simplified - full implementation would involve the BoosterService
        
        Map<String, Object> boosterResult = new HashMap<>();
        boosterResult.put("success", true);
        boosterResult.put("boosterType", boosterType);
        boosterResult.put("applied", true);
        return boosterResult;
    }
    
    public Map<String, Object> pauseGame(String roomCode, String playerId) {
        Room room = roomService.getRoom(roomCode);
        if (room == null || !room.getHostId().equals(playerId)) {
            return null;
        }
        
        GameSession session = room.getGameSession();
        if (session != null) {
            session.setPhase(GamePhase.LEVEL_END);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "PAUSED");
        result.put("roomCode", roomCode);
        return result;
    }
    
    public Map<String, Object> resumeGame(String roomCode, String playerId) {
        Room room = roomService.getRoom(roomCode);
        if (room == null || !room.getHostId().equals(playerId)) {
            return null;
        }
        
        GameSession session = room.getGameSession();
        if (session != null) {
            session.setPhase(GamePhase.PLAYING);
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "RESUMED");
        result.put("roomCode", roomCode);
        return result;
    }
    
    public Map<String, Object> endGame(String roomCode, String playerId) {
        Room room = roomService.getRoom(roomCode);
        if (room == null || !room.getHostId().equals(playerId)) {
            return null;
        }
        
        GameSession session = room.getGameSession();
        if (session != null) {
            session.endGame();
        }
        
        Map<String, Object> result = new HashMap<>();
        result.put("status", "ENDED");
        result.put("roomCode", roomCode);
        return result;
    }
    
    public Map<String, Object> getHint(String roomCode, String playerId) {
        Room room = roomService.getRoom(roomCode);
        if (room == null) {
            return null;
        }
        
        GameSession session = room.getGameSession();
        if (session == null || session.getCurrentLevel() == null) {
            return null;
        }
        
        Level level = session.getCurrentLevel();
        List<String> targetWords = level.getTargetWords();
        
        Map<String, Object> result = new HashMap<>();
        if (!targetWords.isEmpty()) {
            String hint = targetWords.get(0).substring(0, Math.min(3, targetWords.get(0).length()));
            result.put("hint", hint);
            result.put("message", "First 3 letters of a word");
        }
        return result;
    }
    
    public Map<String, Object> getLeaderboard(String roomCode) {
        Room room = roomService.getRoom(roomCode);
        if (room == null || room.getGameSession() == null) {
            return null;
        }
        
        GameSession session = room.getGameSession();
        
        List<Map<String, Object>> leaderboard = room.getPlayers().stream()
            .sorted((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()))
            .map(player -> {
                Map<String, Object> playerData = new HashMap<>();
                playerData.put("rank", 0);
                playerData.put("playerId", player.getId());
                playerData.put("name", player.getName());
                playerData.put("score", player.getTotalScore());
                playerData.put("streak", player.getCurrentStreak());
                return playerData;
            })
            .collect(Collectors.toList());
        
        // Set ranks
        for (int i = 0; i < leaderboard.size(); i++) {
            ((Map<String, Object>) leaderboard.get(i)).put("rank", i + 1);
        }
        
        Level currentLevel = session.getCurrentLevel();
        
        Map<String, Object> levelProgress = new HashMap<>();
        levelProgress.put("current", session.getCurrentLevelIndex() + 1);
        levelProgress.put("total", session.getLevels().size());
        levelProgress.put("timeRemaining", calculateTimeRemaining(session) / 1000);
        
        Map<String, Object> leaderboardResult = new HashMap<>();
        leaderboardResult.put("leaderboard", leaderboard);
        leaderboardResult.put("levelProgress", levelProgress);
        return leaderboardResult;
    }
    
    private int calculateGridSize(int levelNumber) {
        // Increase grid size with level
        int minSize = gameConfig.getGrid().getMinSize();
        int maxSize = gameConfig.getGrid().getMaxSize();
        
        int size = minSize + (levelNumber - 1) / 3;
        return Math.min(size, maxSize);
    }
    
    private Map<String, Object> convertGridToMap(Grid grid) {
        char[][] cells = new char[grid.getRows()][grid.getCols()];
        boolean[][] mask = new boolean[grid.getRows()][grid.getCols()];
        
        for (int i = 0; i < grid.getRows(); i++) {
            for (int j = 0; j < grid.getCols(); j++) {
                Cell cell = grid.getCell(i, j);
                cells[i][j] = cell.getCharacter();
                mask[i][j] = cell.isActive();
            }
        }
        
        Map<String, Object> shape = new HashMap<>();
        shape.put("mask", mask);
        shape.put("cellCount", grid.getTotalCells());
        
        Map<String, Object> gridMap = new HashMap<>();
        gridMap.put("rows", grid.getRows());
        gridMap.put("cols", grid.getCols());
        gridMap.put("cells", cells);
        gridMap.put("shape", shape);
        return gridMap;
    }
    
    private long calculateTimeRemaining(GameSession session) {
        Level level = session.getCurrentLevel();
        if (level == null) return 0;
        
        long elapsed = System.currentTimeMillis() - session.getLevelStartTime();
        long duration = level.getDuration() * 1000L;
        
        return Math.max(0, duration - elapsed);
    }
    
    private double calculateSpeedFactor(long timeRemaining, int totalDuration) {
        double percentRemaining = (double) timeRemaining / (totalDuration * 1000);
        double minMultiplier = gameConfig.getScore().getSpeedMultiplierMin();
        double maxMultiplier = gameConfig.getScore().getSpeedMultiplierMax();
        
        return minMultiplier + (maxMultiplier - minMultiplier) * percentRemaining;
    }
}