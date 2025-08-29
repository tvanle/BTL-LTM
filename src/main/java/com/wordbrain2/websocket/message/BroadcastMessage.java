package com.wordbrain2.websocket.message;

import com.wordbrain2.model.scoring.Leaderboard;
import java.util.List;
import java.util.Map;

public class BroadcastMessage extends BaseMessage {
    
    private String broadcastType;
    private List<String> targetPlayerIds;
    private boolean isGlobal;
    
    public BroadcastMessage() {
        super();
    }
    
    public BroadcastMessage(String type, String roomCode) {
        super();
        setType(type);
        setRoomCode(roomCode);
    }
    
    // Static factory methods for common broadcast messages
    public static BroadcastMessage leaderboardUpdate(String roomCode, Leaderboard leaderboard) {
        BroadcastMessage message = new BroadcastMessage("LEADERBOARD_UPDATE", roomCode);
        message.broadcastType = "leaderboard";
        message.isGlobal = true;
        message.setData(Map.of(
            "leaderboard", leaderboard,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static BroadcastMessage systemAnnouncement(String message, boolean isGlobal) {
        BroadcastMessage broadcast = new BroadcastMessage("SYSTEM_ANNOUNCEMENT", null);
        broadcast.broadcastType = "system";
        broadcast.isGlobal = isGlobal;
        broadcast.setData(Map.of(
            "message", message,
            "timestamp", System.currentTimeMillis(),
            "priority", "normal"
        ));
        return broadcast;
    }
    
    public static BroadcastMessage gameStateUpdate(String roomCode, Map<String, Object> gameState) {
        BroadcastMessage message = new BroadcastMessage("GAME_STATE_UPDATE", roomCode);
        message.broadcastType = "gamestate";
        message.isGlobal = true;
        message.setData(Map.of(
            "gameState", gameState,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static BroadcastMessage playerScoreUpdate(String roomCode, String playerId, int score, int rank) {
        BroadcastMessage message = new BroadcastMessage("PLAYER_SCORE_UPDATE", roomCode);
        message.setPlayerId(playerId);
        message.broadcastType = "score";
        message.isGlobal = true;
        message.setData(Map.of(
            "playerId", playerId,
            "score", score,
            "rank", rank,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static BroadcastMessage levelComplete(String roomCode, Map<String, Object> results) {
        BroadcastMessage message = new BroadcastMessage("LEVEL_COMPLETE", roomCode);
        message.broadcastType = "level";
        message.isGlobal = true;
        message.setData(Map.of(
            "results", results,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static BroadcastMessage achievementUnlocked(String roomCode, String playerId, String achievementId) {
        BroadcastMessage message = new BroadcastMessage("ACHIEVEMENT_UNLOCKED", roomCode);
        message.setPlayerId(playerId);
        message.broadcastType = "achievement";
        message.isGlobal = true;
        message.setData(Map.of(
            "playerId", playerId,
            "achievementId", achievementId,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static BroadcastMessage roomEvent(String roomCode, String eventType, Map<String, Object> eventData) {
        BroadcastMessage message = new BroadcastMessage("ROOM_EVENT", roomCode);
        message.broadcastType = "room_event";
        message.isGlobal = true;
        message.setData(Map.of(
            "eventType", eventType,
            "eventData", eventData,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static BroadcastMessage emergencyBroadcast(String message, String roomCode) {
        BroadcastMessage broadcast = new BroadcastMessage("EMERGENCY_BROADCAST", roomCode);
        broadcast.broadcastType = "emergency";
        broadcast.isGlobal = roomCode == null;
        broadcast.setData(Map.of(
            "message", message,
            "priority", "high",
            "timestamp", System.currentTimeMillis()
        ));
        return broadcast;
    }
    
    public static BroadcastMessage targetedMessage(String roomCode, List<String> targetPlayerIds, String messageType, Map<String, Object> data) {
        BroadcastMessage message = new BroadcastMessage(messageType, roomCode);
        message.broadcastType = "targeted";
        message.targetPlayerIds = targetPlayerIds;
        message.isGlobal = false;
        message.setData(data);
        return message;
    }
    
    public static BroadcastMessage serverStatus(String status, String message) {
        BroadcastMessage broadcast = new BroadcastMessage("SERVER_STATUS", null);
        broadcast.broadcastType = "server";
        broadcast.isGlobal = true;
        broadcast.setData(Map.of(
            "status", status,
            "message", message,
            "timestamp", System.currentTimeMillis()
        ));
        return broadcast;
    }
    
    public static BroadcastMessage tournamentUpdate(String tournamentId, Map<String, Object> updateData) {
        BroadcastMessage message = new BroadcastMessage("TOURNAMENT_UPDATE", null);
        message.broadcastType = "tournament";
        message.isGlobal = true;
        message.setData(Map.of(
            "tournamentId", tournamentId,
            "updateData", updateData,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    // Helper methods
    public boolean shouldBroadcastToPlayer(String playerId) {
        if (isGlobal) {
            return true;
        }
        return targetPlayerIds != null && targetPlayerIds.contains(playerId);
    }
    
    public void addTargetPlayer(String playerId) {
        if (targetPlayerIds == null) {
            targetPlayerIds = List.of(playerId);
        } else {
            targetPlayerIds.add(playerId);
        }
    }
    
    // Getters and Setters
    public String getBroadcastType() { return broadcastType; }
    public void setBroadcastType(String broadcastType) { this.broadcastType = broadcastType; }
    
    public List<String> getTargetPlayerIds() { return targetPlayerIds; }
    public void setTargetPlayerIds(List<String> targetPlayerIds) { this.targetPlayerIds = targetPlayerIds; }
    
    public boolean isGlobal() { return isGlobal; }
    public void setGlobal(boolean global) { this.isGlobal = global; }
    
    @Override
    public String toString() {
        return "BroadcastMessage{" +
               "type='" + getType() + '\'' +
               ", roomCode='" + getRoomCode() + '\'' +
               ", broadcastType='" + broadcastType + '\'' +
               ", isGlobal=" + isGlobal +
               ", targetCount=" + (targetPlayerIds != null ? targetPlayerIds.size() : 0) +
               '}';
    }
}