package com.wordbrain2.model.scoring;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Streak {
    private String playerId;
    private int currentStreak;
    private int maxStreak;
    private int streakMultiplier;
    private boolean streakSaved;
    private long streakStartTime;
    private long lastCorrectTime;
    
    public void incrementStreak() {
        currentStreak++;
        if (currentStreak > maxStreak) {
            maxStreak = currentStreak;
        }
        lastCorrectTime = System.currentTimeMillis();
        updateMultiplier();
    }
    
    public void resetStreak() {
        if (!streakSaved) {
            currentStreak = 0;
            streakMultiplier = 1;
            streakStartTime = 0;
        } else {
            streakSaved = false;
        }
    }
    
    public void saveStreak() {
        streakSaved = true;
    }
    
    private void updateMultiplier() {
        if (currentStreak >= 10) {
            streakMultiplier = 3;
        } else if (currentStreak >= 5) {
            streakMultiplier = 2;
        } else {
            streakMultiplier = 1;
        }
    }
    
    public double getStreakBonus() {
        return 0.1 * currentStreak;
    }
}