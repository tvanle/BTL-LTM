package com.crossword.model;

public class Player {
    private String id;
    private String name;
    private int correctCells;
    private int completedWords;
    private int totalScore;
    private boolean connected;
    private long lastActivity;
    
    public Player() {}
    
    public Player(String id, String name) {
        this.id = id;
        this.name = name;
        this.correctCells = 0;
        this.completedWords = 0;
        this.totalScore = 0;
        this.connected = true;
        this.lastActivity = System.currentTimeMillis();
    }
    
    public void addCellScore() {
        correctCells++;
        totalScore++;
    }
    
    public void addWordBonus(int wordLength) {
        completedWords++;
        totalScore += wordLength;
    }
    
    public void updateActivity() {
        this.lastActivity = System.currentTimeMillis();
    }
    
    // Getters and setters
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }
    
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    
    public int getCorrectCells() { return correctCells; }
    public void setCorrectCells(int correctCells) { this.correctCells = correctCells; }
    
    public int getCompletedWords() { return completedWords; }
    public void setCompletedWords(int completedWords) { this.completedWords = completedWords; }
    
    public int getTotalScore() { return totalScore; }
    public void setTotalScore(int totalScore) { this.totalScore = totalScore; }
    
    public boolean isConnected() { return connected; }
    public void setConnected(boolean connected) { this.connected = connected; }
    
    public long getLastActivity() { return lastActivity; }
    public void setLastActivity(long lastActivity) { this.lastActivity = lastActivity; }
}