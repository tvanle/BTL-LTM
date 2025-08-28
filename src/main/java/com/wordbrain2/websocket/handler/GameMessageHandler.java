package com.wordbrain2.websocket.handler;

import com.wordbrain2.service.core.GameEngine;
import com.wordbrain2.service.core.RoomService;
import com.wordbrain2.websocket.message.BaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

@Slf4j
@Component  
public class GameMessageHandler {
    
    @Autowired
    private GameEngine gameEngine;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private RoomMessageHandler roomMessageHandler;
    
    public Map<String, Object> handleStartGame(WebSocketSession session, BaseMessage message) {
        String playerId = roomMessageHandler.getPlayerIdForSession(session.getId());
        String roomCode = roomMessageHandler.getRoomForPlayer(playerId);
        
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
        boolean allReady = room.getPlayerReady().keySet().stream()
            .allMatch(pid -> Boolean.TRUE.equals(room.getPlayerReady().get(pid)));
        if (!allReady) {
            return createErrorResult("Tất cả người chơi phải sẵn sàng để bắt đầu.");
        }
        
        var gameState = gameEngine.startGame(roomCode);
        
        if (gameState != null) {
            // Schedule level start after countdown
            scheduleGameStart(roomCode);
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
        String playerId = roomMessageHandler.getPlayerIdForSession(session.getId());
        String roomCode = roomMessageHandler.getRoomForPlayer(playerId);
        
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
        String playerId = roomMessageHandler.getPlayerIdForSession(session.getId());
        String roomCode = roomMessageHandler.getRoomForPlayer(playerId);
        
        if (playerId == null || roomCode == null) {
            return createErrorResult("Bạn chưa tham gia phòng.");
        }
        
        // Get hint from game engine
        Map<?, ?> data = (Map<?, ?>) message.getData();
        int hintLevel = getInt(data, "hintLevel", 1);
        
        // For now, just return a basic hint
        // In a real implementation, this would check the current game state
        String hint = generateHint(null, hintLevel);
        
        return Map.of(
            "success", true,
            "hint", hint,
            "hintLevel", hintLevel
        );
    }
    
    private void scheduleGameStart(String roomCode) {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                var levelData = gameEngine.startLevel(roomCode, 1);
                // Level data will be broadcast by GameWebSocketHandler
            } catch (InterruptedException e) {
                log.error("Game start interrupted", e);
            } catch (Exception ex) {
                log.error("Failed to start level", ex);
            }
        }).start();
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