package com.wordbrain2.repository;

import com.wordbrain2.model.entity.GameSession;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class GameSessionRepository {
    
    private final Map<String, GameSession> sessions = new ConcurrentHashMap<>();
    
    public GameSession save(GameSession session) {
        sessions.put(session.getId(), session);
        return session;
    }
    
    public Optional<GameSession> findById(String sessionId) {
        return Optional.ofNullable(sessions.get(sessionId));
    }
    
    public Optional<GameSession> findByRoomCode(String roomCode) {
        return sessions.values().stream()
            .filter(session -> roomCode.equals(session.getRoomCode()))
            .findFirst();
    }
    
    public void deleteById(String sessionId) {
        sessions.remove(sessionId);
    }
    
    public void deleteByRoomCode(String roomCode) {
        sessions.entrySet().removeIf(entry -> 
            roomCode.equals(entry.getValue().getRoomCode()));
    }
    
    public long countActiveSessions() {
        return sessions.size();
    }
    
    public void clear() {
        sessions.clear();
    }
}