package com.wordbrain2.model.enums;

public enum BoosterType {
    DOUBLE_UP("DoubleUp", "x2 points for current word"),
    FREEZE("Freeze", "Freeze all opponents for 3 seconds"),
    REVEAL("Reveal", "Reveal one correct letter"),
    TIME_PLUS("Time+5", "Add 5 seconds to timer"),
    SHIELD("Shield", "Block one negative effect"),
    STREAK_SAVE("StreakSave", "Preserve streak on wrong answer"),
    SKIP_HALF("SkipHalf", "Skip level with 50% points");
    
    private final String displayName;
    private final String description;
    
    BoosterType(String displayName, String description) {
        this.displayName = displayName;
        this.description = description;
    }
    
    public String getDisplayName() {
        return displayName;
    }
    
    public String getDescription() {
        return description;
    }
}