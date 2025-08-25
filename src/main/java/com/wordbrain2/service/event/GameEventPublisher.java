package com.wordbrain2.service.event;

import com.wordbrain2.model.enums.MessageType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.Map;
import java.util.HashMap;

@Service
public class GameEventPublisher {
    @Autowired
    private EventBusService eventBusService;
    
    public void publishGameStart(String roomCode, int countdown) {
        Map<String, Object> data = new HashMap<>();
        data.put("countdown", countdown);
        data.put("message", "Game starting in " + countdown + " seconds!");
        data.put("timestamp", System.currentTimeMillis());
        
        eventBusService.publishEvent(roomCode, MessageType.GAME_STARTING.name(), data);
    }
    
    public void publishLevelStart(String roomCode, int levelNumber, Map<String, Object> levelData) {
        Map<String, Object> data = new HashMap<>(levelData);
        data.put("level", levelNumber);
        data.put("message", "Level " + levelNumber + " started!");
        data.put("timestamp", System.currentTimeMillis());
        
        eventBusService.publishEvent(roomCode, MessageType.LEVEL_START.name(), data);
    }
    
    public void publishLevelEnd(String roomCode, int levelNumber, Map<String, Object> results) {
        Map<String, Object> data = new HashMap<>(results);
        data.put("level", levelNumber);
        data.put("message", "Level " + levelNumber + " completed!");
        data.put("timestamp", System.currentTimeMillis());
        
        eventBusService.publishEvent(roomCode, MessageType.LEVEL_END.name(), data);
    }
    
    public void publishGameEnd(String roomCode, String winnerId, Map<String, Object> finalScores) {
        Map<String, Object> data = new HashMap<>(finalScores);
        data.put("winnerId", winnerId);
        data.put("message", "Game Over!");
        data.put("timestamp", System.currentTimeMillis());
        
        eventBusService.publishEvent(roomCode, MessageType.GAME_END.name(), data);
    }
    
    public void publishTimerUpdate(String roomCode, int timeRemaining) {
        Map<String, Object> data = new HashMap<>();
        data.put("timeRemaining", timeRemaining);
        data.put("timestamp", System.currentTimeMillis());
        
        eventBusService.publishEvent(roomCode, MessageType.TIMER_UPDATE.name(), data);
    }
    
    public void publishOpponentScored(String roomCode, String playerId, int points, String word) {
        Map<String, Object> data = new HashMap<>();
        data.put("playerId", playerId);
        data.put("points", points);
        data.put("word", word);
        data.put("timestamp", System.currentTimeMillis());
        
        eventBusService.publishEvent(roomCode, MessageType.OPPONENT_SCORED.name(), data);
    }
    
    public void publishEffectReceived(String roomCode, String targetId, String effect, Map<String, Object> effectData) {
        Map<String, Object> data = new HashMap<>(effectData);
        data.put("targetId", targetId);
        data.put("effect", effect);
        data.put("timestamp", System.currentTimeMillis());
        
        eventBusService.publishEvent(roomCode, MessageType.EFFECT_RECEIVED.name(), data);
    }
    
    public void publishError(String roomCode, String playerId, String error, String details) {
        Map<String, Object> data = new HashMap<>();
        data.put("playerId", playerId);
        data.put("error", error);
        data.put("details", details);
        data.put("timestamp", System.currentTimeMillis());
        
        eventBusService.publishEvent(roomCode, MessageType.ERROR.name(), data);
    }
    
    public void publishInvalidAction(String roomCode, String playerId, String action, String reason) {
        Map<String, Object> data = new HashMap<>();
        data.put("playerId", playerId);
        data.put("action", action);
        data.put("reason", reason);
        data.put("timestamp", System.currentTimeMillis());
        
        eventBusService.publishEvent(roomCode, MessageType.INVALID_ACTION.name(), data);
    }
}