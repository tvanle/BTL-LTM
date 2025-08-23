package com.wordbrain2.service.scoring;

import com.wordbrain2.config.GameConfig;
import com.wordbrain2.model.entity.Player;
import org.springframework.stereotype.Service;

@Service
public class ScoreCalculator {
    
    private final GameConfig gameConfig;
    
    public ScoreCalculator(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
    }
    
    public int calculateScore(int basePoints, double speedFactor, Player player) {
        // Base score with speed multiplier
        double score = basePoints * speedFactor;
        
        // Apply streak multiplier
        double streakMultiplier = calculateStreakMultiplier(player.getCurrentStreak());
        score *= streakMultiplier;
        
        return (int) Math.round(score);
    }
    
    private double calculateStreakMultiplier(int streak) {
        if (streak <= 0) return 1.0;
        
        double bonus = gameConfig.getScore().getStreakBonus();
        double maxMultiplier = gameConfig.getScore().getStreakMaxMultiplier();
        
        double multiplier = 1.0 + (streak * bonus);
        return Math.min(multiplier, maxMultiplier);
    }
    
    public int calculatePenalty(int wrongAttempts) {
        int penalty = gameConfig.getScore().getPenaltyWrong();
        int maxPenalties = gameConfig.getScore().getPenaltyMax();
        
        int actualPenalties = Math.min(wrongAttempts, maxPenalties);
        return penalty * actualPenalties;
    }
}