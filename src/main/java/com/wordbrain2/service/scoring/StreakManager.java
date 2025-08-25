package com.wordbrain2.service.scoring;

import com.wordbrain2.model.scoring.Streak;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class StreakManager {
    private final Map<String, Streak> playerStreaks = new ConcurrentHashMap<>();
    
    public Streak getStreak(String playerId) {
        return playerStreaks.computeIfAbsent(playerId, k -> 
            Streak.builder()
                .playerId(playerId)
                .currentStreak(0)
                .maxStreak(0)
                .streakMultiplier(1)
                .streakSaved(false)
                .build()
        );
    }
    
    public void incrementStreak(String playerId) {
        Streak streak = getStreak(playerId);
        streak.incrementStreak();
    }
    
    public void resetStreak(String playerId) {
        Streak streak = getStreak(playerId);
        streak.resetStreak();
    }
    
    public void saveStreak(String playerId) {
        Streak streak = getStreak(playerId);
        streak.saveStreak();
    }
    
    public int getStreakMultiplier(String playerId) {
        return getStreak(playerId).getStreakMultiplier();
    }
    
    public double getStreakBonus(String playerId) {
        return getStreak(playerId).getStreakBonus();
    }
    
    public void clearPlayerStreak(String playerId) {
        playerStreaks.remove(playerId);
    }
    
    public void clearAllStreaks() {
        playerStreaks.clear();
    }
}