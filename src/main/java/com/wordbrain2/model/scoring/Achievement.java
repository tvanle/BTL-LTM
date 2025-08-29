package com.wordbrain2.model.scoring;

import java.time.LocalDateTime;

public class Achievement {
    
    private String id;
    private String name;
    private String description;
    private String type;
    private int requiredValue;
    private int currentProgress;
    private boolean isUnlocked;
    private LocalDateTime unlockedAt;
    private String iconUrl;
    private int pointsReward;
    
    public Achievement() {}
    
    public Achievement(String id, String name, String description, String type, int requiredValue) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.type = type;
        this.requiredValue = requiredValue;
        this.currentProgress = 0;
        this.isUnlocked = false;
    }
    
    // Progress tracking
    public void updateProgress(int progress) {
        this.currentProgress = Math.min(progress, requiredValue);
        if (this.currentProgress >= requiredValue && !isUnlocked) {
            unlock();
        }
    }
    
    public void incrementProgress(int amount) {
        updateProgress(this.currentProgress + amount);
    }
    
    public void unlock() {
        if (!isUnlocked) {
            this.isUnlocked = true;
            this.unlockedAt = LocalDateTime.now();
            this.currentProgress = requiredValue;
        }
    }
    
    public boolean isCompleted() {
        return currentProgress >= requiredValue;
    }
    
    public double getProgressPercentage() {
        if (requiredValue == 0) return 100.0;
        return (double) currentProgress / requiredValue * 100.0;
    }
    
    // Static factory methods for common achievements
    public static Achievement wordMaster(int requiredWords) {
        return new Achievement("word_master", "Word Master", 
            "Find " + requiredWords + " correct words", "word_count", requiredWords);
    }
    
    public static Achievement streakLegend(int requiredStreak) {
        return new Achievement("streak_legend", "Streak Legend", 
            "Achieve a streak of " + requiredStreak + " correct words", "streak", requiredStreak);
    }
    
    public static Achievement speedDemon(int requiredSpeed) {
        return new Achievement("speed_demon", "Speed Demon", 
            "Find a word in under " + requiredSpeed + " seconds", "speed", requiredSpeed);
    }
    
    public static Achievement gameWinner(int requiredWins) {
        return new Achievement("game_winner", "Game Winner", 
            "Win " + requiredWins + " games", "wins", requiredWins);
    }
    
    public static Achievement boosterExpert(int requiredBoosters) {
        return new Achievement("booster_expert", "Booster Expert", 
            "Use " + requiredBoosters + " boosters effectively", "boosters", requiredBoosters);
    }
    
    // Getters
    public String getId() { return id; }
    public String getName() { return name; }
    public String getDescription() { return description; }
    public String getType() { return type; }
    public int getRequiredValue() { return requiredValue; }
    public int getCurrentProgress() { return currentProgress; }
    public boolean isUnlocked() { return isUnlocked; }
    public LocalDateTime getUnlockedAt() { return unlockedAt; }
    public String getIconUrl() { return iconUrl; }
    public int getPointsReward() { return pointsReward; }
    
    // Setters
    public void setId(String id) { this.id = id; }
    public void setName(String name) { this.name = name; }
    public void setDescription(String description) { this.description = description; }
    public void setType(String type) { this.type = type; }
    public void setRequiredValue(int requiredValue) { this.requiredValue = requiredValue; }
    public void setCurrentProgress(int currentProgress) { this.currentProgress = currentProgress; }
    public void setUnlocked(boolean unlocked) { this.isUnlocked = unlocked; }
    public void setUnlockedAt(LocalDateTime unlockedAt) { this.unlockedAt = unlockedAt; }
    public void setIconUrl(String iconUrl) { this.iconUrl = iconUrl; }
    public void setPointsReward(int pointsReward) { this.pointsReward = pointsReward; }
    
    @Override
    public String toString() {
        return "Achievement{" +
               "id='" + id + '\'' +
               ", name='" + name + '\'' +
               ", type='" + type + '\'' +
               ", progress=" + currentProgress + "/" + requiredValue +
               ", unlocked=" + isUnlocked +
               '}';
    }
}