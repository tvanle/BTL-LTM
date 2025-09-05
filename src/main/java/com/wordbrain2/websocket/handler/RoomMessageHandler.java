package com.wordbrain2.websocket.handler;

import com.wordbrain2.controller.websocket.ConnectionManager;
import com.wordbrain2.model.enums.MessageType;
import com.wordbrain2.service.core.RoomService;
import com.wordbrain2.websocket.message.BaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class RoomMessageHandler {
    
    private final RoomService roomService;
    private final ConnectionManager connectionManager;
    
    public RoomMessageHandler(RoomService roomService, ConnectionManager connectionManager) {
        this.roomService = roomService;
        this.connectionManager = connectionManager;
    }
    
    public Map<String, Object> handleCreateRoom(String sessionId, BaseMessage message) {
        Map<?, ?> data = (Map<?, ?>) message.getData();
        String playerName = getString(data, "playerName");
        String topic = getString(data, "topic");
        
        var result = roomService.createRoom(playerName, topic, sessionId);
        
        if (result != null) {
            String playerId = (String) result.get("playerId");
            
            // Only register session-player mapping
            connectionManager.registerPlayer(sessionId, playerId);
            // Room-player relationship is already handled by RoomService
        }
        
        return result;
    }
    
    public Map<String, Object> handleJoinRoom(String sessionId, BaseMessage message) {
        Map<?, ?> data = (Map<?, ?>) message.getData();
        String roomCode = getString(data, "roomCode");
        String playerName = getString(data, "playerName");
        
        var result = roomService.joinRoom(roomCode, playerName, sessionId);
        
        if (result != null) {
            String playerId = (String) result.get("playerId");
            
            // Only register session-player mapping
            connectionManager.registerPlayer(sessionId, playerId);
            // Room-player relationship is already handled by RoomService
        }
        
        return result;
    }
    
    public Map<String, Object> handleLeaveRoom(String sessionId, BaseMessage message) {
        String playerId = resolvePlayerId(sessionId, message);
        String roomCode = resolveRoomCode(playerId, message);
        
        if (playerId == null || roomCode == null) {
            return createErrorResult("Invalid player or room");
        }
        
        // Remove player from room
        roomService.removePlayer(roomCode, playerId);
        
        // IMPORTANT: Clear the session-player mapping so they can join again with new ID
        connectionManager.unregisterPlayer(sessionId);
        
        return Map.of(
            "success", true,
            "playerId", playerId,
            "roomCode", roomCode
        );
    }
    
    public Map<String, Object> handlePlayerReady(String sessionId, BaseMessage message) {
        String playerId = resolvePlayerId(sessionId, message);
        String roomCode = resolveRoomCode(playerId, message);
        
        if (playerId == null || roomCode == null) {
            return createErrorResult("Invalid player or room");
        }
        
        Map<?, ?> data = (Map<?, ?>) message.getData();
        boolean ready = getBoolean(data, "ready");
        
        roomService.setPlayerReady(roomCode, playerId, ready);
        
        return Map.of(
            "success", true,
            "playerId", playerId,
            "ready", ready
        );
    }
    
    public String getPlayerIdForSession(String sessionId) {
        return connectionManager.getPlayerId(sessionId);
    }
    
    public String getRoomForPlayer(String playerId) {
        // Use RoomService to find player's room
        return roomService.getPlayerRoom(playerId);
    }
    
    public void registerPlayerSession(String sessionId, String playerId, String roomCode) {
        // Only register session-player mapping
        connectionManager.registerPlayer(sessionId, playerId);
        // Room-player relationship should be handled by RoomService
    }
    
    private String resolvePlayerId(String sessionId, BaseMessage message) {
        String playerId = connectionManager.getPlayerId(sessionId);
        if (playerId == null && message.getData() != null) {
            Map<?, ?> data = (Map<?, ?>) message.getData();
            playerId = getString(data, "playerId");
        }
        return playerId;
    }
    
    private String resolveRoomCode(String playerId, BaseMessage message) {
        String roomCode = null;
        if (playerId != null) {
            roomCode = roomService.getPlayerRoom(playerId);
        }
        if (roomCode == null && message.getData() != null) {
            Map<?, ?> data = (Map<?, ?>) message.getData();
            roomCode = getString(data, "roomCode");
        }
        return roomCode;
    }
    
    private String getString(Map<?, ?> map, String key) {
        if (map == null) return null;
        Object val = map.get(key);
        return val != null ? String.valueOf(val) : null;
    }
    
    private boolean getBoolean(Map<?, ?> map, String key) {
        if (map == null) return false;
        Object val = map.get(key);
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).intValue() != 0;
        if (val != null) return Boolean.parseBoolean(String.valueOf(val));
        return false;
    }
    
    private Map<String, Object> createErrorResult(String error) {
        return Map.of(
            "success", false,
            "error", error
        );
    }
}