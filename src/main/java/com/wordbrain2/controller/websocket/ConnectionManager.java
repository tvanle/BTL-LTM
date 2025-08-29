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

@Slf4j
@Component
public class ConnectionManager {
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToPlayer = new ConcurrentHashMap<>();
    private final Map<String, String> playerToSession = new ConcurrentHashMap<>();
    private final Map<String, Set<String>> roomToPlayers = new ConcurrentHashMap<>();
    private final Map<String, String> playerToRoom = new ConcurrentHashMap<>();
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
            String roomCode = playerToRoom.remove(playerId);
            if (roomCode != null) {
                removePlayerFromRoom(playerId, roomCode);
            }
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
            String roomCode = playerToRoom.remove(playerId);
            if (roomCode != null) {
                removePlayerFromRoom(playerId, roomCode);
            }
            log.debug("Player {} unregistered from session {}", playerId, sessionId);
        }
    }
    
    // Room management
    public void addPlayerToRoom(String playerId, String roomCode) {
        playerToRoom.put(playerId, roomCode);
        roomToPlayers.computeIfAbsent(roomCode, k -> ConcurrentHashMap.newKeySet()).add(playerId);
        log.debug("Player {} added to room {}", playerId, roomCode);
    }
    
    public void removePlayerFromRoom(String playerId, String roomCode) {
        playerToRoom.remove(playerId);
        Set<String> roomPlayers = roomToPlayers.get(roomCode);
        if (roomPlayers != null) {
            roomPlayers.remove(playerId);
            if (roomPlayers.isEmpty()) {
                roomToPlayers.remove(roomCode);
                log.debug("Room {} removed (empty)", roomCode);
            }
        }
        log.debug("Player {} removed from room {}", playerId, roomCode);
    }
    
    public Set<String> getPlayersInRoom(String roomCode) {
        return roomToPlayers.getOrDefault(roomCode, Set.of());
    }
    
    public String getPlayerRoom(String playerId) {
        if (playerId == null) {
            return null;
        }
        return playerToRoom.get(playerId);
    }
    
    public boolean isPlayerInRoom(String playerId, String roomCode) {
        if (playerId == null || roomCode == null) {
            return false;
        }
        return roomCode.equals(playerToRoom.get(playerId));
    }
    
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
    
    public void broadcastToRoom(String roomCode, BaseMessage message) {
        Set<String> players = getPlayersInRoom(roomCode);
        if (players.isEmpty()) {
            log.warn("Cannot broadcast to room {} - no players", roomCode);
            return;
        }
        
        players.forEach(playerId -> sendMessageToPlayer(playerId, message));
        log.debug("Broadcast message to room {} ({} players): {}", roomCode, players.size(), message.getType());
    }
    
    public void broadcastToRoomExcept(String roomCode, String excludePlayerId, BaseMessage message) {
        Set<String> players = getPlayersInRoom(roomCode);
        players.stream()
            .filter(playerId -> !playerId.equals(excludePlayerId))
            .forEach(playerId -> sendMessageToPlayer(playerId, message));
        log.debug("Broadcast message to room {} excluding player {}: {}", roomCode, excludePlayerId, message.getType());
    }
    
    public void broadcastToAll(BaseMessage message) {
        sessions.values().forEach(session -> sendMessage(session, message));
        log.debug("Broadcast message to all sessions: {}", message.getType());
    }
    
    // Status and statistics
    public int getActiveConnectionCount() {
        return sessions.size();
    }
    
    public int getRoomPlayerCount(String roomCode) {
        Set<String> players = roomToPlayers.get(roomCode);
        return players != null ? players.size() : 0;
    }
    
    public int getTotalRoomCount() {
        return roomToPlayers.size();
    }
    
    public boolean isSessionActive(String sessionId) {
        WebSocketSession session = sessions.get(sessionId);
        return session != null && session.isOpen();
    }
    
    public boolean isPlayerOnline(String playerId) {
        String sessionId = playerToSession.get(playerId);
        return sessionId != null && isSessionActive(sessionId);
    }
    
    public List<String> getOnlinePlayersInRoom(String roomCode) {
        return getPlayersInRoom(roomCode).stream()
            .filter(this::isPlayerOnline)
            .collect(Collectors.toList());
    }
    
    public Set<String> getAllConnectedPlayers() {
        return Set.copyOf(playerToSession.keySet());
    }
    
    public Set<String> getAllActiveRooms() {
        return Set.copyOf(roomToPlayers.keySet());
    }
    
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
            "totalRooms", roomToPlayers.size(),
            "activeSessions", sessions.values().stream().mapToInt(session -> session.isOpen() ? 1 : 0).sum()
        );
    }
    
    public void logConnectionStatus() {
        log.info("Connection Status - Sessions: {}, Players: {}, Rooms: {}", 
            sessions.size(), playerToSession.size(), roomToPlayers.size());
    }
}