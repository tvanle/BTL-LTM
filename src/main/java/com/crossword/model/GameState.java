package com.crossword.model;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class GameState {
    private String gameId;
    private CrosswordGrid grid;
    private Map<String, Player> players;
    private GameStatus status;
    private long startTime;
    private long endTime;
    private int gameDurationSeconds;
    private String winnerId;
    
    public enum GameStatus {
        WAITING, IN_PROGRESS, FINISHED
    }
    
    public GameState() {}
    
    public GameState(String gameId, CrosswordGrid grid) {
        this.gameId = gameId;
        this.grid = grid;
        this.players = new ConcurrentHashMap<>();
        this.status = GameStatus.WAITING;
        this.gameDurationSeconds = 180; // 3 minutes default
        this.startTime = 0;
        this.endTime = 0;
        this.winnerId = null;
    }
    
    public void startGame() {
        this.status = GameStatus.IN_PROGRESS;
        this.startTime = System.currentTimeMillis();
        this.endTime = startTime + (gameDurationSeconds * 1000L);
    }
    
    public void endGame() {
        this.status = GameStatus.FINISHED;
        this.endTime = System.currentTimeMillis();
        determineWinner();
    }
    
    private void determineWinner() {
        Player winner = null;
        int maxScore = -1;
        
        for (Player player : players.values()) {
            if (player.getTotalScore() > maxScore) {
                maxScore = player.getTotalScore();
                winner = player;
            } else if (player.getTotalScore() == maxScore && winner != null) {
                // Tie-breaker: most completed words
                if (player.getCompletedWords() > winner.getCompletedWords()) {
                    winner = player;
                }
            }
        }
        
        this.winnerId = (winner != null) ? winner.getId() : null;
    }
    
    public long getRemainingTimeSeconds() {
        if (status != GameStatus.IN_PROGRESS) return gameDurationSeconds;
        long elapsed = (System.currentTimeMillis() - startTime) / 1000;
        return Math.max(0, gameDurationSeconds - elapsed);
    }
    
    public boolean isGameExpired() {
        return status == GameStatus.IN_PROGRESS && 
               System.currentTimeMillis() >= endTime;
    }
    
    public boolean canAcceptPlayers() {
        return status == GameStatus.WAITING && players.size() < 2;
    }
    
    // Getters and setters
    public String getGameId() { return gameId; }
    public void setGameId(String gameId) { this.gameId = gameId; }
    
    public CrosswordGrid getGrid() { return grid; }
    public void setGrid(CrosswordGrid grid) { this.grid = grid; }
    
    public Map<String, Player> getPlayers() { return players; }
    public void setPlayers(Map<String, Player> players) { this.players = players; }
    
    public GameStatus getStatus() { return status; }
    public void setStatus(GameStatus status) { this.status = status; }
    
    public long getStartTime() { return startTime; }
    public void setStartTime(long startTime) { this.startTime = startTime; }
    
    public long getEndTime() { return endTime; }
    public void setEndTime(long endTime) { this.endTime = endTime; }
    
    public int getGameDurationSeconds() { return gameDurationSeconds; }
    public void setGameDurationSeconds(int gameDurationSeconds) { this.gameDurationSeconds = gameDurationSeconds; }
    
    public String getWinnerId() { return winnerId; }
    public void setWinnerId(String winnerId) { this.winnerId = winnerId; }
}