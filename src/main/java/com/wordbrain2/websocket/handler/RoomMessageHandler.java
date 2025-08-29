package com.wordbrain2.websocket.handler;

import com.wordbrain2.controller.websocket.ConnectionManager;
import com.wordbrain2.model.enums.MessageType;
import com.wordbrain2.service.core.RoomService;
import com.wordbrain2.websocket.message.BaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

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
    
    public Map<String, Object> handleCreateRoom(WebSocketSession session, BaseMessage message) {
        Map<?, ?> data = (Map<?, ?>) message.getData();
        String playerName = getString(data, "playerName");
        String topic = getString(data, "topic");
        
        var result = roomService.createRoom(playerName, topic, session.getId());
        
        if (result != null) {
            String roomCode = (String) result.get("roomCode");
            String playerId = (String) result.get("playerId");
            
            connectionManager.registerPlayer(session.getId(), playerId);
            connectionManager.addPlayerToRoom(playerId, roomCode);
        }
        
        return result;
    }
    
    public Map<String, Object> handleJoinRoom(WebSocketSession session, BaseMessage message) {
        Map<?, ?> data = (Map<?, ?>) message.getData();
        String roomCode = getString(data, "roomCode");
        String playerName = getString(data, "playerName");
        
        var result = roomService.joinRoom(roomCode, playerName, session.getId());
        
        if (result != null) {
            String playerId = (String) result.get("playerId");
            
            connectionManager.registerPlayer(session.getId(), playerId);
            connectionManager.addPlayerToRoom(playerId, roomCode);
        }
        
        return result;
    }
    
    public Map<String, Object> handleLeaveRoom(WebSocketSession session, BaseMessage message) {
        String playerId = resolvePlayerId(session, message);
        String roomCode = resolveRoomCode(playerId, message);
        
        if (playerId == null || roomCode == null) {
            return createErrorResult("Invalid player or room");
        }
        
        roomService.removePlayer(roomCode, playerId);
        connectionManager.removePlayerFromRoom(playerId, roomCode);
        
        return Map.of(
            "success", true,
            "playerId", playerId,
            "roomCode", roomCode
        );
    }
    
    public Map<String, Object> handlePlayerReady(WebSocketSession session, BaseMessage message) {
        String playerId = resolvePlayerId(session, message);
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
    
    public void cleanupSession(String sessionId) {
        connectionManager.removeSession(sessionId);
    }
    
    public String getPlayerIdForSession(String sessionId) {
        return connectionManager.getPlayerId(sessionId);
    }
    
    public String getRoomForPlayer(String playerId) {
        return connectionManager.getPlayerRoom(playerId);
    }
    
    public void registerPlayerSession(String sessionId, String playerId, String roomCode) {
        connectionManager.registerPlayer(sessionId, playerId);
        connectionManager.addPlayerToRoom(playerId, roomCode);
    }
    
    private String resolvePlayerId(WebSocketSession session, BaseMessage message) {
        String playerId = connectionManager.getPlayerId(session.getId());
        if (playerId == null && message.getData() != null) {
            Map<?, ?> data = (Map<?, ?>) message.getData();
            playerId = getString(data, "playerId");
        }
        return playerId;
    }
    
    private String resolveRoomCode(String playerId, BaseMessage message) {
        String roomCode = null;
        if (playerId != null) {
            roomCode = connectionManager.getPlayerRoom(playerId);
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