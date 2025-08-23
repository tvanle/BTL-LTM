package com.wordbrain2.model.game;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Level {
    private int levelNumber;
    private Grid grid;
    private int duration; // in seconds
    private List<String> targetWords;
    private int targetWordCount;
    private boolean completed;
    
    public Level(int levelNumber) {
        this.levelNumber = levelNumber;
        this.duration = 30; // default 30 seconds
        this.targetWords = new ArrayList<>();
        this.targetWordCount = calculateTargetWordCount(levelNumber);
        this.completed = false;
    }
    
    private int calculateTargetWordCount(int level) {
        // Increase difficulty with level
        if (level <= 3) return 1;
        if (level <= 6) return 2;
        return 3;
    }
    
    public void setGrid(Grid grid) {
        this.grid = grid;
    }
    
    public void complete() {
        this.completed = true;
    }
}