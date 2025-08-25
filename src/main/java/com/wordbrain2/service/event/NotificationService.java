package com.wordbrain2.service.event;

import com.wordbrain2.model.enums.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

@Service  
public class NotificationService {
    @Autowired
    @Lazy
    private EventBusService eventBusService;
    
    public void notifyPlayerJoined(String roomCode, String playerId, String playerName) {
        Map<String, Object> data = new HashMap<>();
        data.put("playerId", playerId);
        data.put("playerName", playerName);
        data.put("message", playerName + " joined the room");
        data.put("type", "info");
        
        eventBusService.publishEvent(roomCode, "PLAYER_JOINED", data);
    }
    
    public void notifyPlayerLeft(String roomCode, String playerId, String playerName) {
        Map<String, Object> data = new HashMap<>();
        data.put("playerId", playerId);
        data.put("playerName", playerName);
        data.put("message", playerName + " left the room");
        data.put("type", "warning");
        
        eventBusService.publishEvent(roomCode, "PLAYER_LEFT", data);
    }
    
    public void notifyPlayerDisconnected(String roomCode, String playerId, String playerName) {
        Map<String, Object> data = new HashMap<>();
        data.put("playerId", playerId);
        data.put("playerName", playerName);
        data.put("message", playerName + " disconnected");
        data.put("type", "error");
        
        eventBusService.publishEvent(roomCode, MessageType.PLAYER_DISCONNECTED.name(), data);
    }
    
    public void notifyPlayerReconnected(String roomCode, String playerId, String playerName) {
        Map<String, Object> data = new HashMap<>();
        data.put("playerId", playerId);
        data.put("playerName", playerName);
        data.put("message", playerName + " reconnected");
        data.put("type", "success");
        
        eventBusService.publishEvent(roomCode, MessageType.PLAYER_RECONNECTED.name(), data);
    }
    
    public void notifyRoomCreated(String roomCode, String hostName) {
        Map<String, Object> data = new HashMap<>();
        data.put("roomCode", roomCode);
        data.put("hostName", hostName);
        data.put("message", "Room " + roomCode + " created by " + hostName);
        data.put("type", "success");
        
        eventBusService.publishEvent(roomCode, "ROOM_CREATED", data);
    }
    
    public void notifyGameStarting(String roomCode, int countdown) {
        Map<String, Object> data = new HashMap<>();
        data.put("countdown", countdown);
        data.put("message", "Game starting in " + countdown + " seconds!");
        data.put("type", "info");
        
        eventBusService.publishEvent(roomCode, MessageType.GAME_STARTING.name(), data);
    }
    
    public void notifyLevelComplete(String roomCode, int level, String topPlayer) {
        Map<String, Object> data = new HashMap<>();
        data.put("level", level);
        data.put("topPlayer", topPlayer);
        data.put("message", "Level " + level + " complete! Top player: " + topPlayer);
        data.put("type", "success");
        
        eventBusService.publishEvent(roomCode, MessageType.LEVEL_END.name(), data);
    }
    
    public void notifyGameOver(String roomCode, String winner) {
        Map<String, Object> data = new HashMap<>();
        data.put("winner", winner);
        data.put("message", "Game Over! Winner: " + winner);
        data.put("type", "success");
        
        eventBusService.publishEvent(roomCode, MessageType.GAME_END.name(), data);
    }
    
    public void notifyAchievement(String roomCode, String playerId, String achievement) {
        Map<String, Object> data = new HashMap<>();
        data.put("playerId", playerId);
        data.put("achievement", achievement);
        data.put("message", "Achievement unlocked: " + achievement);
        data.put("type", "achievement");
        
        eventBusService.publishEvent(roomCode, "ACHIEVEMENT_UNLOCKED", data);
    }
    
    public void sendSystemMessage(String roomCode, String message, String type) {
        Map<String, Object> data = new HashMap<>();
        data.put("message", message);
        data.put("type", type);
        data.put("system", true);
        
        eventBusService.publishEvent(roomCode, "SYSTEM_MESSAGE", data);
    }
}