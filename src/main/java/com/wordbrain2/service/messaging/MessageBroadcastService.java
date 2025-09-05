package com.wordbrain2.service.messaging;

import com.google.gson.Gson;
import com.wordbrain2.controller.websocket.ConnectionManager;
import com.wordbrain2.model.entity.Player;
import com.wordbrain2.model.entity.Room;
import com.wordbrain2.model.enums.MessageType;
import com.wordbrain2.service.core.RoomService;
import com.wordbrain2.websocket.message.BaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Service for broadcasting messages to rooms and players.
 * Centralizes all broadcast logic to avoid code duplication.
 */
@Slf4j
@Service
public class MessageBroadcastService {
    
    private final RoomService roomService;
    private final ConnectionManager connectionManager;
    private final Gson gson = new Gson();
    
    public MessageBroadcastService(RoomService roomService, ConnectionManager connectionManager) {
        this.roomService = roomService;
        this.connectionManager = connectionManager;
    }
    
    /**
     * Broadcast a message to all players in a room
     */
    public void broadcastToRoom(String roomCode, MessageType type, Object data) {
        broadcastToRoom(roomCode, type, data, null);
    }
    
    /**
     * Broadcast a message to all players in a room except one
     */
    public void broadcastToRoom(String roomCode, MessageType type, Object data, String excludeSessionId) {
        Room room = roomService.getRoom(roomCode);
        if (room == null) {
            log.warn("Cannot broadcast to room {} - room not found", roomCode);
            return;
        }
        
        BaseMessage message = new BaseMessage(type, convertToMap(data));
        broadcastToRoom(roomCode, message, excludeSessionId);
    }
    
    /**
     * Broadcast a BaseMessage to all players in a room
     */
    public void broadcastToRoom(String roomCode, BaseMessage message) {
        broadcastToRoom(roomCode, message, null);
    }
    
    /**
     * Broadcast a BaseMessage to all players in a room except one
     */
    public void broadcastToRoom(String roomCode, BaseMessage message, String excludeSessionId) {
        Room room = roomService.getRoom(roomCode);
        if (room == null) {
            log.warn("Cannot broadcast to room {} - room not found", roomCode);
            return;
        }
        
        int sentCount = 0;
        for (Player player : room.getPlayers()) {
            String sessionId = player.getSessionId();
            if (sessionId == null) continue;
            if (excludeSessionId != null && excludeSessionId.equals(sessionId)) continue;
            
            connectionManager.sendMessage(sessionId, message);
            sentCount++;
        }
        
        log.debug("Broadcast message {} to room {} ({} players)", message.getType(), roomCode, sentCount);
    }
    
    /**
     * Send a message to a specific session
     */
    public void sendMessageToSession(String sessionId, MessageType type, Object data) {
        BaseMessage message = new BaseMessage(type, convertToMap(data));
        connectionManager.sendMessage(sessionId, message);
    }
    
    /**
     * Send a message to a specific player
     */
    public void sendMessageToPlayer(String playerId, MessageType type, Object data) {
        BaseMessage message = new BaseMessage(type, convertToMap(data));
        sendMessageToPlayer(playerId, message);
    }
    
    /**
     * Send a BaseMessage to a specific player
     */
    public void sendMessageToPlayer(String playerId, BaseMessage message) {
        connectionManager.sendMessageToPlayer(playerId, message);
    }
    
    /**
     * Broadcast room state to all players in the room
     */
    public void broadcastRoomState(String roomCode) {
        Room room = roomService.getRoom(roomCode);
        if (room == null) return;
        
        // Build room state with player list
        Map<String, Object> state = new HashMap<>();
        
        List<Map<String, Object>> players = new ArrayList<>();
        room.getPlayers().forEach(p -> {
            boolean ready = Boolean.TRUE.equals(room.getPlayerReady().get(p.getId()));
            Map<String, Object> pInfo = new HashMap<>();
            pInfo.put("id", p.getId());
            pInfo.put("name", p.getName());
            pInfo.put("ready", ready);
            pInfo.put("isHost", p.getId().equals(room.getHostId()));
            players.add(pInfo);
        });
        
        state.put("roomCode", room.getRoomCode());
        state.put("hostId", room.getHostId());
        state.put("players", players);
        state.put("playersCount", room.getPlayerCount());
        state.put("maxPlayers", room.getMaxPlayers());
        
        broadcastToRoom(roomCode, MessageType.ROOM_STATE, state);
    }
    
    /**
     * Send error message to a session
     */
    public void sendErrorToSession(String sessionId, String error) {
        sendMessageToSession(sessionId, MessageType.ERROR, Map.of("error", error));
    }
    
    /**
     * Send invalid action message to a session
     */
    public void sendInvalidActionToSession(String sessionId, String reason) {
        sendMessageToSession(sessionId, MessageType.INVALID_ACTION, Map.of("reason", reason));
    }
    
    private Map<String, Object> convertToMap(Object data) {
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        } else {
            // Wrap non-map objects
            return Map.of("data", data);
        }
    }
}