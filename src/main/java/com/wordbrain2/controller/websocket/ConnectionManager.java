package com.wordbrain2.controller.websocket;

import com.google.gson.Gson;
import com.wordbrain2.websocket.message.BaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * Manages WebSocket connections and session-player mappings only.
 * Room-player relationships are handled by RoomService.
 */
@Slf4j
@Component
public class ConnectionManager {
    
    @Autowired
    private GameWebSocketHandler gameWebSocketHandler;
    
    private final Map<String, Object> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToPlayer = new ConcurrentHashMap<>();
    private final Map<String, String> playerToSession = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();
    
    // Basic session management
    public void addSession(String sessionId, Object session) {
        sessions.put(sessionId, session);
        log.info("Session added: {}", sessionId);
    }
    
    public void addTcpSession(String sessionId, Object tcpHandler) {
        sessions.put(sessionId, tcpHandler);
        log.info("TCP Session added: {}", sessionId);
    }
    
    public void removeSession(String sessionId) {
        String playerId = sessionToPlayer.remove(sessionId);
        if (playerId != null) {
            playerToSession.remove(playerId);
        }
        sessions.remove(sessionId);
        log.info("Session removed: {}", sessionId);
    }
    
    public Object getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    public Object getSessionByPlayerId(String playerId) {
        if (playerId == null) {
            return null;
        }
        String sessionId = playerToSession.get(playerId);
        return sessionId != null ? sessions.get(sessionId) : null;
    }
    
    // Player management
    public void registerPlayer(String sessionId, String playerId) {
        sessionToPlayer.put(sessionId, playerId);
        playerToSession.put(playerId, sessionId);
        log.debug("Player {} registered for session {}", playerId, sessionId);
    }
    
    public String getPlayerId(String sessionId) {
        if (sessionId == null) {
            return null;
        }
        return sessionToPlayer.get(sessionId);
    }
    
    public void unregisterPlayer(String sessionId) {
        String playerId = sessionToPlayer.remove(sessionId);
        if (playerId != null) {
            playerToSession.remove(playerId);
            log.debug("Player {} unregistered from session {}", playerId, sessionId);
        }
    }
    
    // Room management removed - use RoomService instead
    
    // Message sending
    public void sendMessage(String sessionId, BaseMessage message) {
        if (sessionId == null) {
            log.warn("Cannot send message - sessionId is null");
            return;
        }
        
        String jsonMessage = gson.toJson(message);
        gameWebSocketHandler.sendMessage(sessionId, jsonMessage);
        log.debug("Message sent to session {}: {}", sessionId, message.getType());
    }
    
    public void sendMessageToPlayer(String playerId, BaseMessage message) {
        String sessionId = playerToSession.get(playerId);
        if (sessionId != null) {
            sendMessage(sessionId, message);
        } else {
            log.warn("Cannot send message to player {} - no active session", playerId);
        }
    }
    
    public void sendMessageToPlayers(List<String> playerIds, BaseMessage message) {
        playerIds.forEach(playerId -> sendMessageToPlayer(playerId, message));
    }
    
    // Broadcast methods removed - use MessageRouter with RoomService instead
    
    public void broadcastToAll(BaseMessage message) {
        String jsonMessage = gson.toJson(message);
        gameWebSocketHandler.broadcastMessage(jsonMessage);
        log.debug("Broadcast message to all sessions: {}", message.getType());
    }
    
    // Status and statistics
    public int getActiveConnectionCount() {
        return sessions.size();
    }
    
    // Room statistics removed - use RoomService instead
    
    public boolean isSessionActive(String sessionId) {
        Object session = sessions.get(sessionId);
        return session != null;
    }
    
    public boolean isPlayerOnline(String playerId) {
        String sessionId = playerToSession.get(playerId);
        return sessionId != null && isSessionActive(sessionId);
    }
    
    // Online players in room removed - use RoomService instead
    
    public Set<String> getAllConnectedPlayers() {
        return Set.copyOf(playerToSession.keySet());
    }
    
    // Active rooms removed - use RoomService instead
    
    // Health check and cleanup
    public void cleanupClosedSessions() {
        // TCP sockets handle their own cleanup in the handler
        log.info("Session cleanup delegated to TCP handler");
    }
    
    public void closePlayerSession(String playerId) {
        String sessionId = playerToSession.get(playerId);
        if (sessionId != null) {
            removeSession(sessionId);
            log.info("Closed session for player {}", playerId);
        }
    }
    
    // Debug and monitoring
    public Map<String, Object> getConnectionStats() {
        return Map.of(
            "totalSessions", sessions.size(),
            "totalPlayers", playerToSession.size(),
            "activeSessions", sessions.size()
        );
    }
    
    public void logConnectionStatus() {
        log.info("Connection Status - Sessions: {}, Players: {}", 
            sessions.size(), playerToSession.size());
    }
}