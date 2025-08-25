package com.wordbrain2.service.core;

import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import java.util.HashSet;
import java.util.Collections;

@Service
public class SessionService {
    private final Map<String, WebSocketSession> playerSessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToPlayer = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> roomSessions = new ConcurrentHashMap<>();
    private final Map<String, Long> lastHeartbeat = new ConcurrentHashMap<>();
    
    public void registerSession(String playerId, WebSocketSession session) {
        playerSessions.put(playerId, session);
        sessionToPlayer.put(session.getId(), playerId);
        lastHeartbeat.put(playerId, System.currentTimeMillis());
    }
    
    public void unregisterSession(String sessionId) {
        String playerId = sessionToPlayer.remove(sessionId);
        if (playerId != null) {
            playerSessions.remove(playerId);
            lastHeartbeat.remove(playerId);
        }
    }
    
    public WebSocketSession getSession(String playerId) {
        return playerSessions.get(playerId);
    }
    
    public String getPlayerId(String sessionId) {
        return sessionToPlayer.get(sessionId);
    }
    
    public void addSessionToRoom(String roomCode, String playerId) {
        roomSessions.computeIfAbsent(roomCode, k -> Collections.synchronizedSet(new HashSet<>()))
                    .add(playerId);
    }
    
    public void removeSessionFromRoom(String roomCode, String playerId) {
        Set<String> sessions = roomSessions.get(roomCode);
        if (sessions != null) {
            sessions.remove(playerId);
            if (sessions.isEmpty()) {
                roomSessions.remove(roomCode);
            }
        }
    }
    
    public Set<String> getRoomSessions(String roomCode) {
        return roomSessions.getOrDefault(roomCode, Collections.emptySet());
    }
    
    public void updateHeartbeat(String playerId) {
        lastHeartbeat.put(playerId, System.currentTimeMillis());
    }
    
    public boolean isSessionActive(String playerId) {
        WebSocketSession session = playerSessions.get(playerId);
        return session != null && session.isOpen();
    }
    
    public Map<String, Long> getInactiveSessions(long timeoutMillis) {
        Map<String, Long> inactive = new ConcurrentHashMap<>();
        long currentTime = System.currentTimeMillis();
        
        lastHeartbeat.forEach((playerId, lastBeat) -> {
            if (currentTime - lastBeat > timeoutMillis) {
                inactive.put(playerId, lastBeat);
            }
        });
        
        return inactive;
    }
    
    public void cleanupInactiveSessions(long timeoutMillis) {
        Map<String, Long> inactive = getInactiveSessions(timeoutMillis);
        inactive.keySet().forEach(playerId -> {
            WebSocketSession session = playerSessions.get(playerId);
            if (session != null) {
                try {
                    session.close();
                } catch (Exception e) {
                    // Log error
                }
            }
            unregisterSession(sessionToPlayer.entrySet().stream()
                .filter(e -> e.getValue().equals(playerId))
                .map(Map.Entry::getKey)
                .findFirst()
                .orElse(null));
        });
    }
}