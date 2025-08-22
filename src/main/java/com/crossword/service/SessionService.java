package com.crossword.service;

import com.crossword.model.Player;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class SessionService {
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToPlayer = new ConcurrentHashMap<>();
    
    public void addSession(String sessionId, WebSocketSession session, Player player) {
        sessions.put(sessionId, session);
        players.put(player.getId(), player);
        sessionToPlayer.put(sessionId, player.getId());
    }
    
    public void removeSession(String sessionId) {
        String playerId = sessionToPlayer.get(sessionId);
        if (playerId != null) {
            Player player = players.get(playerId);
            if (player != null) {
                player.setConnected(false);
            }
        }
        
        sessions.remove(sessionId);
        sessionToPlayer.remove(sessionId);
    }
    
    public WebSocketSession getSession(String sessionId) {
        return sessions.get(sessionId);
    }
    
    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }
    
    public Player getPlayerBySessionId(String sessionId) {
        String playerId = sessionToPlayer.get(sessionId);
        return playerId != null ? players.get(playerId) : null;
    }
    
    public String getSessionIdByPlayerId(String playerId) {
        for (Map.Entry<String, String> entry : sessionToPlayer.entrySet()) {
            if (playerId.equals(entry.getValue())) {
                return entry.getKey();
            }
        }
        return null;
    }
    
    public WebSocketSession getSessionByPlayerId(String playerId) {
        String sessionId = getSessionIdByPlayerId(playerId);
        return sessionId != null ? sessions.get(sessionId) : null;
    }
    
    public boolean isPlayerConnected(String playerId) {
        Player player = players.get(playerId);
        return player != null && player.isConnected();
    }
    
    public void updatePlayerActivity(String playerId) {
        Player player = players.get(playerId);
        if (player != null) {
            player.updateActivity();
        }
    }
    
    public Map<String, Player> getAllPlayers() {
        return new ConcurrentHashMap<>(players);
    }
    
    public Map<String, WebSocketSession> getAllSessions() {
        return new ConcurrentHashMap<>(sessions);
    }
    
    public int getActivePlayerCount() {
        return (int) players.values().stream().filter(Player::isConnected).count();
    }
}