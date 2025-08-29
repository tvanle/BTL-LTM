package com.wordbrain2.service.scoring;

import com.wordbrain2.model.entity.Player;
import com.wordbrain2.model.scoring.Score;
import com.wordbrain2.repository.LeaderboardRepository;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Service
public class StatisticsService {
    
    private final LeaderboardRepository leaderboardRepository;
    private final Map<String, PlayerStats> playerStats = new HashMap<>();
    
    public StatisticsService(LeaderboardRepository leaderboardRepository) {
        this.leaderboardRepository = leaderboardRepository;
    }
    
    public void recordGameStart(String playerId, String roomCode) {
        PlayerStats stats = getOrCreatePlayerStats(playerId);
        stats.incrementGamesPlayed();
        stats.setLastRoomCode(roomCode);
    }
    
    public void recordWordSubmission(String playerId, boolean correct, int points, long responseTime) {
        PlayerStats stats = getOrCreatePlayerStats(playerId);
        
        if (correct) {
            stats.incrementCorrectWords();
            stats.addPoints(points);
        } else {
            stats.incrementWrongWords();
        }
        
        stats.updateAverageResponseTime(responseTime);
    }
    
    public void recordBoosterUsage(String playerId, String boosterType) {
        PlayerStats stats = getOrCreatePlayerStats(playerId);
        stats.incrementBoostersUsed();
        stats.recordBoosterUsage(boosterType);
    }
    
    public void recordGameEnd(String playerId, boolean won, int finalScore) {
        PlayerStats stats = getOrCreatePlayerStats(playerId);
        
        if (won) {
            stats.incrementWins();
        } else {
            stats.incrementLosses();
        }
        
        stats.updateHighScore(finalScore);
    }
    
    public PlayerStats getPlayerStatistics(String playerId) {
        return getOrCreatePlayerStats(playerId);
    }
    
    public double getWinRate(String playerId) {
        PlayerStats stats = playerStats.get(playerId);
        if (stats == null || stats.getGamesPlayed() == 0) {
            return 0.0;
        }
        return (double) stats.getWins() / stats.getGamesPlayed();
    }
    
    public double getAccuracy(String playerId) {
        PlayerStats stats = playerStats.get(playerId);
        if (stats == null) {
            return 0.0;
        }
        
        int totalWords = stats.getCorrectWords() + stats.getWrongWords();
        if (totalWords == 0) {
            return 0.0;
        }
        
        return (double) stats.getCorrectWords() / totalWords;
    }
    
    private PlayerStats getOrCreatePlayerStats(String playerId) {
        return playerStats.computeIfAbsent(playerId, k -> new PlayerStats(playerId));
    }
    
    public static class PlayerStats {
        private final String playerId;
        private int gamesPlayed;
        private int wins;
        private int losses;
        private int correctWords;
        private int wrongWords;
        private int totalPoints;
        private int highScore;
        private int boostersUsed;
        private double averageResponseTime;
        private int responseCount;
        private String lastRoomCode;
        private final Map<String, Integer> boosterUsageCount = new HashMap<>();
        
        public PlayerStats(String playerId) {
            this.playerId = playerId;
        }
        
        // Increment methods
        public void incrementGamesPlayed() { this.gamesPlayed++; }
        public void incrementWins() { this.wins++; }
        public void incrementLosses() { this.losses++; }
        public void incrementCorrectWords() { this.correctWords++; }
        public void incrementWrongWords() { this.wrongWords++; }
        public void incrementBoostersUsed() { this.boostersUsed++; }
        
        public void addPoints(int points) { this.totalPoints += points; }
        
        public void updateHighScore(int score) {
            if (score > this.highScore) {
                this.highScore = score;
            }
        }
        
        public void updateAverageResponseTime(long responseTime) {
            this.averageResponseTime = (this.averageResponseTime * this.responseCount + responseTime) / (this.responseCount + 1);
            this.responseCount++;
        }
        
        public void recordBoosterUsage(String boosterType) {
            boosterUsageCount.merge(boosterType, 1, Integer::sum);
        }
        
        // Getters
        public String getPlayerId() { return playerId; }
        public int getGamesPlayed() { return gamesPlayed; }
        public int getWins() { return wins; }
        public int getLosses() { return losses; }
        public int getCorrectWords() { return correctWords; }
        public int getWrongWords() { return wrongWords; }
        public int getTotalPoints() { return totalPoints; }
        public int getHighScore() { return highScore; }
        public int getBoostersUsed() { return boostersUsed; }
        public double getAverageResponseTime() { return averageResponseTime; }
        public String getLastRoomCode() { return lastRoomCode; }
        public void setLastRoomCode(String lastRoomCode) { this.lastRoomCode = lastRoomCode; }
        public Map<String, Integer> getBoosterUsageCount() { return new HashMap<>(boosterUsageCount); }
    }
}