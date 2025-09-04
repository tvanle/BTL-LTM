package com.wordbrain2.model.entity;

import com.wordbrain2.model.enums.GamePhase;
import com.wordbrain2.model.game.GameState;
import com.wordbrain2.model.game.Level;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class GameSession {
    private String sessionId;
    private String roomCode;
    private GamePhase phase;
    private GameState currentState;
    private int currentLevelIndex;
    private List<Level> levels;
    private Map<String, Integer> playerScores;
    private Map<String, Integer> playerWordIndexes; // Track each player's progress
    private Map<String, List<String>> playerCompletedWords; // Track completed words per player
    private LocalDateTime startTime;
    private LocalDateTime endTime;
    private long levelStartTime;
    
    public GameSession(String roomCode, int levelCount) {
        this.sessionId = java.util.UUID.randomUUID().toString();
        this.roomCode = roomCode;
        this.phase = GamePhase.LOBBY;
        this.currentLevelIndex = 0;
        this.levels = new ArrayList<>();
        this.playerScores = new ConcurrentHashMap<>();
        this.playerWordIndexes = new ConcurrentHashMap<>();
        this.playerCompletedWords = new ConcurrentHashMap<>();
        
        // Initialize levels
        for (int i = 0; i < levelCount; i++) {
            levels.add(new Level(i + 1));
        }
    }
    
    public void startGame() {
        this.phase = GamePhase.PLAYING;
        this.startTime = LocalDateTime.now();
        this.levelStartTime = System.currentTimeMillis();
    }
    
    public void nextLevel() {
        if (currentLevelIndex < levels.size() - 1) {
            currentLevelIndex++;
            levelStartTime = System.currentTimeMillis();
            // Reset all players' progress for new level
            playerWordIndexes.clear();
            playerCompletedWords.clear();
        } else {
            endGame();
        }
    }
    
    public void endGame() {
        this.phase = GamePhase.FINISHED;
        this.endTime = LocalDateTime.now();
    }
    
    public Level getCurrentLevel() {
        if (currentLevelIndex < levels.size()) {
            return levels.get(currentLevelIndex);
        }
        return null;
    }
    
    public boolean isLastLevel() {
        return currentLevelIndex >= levels.size() - 1;
    }
    
    public void updatePlayerScore(String playerId, int points) {
        playerScores.merge(playerId, points, Integer::sum);
    }
    
    public int getPlayerScore(String playerId) {
        return playerScores.getOrDefault(playerId, 0);
    }
    
    public String getId() {
        return sessionId;
    }
    
    public boolean isActive() {
        return phase == GamePhase.PLAYING || phase == GamePhase.LEVEL_END;
    }
    
    public int getPlayerWordIndex(String playerId) {
        return playerWordIndexes.getOrDefault(playerId, 0);
    }
    
    public void incrementPlayerWordIndex(String playerId) {
        playerWordIndexes.put(playerId, playerWordIndexes.getOrDefault(playerId, 0) + 1);
    }
    
    public void addCompletedWord(String playerId, String word) {
        playerCompletedWords.computeIfAbsent(playerId, k -> new ArrayList<>()).add(word);
    }
    
    public boolean hasPlayerCompletedLevel(String playerId) {
        Level currentLevel = getCurrentLevel();
        if (currentLevel == null || currentLevel.getTargetWords() == null) {
            return false;
        }
        
        List<String> completedWords = playerCompletedWords.getOrDefault(playerId, new ArrayList<>());
        return completedWords.size() >= currentLevel.getTargetWords().size();
    }
    
    public boolean allPlayersCompletedLevel(List<String> playerIds) {
        return playerIds.stream().allMatch(this::hasPlayerCompletedLevel);
    }
}