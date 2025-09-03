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
    private List<Integer> wordTargets; // Word length targets (e.g., [3, 4, 5])
    private List<String> completedWords;
    private int targetWordCount;
    private boolean completed;
    
    public Level(int levelNumber) {
        this.levelNumber = levelNumber;
        this.duration = 30; // default 30 seconds
        this.targetWords = new ArrayList<>();
        this.wordTargets = new ArrayList<>();
        this.completedWords = new ArrayList<>();
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
    
    public void addCompletedWord(String word) {
        if (!completedWords.contains(word)) {
            completedWords.add(word);
        }
    }
    
    public boolean isWordCompleted(String word) {
        return completedWords.contains(word);
    }
    
    public boolean isComplete() {
        // Level is complete when all word targets are found
        return completedWords.size() >= wordTargets.size();
    }
    
    public List<Integer> getRemainingTargets() {
        List<Integer> remaining = new ArrayList<>(wordTargets);
        for (String completed : completedWords) {
            remaining.remove(Integer.valueOf(completed.length()));
        }
        return remaining;
    }
}