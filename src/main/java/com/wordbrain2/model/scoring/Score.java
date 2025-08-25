package com.wordbrain2.model.scoring;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Score {
    private String playerId;
    private int totalPoints;
    private int correctWords;
    private int incorrectWords;
    private int currentStreak;
    private int maxStreak;
    private double avgResponseTime;
    private int boostersUsed;
    private int rank;
    private int levelPoints;
    private long lastUpdated;
    
    public void addPoints(int points) {
        this.totalPoints += points;
        this.levelPoints += points;
        this.lastUpdated = System.currentTimeMillis();
    }
    
    public void incrementCorrect() {
        this.correctWords++;
        this.currentStreak++;
        if (currentStreak > maxStreak) {
            maxStreak = currentStreak;
        }
    }
    
    public void incrementIncorrect() {
        this.incorrectWords++;
        this.currentStreak = 0;
    }
    
    public void resetLevelPoints() {
        this.levelPoints = 0;
    }
    
    public double getAccuracy() {
        int total = correctWords + incorrectWords;
        return total > 0 ? (double) correctWords / total : 0.0;
    }
}