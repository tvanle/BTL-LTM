package com.wordbrain2.model.game;

import com.wordbrain2.model.enums.GamePhase;
import lombok.Data;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class GameState {
    private String roomCode;
    private GamePhase phase;
    private Grid currentGrid;
    private int currentLevel;
    private int totalLevels;
    private long timeRemaining;
    private Map<String, PlayerGameState> playerStates;
    
    public GameState() {
        this.playerStates = new ConcurrentHashMap<>();
        this.phase = GamePhase.LOBBY;
    }
    
    @Data
    public static class PlayerGameState {
        private String playerId;
        private int score;
        private int streak;
        private int correctWords;
        private boolean hasSubmitted;
        private Map<String, Integer> boosterUsage;
        
        public PlayerGameState(String playerId) {
            this.playerId = playerId;
            this.score = 0;
            this.streak = 0;
            this.correctWords = 0;
            this.hasSubmitted = false;
            this.boosterUsage = new ConcurrentHashMap<>();
        }
    }
}