package com.wordbrain2.websocket.handler;

import com.wordbrain2.controller.websocket.ConnectionManager;
import com.wordbrain2.service.core.GameEngine;
import com.wordbrain2.service.core.RoomService;
import com.wordbrain2.websocket.message.BaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

@Slf4j
@Component  
public class GameMessageHandler {
    
    private final GameEngine gameEngine;
    private final RoomService roomService;
    private final ConnectionManager connectionManager;
    
    public GameMessageHandler(GameEngine gameEngine, RoomService roomService, ConnectionManager connectionManager) {
        this.gameEngine = gameEngine;
        this.roomService = roomService;
        this.connectionManager = connectionManager;
    }
    
    public Map<String, Object> handleStartGame(WebSocketSession session, BaseMessage message) {
        String playerId = connectionManager.getPlayerId(session.getId());
        String roomCode = roomService.getPlayerRoom(playerId);
        
        if (playerId == null || roomCode == null) {
            return createErrorResult("Bạn chưa tham gia phòng.");
        }
        
        // Enforce host-only start
        var room = roomService.getRoom(roomCode);
        if (room == null) {
            return createErrorResult("Phòng không tồn tại.");
        }
        
        if (!playerId.equals(room.getHostId())) {
            return createErrorResult("Chỉ chủ phòng mới có thể bắt đầu trò chơi.");
        }
        
        // Enforce all players ready before starting
        if (!roomService.areAllPlayersReady(roomCode)) {
            return createErrorResult("Tất cả người chơi phải sẵn sàng để bắt đầu.");
        }

        var gameState = gameEngine.startGame(roomCode);
        
        if (gameState != null) {
            // MessageRouter will handle scheduling
            return Map.of(
                "success", true,
                "gameState", gameState,
                "countdown", 5,
                "message", "Game starting in 5 seconds..."
            );
        }
        
        return createErrorResult("Không thể bắt đầu trò chơi.");
    }
    
    public Map<String, Object> handleSubmitWord(WebSocketSession session, BaseMessage message) {
        String playerId = connectionManager.getPlayerId(session.getId());
        String roomCode = roomService.getPlayerRoom(playerId);
        
        if (playerId == null || roomCode == null) {
            return createErrorResult("Bạn chưa tham gia phòng.");
        }
        
        var result = gameEngine.submitWord(roomCode, playerId, message.getData());
        
        if (result != null) {
            return result;
        }
        
        return createErrorResult("Không thể xử lý từ.");
    }
    
    public Map<String, Object> handleRequestHint(WebSocketSession session, BaseMessage message) {
        String playerId = connectionManager.getPlayerId(session.getId());
        String roomCode = roomService.getPlayerRoom(playerId);
        
        if (playerId == null || roomCode == null) {
            return createErrorResult("Bạn chưa tham gia phòng.");
        }
        
        // Get hint from game engine
        Map<?, ?> data = (Map<?, ?>) message.getData();
        int hintLevel = getInt(data, "hintLevel", 1);

        String hint = generateHint(null, hintLevel);
        
        return Map.of(
            "success", true,
            "hint", hint,
            "hintLevel", hintLevel
        );
    }
    
    private String generateHint(Map<String, Object> gameState, int level) {
        // Simple hint generation - in real implementation would be more sophisticated
        switch(level) {
            case 1:
                return "Look for common 3-letter words";
            case 2:
                return "Try words starting with vowels";
            case 3:
                return "Check the corners of the grid";
            default:
                return "Keep looking!";
        }
    }
    
    private int getInt(Map<?, ?> map, String key, int defaultValue) {
        if (map == null) return defaultValue;
        Object val = map.get(key);
        if (val instanceof Number) return ((Number) val).intValue();
        if (val != null) {
            try {
                return Integer.parseInt(String.valueOf(val));
            } catch (NumberFormatException e) {
                return defaultValue;
            }
        }
        return defaultValue;
    }
    
    private Map<String, Object> createErrorResult(String error) {
        return Map.of(
            "success", false,
            "error", error
        );
    }
}