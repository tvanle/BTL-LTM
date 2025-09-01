package com.wordbrain2.controller.websocket;

import com.google.gson.Gson;
import com.wordbrain2.websocket.message.BaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

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
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToPlayer = new ConcurrentHashMap<>();
    private final Map<String, String> playerToSession = new ConcurrentHashMap<>();
    private final Gson gson = new Gson();
    
    // Basic session management
    public void addSession(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
        log.info("Session added: {}", sessionId);
    }
    
    public void removeSession(String sessionId) {
        String playerId = sessionToPlayer.remove(sessionId);
        if (playerId != null) {
            playerToSession.remove(playerId);
        }
        sessions.remove(sessionId);
        log.info("Session removed: {}", sessionId);
    }
    
    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    public WebSocketSession getSessionByPlayerId(String playerId) {
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
    public void sendMessage(WebSocketSession session, BaseMessage message) {
        if (session == null || !session.isOpen()) {
            log.warn("Cannot send message - session is null or closed");
            return;
        }
        
        try {
            String jsonMessage = gson.toJson(message);
            session.sendMessage(new TextMessage(jsonMessage));
            log.debug("Message sent to session {}: {}", session.getId(), message.getType());
        } catch (IOException e) {
            log.error("Failed to send message to session {}: {}", session.getId(), e.getMessage());
        }
    }
    
    public void sendMessageToPlayer(String playerId, BaseMessage message) {
        WebSocketSession session = getSessionByPlayerId(playerId);
        if (session != null) {
            sendMessage(session, message);
        } else {
            log.warn("Cannot send message to player {} - no active session", playerId);
        }
    }
    
    public void sendMessageToPlayers(List<String> playerIds, BaseMessage message) {
        playerIds.forEach(playerId -> sendMessageToPlayer(playerId, message));
    }
    
    // Broadcast methods removed - use MessageRouter with RoomService instead
    
    public void broadcastToAll(BaseMessage message) {
        sessions.values().forEach(session -> sendMessage(session, message));
        log.debug("Broadcast message to all sessions: {}", message.getType());
    }
    
    // Status and statistics
    public int getActiveConnectionCount() {
        return sessions.size();
    }
    
    // Room statistics removed - use RoomService instead
    
    public boolean isSessionActive(String sessionId) {
        WebSocketSession session = sessions.get(sessionId);
        return session != null && session.isOpen();
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
        List<String> closedSessions = sessions.entrySet().stream()
            .filter(entry -> !entry.getValue().isOpen())
            .map(Map.Entry::getKey)
            .collect(Collectors.toList());
        
        closedSessions.forEach(this::removeSession);
        log.info("Cleaned up {} closed sessions", closedSessions.size());
    }
    
    public void closePlayerSession(String playerId) {
        WebSocketSession session = getSessionByPlayerId(playerId);
        if (session != null && session.isOpen()) {
            try {
                session.close();
                log.info("Closed session for player {}", playerId);
            } catch (IOException e) {
                log.error("Failed to close session for player {}: {}", playerId, e.getMessage());
            }
        }
    }
    
    // Debug and monitoring
    public Map<String, Object> getConnectionStats() {
        return Map.of(
            "totalSessions", sessions.size(),
            "totalPlayers", playerToSession.size(),
            "activeSessions", sessions.values().stream().mapToInt(session -> session.isOpen() ? 1 : 0).sum()
        );
    }
    
    public void logConnectionStatus() {
        log.info("Connection Status - Sessions: {}, Players: {}", 
            sessions.size(), playerToSession.size());
    }
}