package com.wordbrain2.websocket.handler;

import com.wordbrain2.controller.websocket.ConnectionManager;
import com.wordbrain2.service.core.GameEngine;
import com.wordbrain2.service.core.RoomService;
import com.wordbrain2.websocket.message.BaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class BoosterMessageHandler {
    
    private final GameEngine gameEngine;
    private final ConnectionManager connectionManager;
    private final RoomService roomService;
    
    public BoosterMessageHandler(GameEngine gameEngine, ConnectionManager connectionManager, RoomService roomService) {
        this.gameEngine = gameEngine;
        this.connectionManager = connectionManager;
        this.roomService = roomService;
    }
    
    public Map<String, Object> handleUseBooster(String sessionId, BaseMessage message) {
        String playerId = connectionManager.getPlayerId(sessionId);
        String roomCode = roomService.getPlayerRoom(playerId);
        
        if (playerId == null || roomCode == null) {
            return createErrorResult("Bạn chưa tham gia phòng.");
        }
        
        var result = gameEngine.useBooster(roomCode, playerId, message.getData());
        
        if (result != null) {
            // Check if this booster affects other players
            Map<?, ?> data = (Map<?, ?>) message.getData();
            String boosterType = getString(data, "boosterType");
            
            if ("FREEZE".equals(boosterType)) {
                // Add freeze effect info for broadcasting
                result.put("affectsOthers", true);
                result.put("effectType", "FREEZE");
                result.put("effectDuration", 3000);
                result.put("fromPlayer", playerId);
            }
            
            return result;
        }
        
        return createErrorResult("Không thể sử dụng booster.");
    }
    
    public Map<String, Object> getBoosterStatus(String roomCode, String playerId) {
        // Get player's available boosters
        // In a real implementation, this would check the actual game state
        return Map.of(
            "success", true,
            "boosters", Map.of(
                "DOUBLE_UP", Map.of("available", 1, "cooldown", 0),
                "FREEZE", Map.of("available", 1, "cooldown", 0),
                "REVEAL", Map.of("available", 2, "cooldown", 0),
                "TIME_PLUS", Map.of("available", 2, "cooldown", 0),
                "SHIELD", Map.of("available", 1, "cooldown", 0),
                "STREAK_SAVE", Map.of("available", 1, "cooldown", 0),
                "SKIP_HALF", Map.of("available", 1, "cooldown", 0)
            )
        );
    }
    
    private String getString(Map<?, ?> map, String key) {
        if (map == null) return null;
        Object val = map.get(key);
        return val != null ? String.valueOf(val) : null;
    }
    
    private Map<String, Object> createErrorResult(String error) {
        return Map.of(
            "success", false,
            "error", error
        );
    }
}