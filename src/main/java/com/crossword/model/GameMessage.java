package com.crossword.model;

public class GameMessage {
    private MessageType type;
    private String playerId;
    private Object data;
    private long timestamp;
    
    public enum MessageType {
        // Client to Server
        JOIN_GAME,
        PLACE_CHAR,
        GAME_READY,
        
        // Server to Client
        GAME_STATE,
        CHAR_VALIDATED,
        CHAR_REJECTED,
        SCORE_UPDATE,
        GAME_STARTED,
        GAME_ENDED,
        PLAYER_JOINED,
        PLAYER_LEFT,
        ERROR,
        
        // Bidirectional
        PING,
        PONG,
        RECONNECT
    }
    
    public GameMessage() {
        this.timestamp = System.currentTimeMillis();
    }
    
    public GameMessage(MessageType type, String playerId, Object data) {
        this();
        this.type = type;
        this.playerId = playerId;
        this.data = data;
    }
    
    // Getters and setters
    public MessageType getType() { return type; }
    public void setType(MessageType type) { this.type = type; }
    
    public String getPlayerId() { return playerId; }
    public void setPlayerId(String playerId) { this.playerId = playerId; }
    
    public Object getData() { return data; }
    public void setData(Object data) { this.data = data; }
    
    public long getTimestamp() { return timestamp; }
    public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
}