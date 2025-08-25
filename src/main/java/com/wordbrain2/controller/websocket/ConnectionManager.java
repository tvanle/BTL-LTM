package com.wordbrain2.controller.websocket;

import com.wordbrain2.service.core.SessionService;
import com.wordbrain2.service.core.PlayerService;
import com.wordbrain2.service.event.NotificationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Map;

@Component
public class ConnectionManager {
    @Autowired
    private SessionService sessionService;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private NotificationService notificationService;
    
    private final Map<String, Long> connectionTimes = new ConcurrentHashMap<>();
    
    public void onConnect(WebSocketSession session) {
        connectionTimes.put(session.getId(), System.currentTimeMillis());
        System.out.println("WebSocket connected: " + session.getId());
    }
    
    public void onDisconnect(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        String playerId = sessionService.getPlayerId(sessionId);
        
        if (playerId != null) {
            String roomCode = playerService.getPlayerRoom(playerId);
            if (roomCode != null) {
                // Notify room about disconnection
                notificationService.notifyPlayerDisconnected(
                    roomCode, 
                    playerId, 
                    playerService.getPlayer(playerId).getName()
                );
            }
            
            // Clean up session
            sessionService.unregisterSession(sessionId);
        }
        
        connectionTimes.remove(sessionId);
        System.out.println("WebSocket disconnected: " + sessionId + " with status: " + status);
    }
    
    public void registerPlayer(WebSocketSession session, String playerId) {
        sessionService.registerSession(playerId, session);
    }
    
    public boolean isConnectionValid(WebSocketSession session) {
        return session != null && session.isOpen();
    }
    
    public long getConnectionDuration(String sessionId) {
        Long connectTime = connectionTimes.get(sessionId);
        if (connectTime != null) {
            return System.currentTimeMillis() - connectTime;
        }
        return 0;
    }
    
    public void heartbeat(String playerId) {
        sessionService.updateHeartbeat(playerId);
    }
    
    public void cleanupInactiveSessions() {
        sessionService.cleanupInactiveSessions(30000); // 30 seconds timeout
    }
}