package com.wordbrain2.websocket.message;

import com.wordbrain2.model.enums.RoomStatus;
import java.util.Map;

public class RoomMessage extends BaseMessage {
    
    private RoomStatus roomStatus;
    private int playerCount;
    private int maxPlayers;
    private String hostId;
    
    public RoomMessage() {
        super();
    }
    
    public RoomMessage(String type, String roomCode) {
        super();
        setType(type);
        setRoomCode(roomCode);
    }
    
    // Static factory methods for common room messages
    public static RoomMessage roomCreated(String roomCode, String hostId, int maxPlayers) {
        RoomMessage message = new RoomMessage("ROOM_CREATED", roomCode);
        message.hostId = hostId;
        message.maxPlayers = maxPlayers;
        message.playerCount = 1;
        message.roomStatus = RoomStatus.WAITING;
        message.setData(Map.of(
            "hostId", hostId,
            "maxPlayers", maxPlayers,
            "status", "WAITING"
        ));
        return message;
    }
    
    public static RoomMessage playerJoined(String roomCode, String playerId, int currentPlayerCount) {
        RoomMessage message = new RoomMessage("PLAYER_JOINED", roomCode);
        message.setPlayerId(playerId);
        message.playerCount = currentPlayerCount;
        message.setData(Map.of(
            "playerId", playerId,
            "playerCount", currentPlayerCount
        ));
        return message;
    }
    
    public static RoomMessage playerLeft(String roomCode, String playerId, int currentPlayerCount) {
        RoomMessage message = new RoomMessage("PLAYER_LEFT", roomCode);
        message.setPlayerId(playerId);
        message.playerCount = currentPlayerCount;
        message.setData(Map.of(
            "playerId", playerId,
            "playerCount", currentPlayerCount
        ));
        return message;
    }
    
    public static RoomMessage playerReady(String roomCode, String playerId, boolean isReady) {
        RoomMessage message = new RoomMessage("PLAYER_READY", roomCode);
        message.setPlayerId(playerId);
        message.setData(Map.of(
            "playerId", playerId,
            "isReady", isReady
        ));
        return message;
    }
    
    public static RoomMessage statusChanged(String roomCode, RoomStatus newStatus) {
        RoomMessage message = new RoomMessage("ROOM_STATUS_CHANGED", roomCode);
        message.roomStatus = newStatus;
        message.setData(Map.of(
            "status", newStatus.name(),
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static RoomMessage gameStarting(String roomCode, int countdown) {
        RoomMessage message = new RoomMessage("GAME_STARTING", roomCode);
        message.roomStatus = RoomStatus.IN_GAME;
        message.setData(Map.of(
            "countdown", countdown,
            "status", "IN_GAME"
        ));
        return message;
    }
    
    public static RoomMessage roomClosed(String roomCode, String reason) {
        RoomMessage message = new RoomMessage("ROOM_CLOSED", roomCode);
        message.roomStatus = RoomStatus.CLOSED;
        message.setData(Map.of(
            "reason", reason,
            "status", "CLOSED"
        ));
        return message;
    }
    
    public static RoomMessage error(String roomCode, String errorMessage) {
        RoomMessage message = new RoomMessage("ROOM_ERROR", roomCode);
        message.setData(Map.of(
            "error", errorMessage,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    // Getters and Setters
    public RoomStatus getRoomStatus() { return roomStatus; }
    public void setRoomStatus(RoomStatus roomStatus) { this.roomStatus = roomStatus; }
    
    public int getPlayerCount() { return playerCount; }
    public void setPlayerCount(int playerCount) { this.playerCount = playerCount; }
    
    public int getMaxPlayers() { return maxPlayers; }
    public void setMaxPlayers(int maxPlayers) { this.maxPlayers = maxPlayers; }
    
    public String getHostId() { return hostId; }
    public void setHostId(String hostId) { this.hostId = hostId; }
    
    @Override
    public String toString() {
        return "RoomMessage{" +
               "type='" + getType() + '\'' +
               ", roomCode='" + getRoomCode() + '\'' +
               ", roomStatus=" + roomStatus +
               ", playerCount=" + playerCount +
               ", maxPlayers=" + maxPlayers +
               ", hostId='" + hostId + '\'' +
               '}';
    }
}