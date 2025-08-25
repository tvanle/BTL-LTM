package com.wordbrain2.model.entity;

import com.wordbrain2.model.enums.PlayerStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class Player {
    private String id;
    private String name;
    private String sessionId;
    private String roomCode;
    private PlayerStatus status;
    private int totalScore;
    private int currentStreak;
    private int maxStreak;
    private int correctWords;
    private int boostersUsed;
    private boolean isHost;
    private LocalDateTime joinedAt;
    private LocalDateTime lastActivityAt;
    
    public Player() {
        this.id = UUID.randomUUID().toString();
        this.status = PlayerStatus.IDLE;
        this.totalScore = 0;
        this.currentStreak = 0;
        this.maxStreak = 0;
        this.correctWords = 0;
        this.boostersUsed = 0;
        this.joinedAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
    }
    
    public Player(String name, String sessionId) {
        this();
        this.name = name;
        this.sessionId = sessionId;
    }
    
    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }
    
    public void incrementStreak() {
        this.currentStreak++;
        if (this.currentStreak > this.maxStreak) {
            this.maxStreak = this.currentStreak;
        }
    }
    
    public void resetStreak() {
        this.currentStreak = 0;
    }
    
    public void addScore(int points) {
        this.totalScore += points;
    }
}