package com.wordbrain2.service.event;

import com.wordbrain2.model.enums.BoosterType;
import com.wordbrain2.model.enums.MessageType;
import com.wordbrain2.websocket.message.BaseMessage;
import com.wordbrain2.service.core.SessionService;
import com.google.gson.Gson;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class EventBusService {
    @Autowired
    private SessionService sessionService;
    
    @Autowired(required = false)
    private NotificationService notificationService;
    
    private final Gson gson = new Gson();
    private final Map<String, CopyOnWriteArrayList<EventListener>> roomListeners = new ConcurrentHashMap<>();
    
    public void publishEvent(String roomCode, String eventType, Map<String, Object> data) {
        // Notify internal listeners
        CopyOnWriteArrayList<EventListener> listeners = roomListeners.get(roomCode);
        if (listeners != null) {
            Event event = new Event(eventType, data);
            listeners.forEach(listener -> listener.onEvent(event));
        }
        
        // Broadcast to WebSocket clients
        broadcastToRoom(roomCode, eventType, data);
    }
    
    public void broadcastToRoom(String roomCode, String eventType, Map<String, Object> data) {
        Set<String> playerIds = sessionService.getRoomSessions(roomCode);
        
        BaseMessage message = new BaseMessage();
        message.setType(eventType);
        message.setData(data);
        message.setTimestamp(System.currentTimeMillis());
        
        String jsonMessage = gson.toJson(message);
        
        playerIds.forEach(playerId -> {
            sendToPlayer(playerId, jsonMessage);
        });
    }
    
    public void sendToPlayer(String playerId, String message) {
        WebSocketSession session = sessionService.getSession(playerId);
        if (session != null && session.isOpen()) {
            try {
                session.sendMessage(new TextMessage(message));
            } catch (Exception e) {
                System.err.println("Failed to send message to player " + playerId + ": " + e.getMessage());
            }
        }
    }
    
    public void publishGameStart(String roomCode, int countdown) {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("countdown", countdown);
        data.put("message", "Game starting in " + countdown + " seconds!");
        data.put("timestamp", System.currentTimeMillis());
        publishEvent(roomCode, MessageType.GAME_STARTING.name(), data);
    }
    
    public void publishLevelStart(String roomCode, int levelNumber, Map<String, Object> levelData) {
        Map<String, Object> data = new ConcurrentHashMap<>(levelData);
        data.put("level", levelNumber);
        data.put("message", "Level " + levelNumber + " started!");
        data.put("timestamp", System.currentTimeMillis());
        publishEvent(roomCode, MessageType.LEVEL_START.name(), data);
    }
    
    public void publishLevelEnd(String roomCode, int levelNumber, Map<String, Object> results) {
        Map<String, Object> data = new ConcurrentHashMap<>(results);
        data.put("level", levelNumber);
        data.put("message", "Level " + levelNumber + " completed!");
        data.put("timestamp", System.currentTimeMillis());
        publishEvent(roomCode, MessageType.LEVEL_END.name(), data);
    }
    
    public void publishGameEnd(String roomCode, String winnerId, Map<String, Object> finalScores) {
        Map<String, Object> data = new ConcurrentHashMap<>(finalScores);
        data.put("winnerId", winnerId);
        data.put("message", "Game Over!");
        data.put("timestamp", System.currentTimeMillis());
        publishEvent(roomCode, MessageType.GAME_END.name(), data);
    }
    
    public void publishWordAccepted(String roomCode, String playerId, String word, int points) {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("playerId", playerId);
        data.put("word", word);
        data.put("points", points);
        data.put("result", "ACCEPTED");
        publishEvent(roomCode, MessageType.WORD_ACCEPTED.name(), data);
    }
    
    public void publishWordRejected(String roomCode, String playerId, String word, String reason) {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("playerId", playerId);
        data.put("word", word);
        data.put("reason", reason);
        data.put("result", "REJECTED");
        publishEvent(roomCode, MessageType.WORD_REJECTED.name(), data);
    }
    
    public void publishBoosterUsed(String roomCode, String playerId, BoosterType boosterType) {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("playerId", playerId);
        data.put("boosterType", boosterType.name());
        data.put("timestamp", System.currentTimeMillis());
        publishEvent(roomCode, MessageType.BOOSTER_APPLIED.name(), data);
    }
    
    public void publishLeaderboardUpdate(String roomCode, Map<String, Object> leaderboardData) {
        publishEvent(roomCode, MessageType.LEADERBOARD_UPDATE.name(), leaderboardData);
    }
    
    public void publishPlayerJoined(String roomCode, String playerId, String playerName) {
        if (notificationService != null) {
            notificationService.notifyPlayerJoined(roomCode, playerId, playerName);
        }
    }
    
    public void publishPlayerLeft(String roomCode, String playerId, String playerName) {
        if (notificationService != null) {
            notificationService.notifyPlayerLeft(roomCode, playerId, playerName);
        }
    }
    
    public void publishPlayerReady(String roomCode, String playerId, boolean ready) {
        Map<String, Object> data = new ConcurrentHashMap<>();
        data.put("playerId", playerId);
        data.put("ready", ready);
        publishEvent(roomCode, MessageType.PLAYER_READY.name(), data);
    }
    
    public void registerListener(String roomCode, EventListener listener) {
        roomListeners.computeIfAbsent(roomCode, k -> new CopyOnWriteArrayList<>()).add(listener);
    }
    
    public void unregisterListener(String roomCode, EventListener listener) {
        CopyOnWriteArrayList<EventListener> listeners = roomListeners.get(roomCode);
        if (listeners != null) {
            listeners.remove(listener);
        }
    }
    
    public void clearRoomListeners(String roomCode) {
        roomListeners.remove(roomCode);
    }
    
    public interface EventListener {
        void onEvent(Event event);
    }
    
    public static class Event {
        private final String type;
        private final Map<String, Object> data;
        private final long timestamp;
        
        public Event(String type, Map<String, Object> data) {
            this.type = type;
            this.data = data;
            this.timestamp = System.currentTimeMillis();
        }
        
        public String getType() { return type; }
        public Map<String, Object> getData() { return data; }
        public long getTimestamp() { return timestamp; }
    }
}