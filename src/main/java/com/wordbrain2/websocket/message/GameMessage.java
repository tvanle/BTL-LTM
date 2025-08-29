package com.wordbrain2.websocket.message;

import com.wordbrain2.model.enums.GamePhase;
import com.wordbrain2.model.game.Grid;
import java.util.List;
import java.util.Map;

public class GameMessage extends BaseMessage {
    
    private GamePhase gamePhase;
    private int currentLevel;
    private long timeRemaining;
    private Grid grid;
    
    public GameMessage() {
        super();
    }
    
    public GameMessage(String type, String roomCode) {
        super();
        setType(type);
        setRoomCode(roomCode);
    }
    
    // Static factory methods for common game messages
    public static GameMessage levelStart(String roomCode, int level, Grid grid, long duration) {
        GameMessage message = new GameMessage("LEVEL_START", roomCode);
        message.gamePhase = GamePhase.PLAYING;
        message.currentLevel = level;
        message.timeRemaining = duration;
        message.grid = grid;
        message.setData(Map.of(
            "level", level,
            "grid", grid,
            "duration", duration,
            "serverTime", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static GameMessage levelEnd(String roomCode, int level, Map<String, Object> results) {
        GameMessage message = new GameMessage("LEVEL_END", roomCode);
        message.gamePhase = GamePhase.LEVEL_END;
        message.currentLevel = level;
        message.setData(Map.of(
            "level", level,
            "results", results,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static GameMessage gameEnd(String roomCode, Map<String, Object> finalResults) {
        GameMessage message = new GameMessage("GAME_END", roomCode);
        message.gamePhase = GamePhase.FINISHED;
        message.setData(Map.of(
            "finalResults", finalResults,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static GameMessage wordSubmitted(String roomCode, String playerId, String word, boolean accepted, int points) {
        GameMessage message = new GameMessage(accepted ? "WORD_ACCEPTED" : "WORD_REJECTED", roomCode);
        message.setPlayerId(playerId);
        message.setData(Map.of(
            "word", word,
            "accepted", accepted,
            "points", points,
            "playerId", playerId,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static GameMessage timerUpdate(String roomCode, long timeRemaining) {
        GameMessage message = new GameMessage("TIMER_UPDATE", roomCode);
        message.timeRemaining = timeRemaining;
        message.setData(Map.of(
            "timeRemaining", timeRemaining,
            "serverTime", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static GameMessage opponentScored(String roomCode, String scoringPlayerId, String word, int points) {
        GameMessage message = new GameMessage("OPPONENT_SCORED", roomCode);
        message.setPlayerId(scoringPlayerId);
        message.setData(Map.of(
            "scoringPlayer", scoringPlayerId,
            "word", word,
            "points", points,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static GameMessage hintRequested(String roomCode, String playerId, String hint) {
        GameMessage message = new GameMessage("HINT_PROVIDED", roomCode);
        message.setPlayerId(playerId);
        message.setData(Map.of(
            "hint", hint,
            "playerId", playerId
        ));
        return message;
    }
    
    public static GameMessage gamePaused(String roomCode, String reason) {
        GameMessage message = new GameMessage("GAME_PAUSED", roomCode);
        message.setData(Map.of(
            "reason", reason,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static GameMessage gameResumed(String roomCode) {
        GameMessage message = new GameMessage("GAME_RESUMED", roomCode);
        message.setData(Map.of(
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static GameMessage playerDisconnected(String roomCode, String playerId) {
        GameMessage message = new GameMessage("PLAYER_DISCONNECTED", roomCode);
        message.setPlayerId(playerId);
        message.setData(Map.of(
            "playerId", playerId,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static GameMessage playerReconnected(String roomCode, String playerId) {
        GameMessage message = new GameMessage("PLAYER_RECONNECTED", roomCode);
        message.setPlayerId(playerId);
        message.setData(Map.of(
            "playerId", playerId,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    // Getters and Setters
    public GamePhase getGamePhase() { return gamePhase; }
    public void setGamePhase(GamePhase gamePhase) { this.gamePhase = gamePhase; }
    
    public int getCurrentLevel() { return currentLevel; }
    public void setCurrentLevel(int currentLevel) { this.currentLevel = currentLevel; }
    
    public long getTimeRemaining() { return timeRemaining; }
    public void setTimeRemaining(long timeRemaining) { this.timeRemaining = timeRemaining; }
    
    public Grid getGrid() { return grid; }
    public void setGrid(Grid grid) { this.grid = grid; }
    
    @Override
    public String toString() {
        return "GameMessage{" +
               "type='" + getType() + '\'' +
               ", roomCode='" + getRoomCode() + '\'' +
               ", gamePhase=" + gamePhase +
               ", currentLevel=" + currentLevel +
               ", timeRemaining=" + timeRemaining +
               '}';
    }
}