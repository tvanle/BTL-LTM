package com.wordbrain2.controller.websocket;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class ConnectionManager {
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToPlayer = new ConcurrentHashMap<>();
    
    public void addSession(String sessionId, WebSocketSession session) {
        sessions.put(sessionId, session);
        log.info("Session added: {}", sessionId);
    }
    
    public void removeSession(String sessionId) {
        sessions.remove(sessionId);
        sessionToPlayer.remove(sessionId);
        log.info("Session removed: {}", sessionId);
    }
    
    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    public void registerPlayer(String sessionId, String playerId) {
        sessionToPlayer.put(sessionId, playerId);
        log.debug("Player {} registered for session {}", playerId, sessionId);
    }
    
    public String getPlayerId(String sessionId) {
        return sessionToPlayer.get(sessionId);
    }
    
    public void unregisterPlayer(String sessionId) {
        String playerId = sessionToPlayer.remove(sessionId);
        if (playerId != null) {
            log.debug("Player {} unregistered from session {}", playerId, sessionId);
        }
    }
    
    public int getActiveConnectionCount() {
        return sessions.size();
    }
    
    public boolean isSessionActive(String sessionId) {
        WebSocketSession session = sessions.get(sessionId);
        return session != null && session.isOpen();
    }
}