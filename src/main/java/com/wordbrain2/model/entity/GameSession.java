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
}