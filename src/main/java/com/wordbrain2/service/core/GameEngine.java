package com.wordbrain2.service.core;

import com.wordbrain2.config.GameConfig;
import com.wordbrain2.model.entity.GameSession;
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
        room.getPlayers().values().forEach(player -> {
            session.updatePlayerScore(player.getId(), 0);
        });
        
        log.info("Game started for room: {}", roomCode);
        
        return Map.of(
            "roomCode", roomCode,
            "status", "STARTED",
            "levelCount", room.getLevelCount(),
            "players", room.getPlayers().size()
        );
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
        
        return Map.of(
            "level", levelNumber,
            "grid", convertGridToMap(grid),
            "duration", level.getDuration(),
            "serverTime", System.currentTimeMillis(),
            "targetWords", level.getTargetWordCount()
        );
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
        
        if (isValid && inDictionary) {
            // Calculate score
            long timeRemaining = calculateTimeRemaining(session);
            double speedFactor = calculateSpeedFactor(timeRemaining, level.getDuration());
            int basePoints = gameConfig.getScore().getBasePoints();
            int points = scoreCalculator.calculateScore(basePoints, speedFactor, 
                                                       room.getPlayers().get(playerId));
            
            // Update player score
            room.getPlayers().get(playerId).addScore(points);
            room.getPlayers().get(playerId).incrementStreak();
            session.updatePlayerScore(playerId, points);
            
            return Map.of(
                "correct", true,
                "word", word,
                "points", points,
                "streak", room.getPlayers().get(playerId).getCurrentStreak()
            );
        } else {
            // Wrong answer
            room.getPlayers().get(playerId).resetStreak();
            
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
        
        return Map.of(
            "success", true,
            "boosterType", boosterType,
            "applied", true
        );
    }
    
    public Map<String, Object> getLeaderboard(String roomCode) {
        Room room = roomService.getRoom(roomCode);
        if (room == null || room.getGameSession() == null) {
            return null;
        }
        
        GameSession session = room.getGameSession();
        
        List<Map<String, Object>> leaderboard = room.getPlayers().values().stream()
            .sorted((p1, p2) -> Integer.compare(p2.getTotalScore(), p1.getTotalScore()))
            .map(player -> Map.of(
                "rank", 0, // Will be set after sorting
                "playerId", (Object) player.getId(),
                "name", player.getName(),
                "score", player.getTotalScore(),
                "streak", player.getCurrentStreak()
            ))
            .collect(Collectors.toList());
        
        // Set ranks
        for (int i = 0; i < leaderboard.size(); i++) {
            ((Map<String, Object>) leaderboard.get(i)).put("rank", i + 1);
        }
        
        Level currentLevel = session.getCurrentLevel();
        
        return Map.of(
            "leaderboard", leaderboard,
            "levelProgress", Map.of(
                "current", session.getCurrentLevelIndex() + 1,
                "total", session.getLevels().size(),
                "timeRemaining", calculateTimeRemaining(session) / 1000
            )
        );
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
        
        return Map.of(
            "rows", grid.getRows(),
            "cols", grid.getCols(),
            "cells", cells,
            "shape", Map.of(
                "mask", mask,
                "cellCount", grid.getTotalCells()
            )
        );
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