package com.wordbrain2.service.booster;

import com.wordbrain2.model.booster.*;
import com.wordbrain2.model.enums.BoosterType;
import com.wordbrain2.model.entity.Player;
import com.wordbrain2.model.entity.Room;
import com.wordbrain2.service.core.PlayerService;
import com.wordbrain2.service.core.RoomService;
import com.wordbrain2.service.game.TimerService;
import com.wordbrain2.service.event.EventBusService;
import com.wordbrain2.service.scoring.StreakManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.stream.Collectors;

@Service
public class BoosterEffectApplier {
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private TimerService timerService;
    
    @Autowired
    private EventBusService eventBusService;
    
    @Autowired
    private StreakManager streakManager;
    
    // Remove BoosterService dependency to avoid circular reference
    // Booster instances will be passed as parameters when needed
    
    public void applyEffect(String playerId, BoosterType type, String roomCode) {
        switch (type) {
            case DOUBLE_UP:
                applyDoubleUp(playerId, roomCode);
                break;
            case FREEZE:
                applyFreeze(playerId, roomCode);
                break;
            case REVEAL:
                applyReveal(playerId, roomCode);
                break;
            case TIME_PLUS:
                applyTimeBonus(playerId, roomCode);
                break;
            case SHIELD:
                applyShield(playerId, roomCode);
                break;
            case STREAK_SAVE:
                applyStreakSave(playerId, roomCode);
                break;
            case SKIP_HALF:
                applySkipHalf(playerId, roomCode);
                break;
        }
        
        // Broadcast booster usage to all players
        eventBusService.publishBoosterUsed(roomCode, playerId, type);
    }
    
    private void applyDoubleUp(String playerId, String roomCode) {
        // Double points will be applied on next correct answer
        Map<String, Object> data = new HashMap<>();
        data.put("playerId", playerId);
        data.put("type", "DOUBLE_UP");
        data.put("message", "2x points activated!");
        eventBusService.publishEvent(roomCode, "BOOSTER_ACTIVATED", data);
    }
    
    private void applyFreeze(String playerId, String roomCode) {
        Room room = roomService.getRoom(roomCode);
        if (room == null) return;
        
        // Get all other players in the room
        List<String> targetPlayers = room.getPlayers().stream()
            .map(Player::getId)
            .filter(id -> !id.equals(playerId))
            .collect(Collectors.toList());
        
        // Notify frozen players
        targetPlayers.forEach(targetId -> {
            Map<String, Object> freezeData = new HashMap<>();
            freezeData.put("targetId", targetId);
            freezeData.put("duration", 3000);
            freezeData.put("sourceId", playerId);
            eventBusService.publishEvent(roomCode, "FREEZE_EFFECT", freezeData);
        });
    }
    
    private void applyReveal(String playerId, String roomCode) {
        // Deduct points for reveal
        playerService.updatePlayerScore(playerId, -100);
        
        // Reveal hint will be sent with next grid update
        Map<String, Object> revealData = new HashMap<>();
        revealData.put("playerId", playerId);
        revealData.put("message", "Hint revealed!");
        eventBusService.publishEvent(roomCode, "REVEAL_REQUESTED", revealData);
    }
    
    private void applyTimeBonus(String playerId, String roomCode) {
        // Add 5 seconds to the timer
        timerService.addTime(roomCode, 5);
        
        Map<String, Object> timeData = new HashMap<>();
        timeData.put("playerId", playerId);
        timeData.put("seconds", 5);
        timeData.put("message", "+5 seconds!");
        eventBusService.publishEvent(roomCode, "TIME_ADDED", timeData);
    }
    
    private void applyShield(String playerId, String roomCode) {
        // Shield is now active and will block next negative effect
        Map<String, Object> shieldData = new HashMap<>();
        shieldData.put("playerId", playerId);
        shieldData.put("message", "Shield activated!");
        eventBusService.publishEvent(roomCode, "SHIELD_ACTIVATED", shieldData);
    }
    
    private void applyStreakSave(String playerId, String roomCode) {
        // Save current streak
        streakManager.saveStreak(playerId);
        
        Map<String, Object> streakData = new HashMap<>();
        streakData.put("playerId", playerId);
        streakData.put("message", "Streak protection active!");
        eventBusService.publishEvent(roomCode, "STREAK_SAVED", streakData);
    }
    
    private void applySkipHalf(String playerId, String roomCode) {
        // Skip level with 50% points
        // This needs to be handled by the game engine
        Map<String, Object> skipData = new HashMap<>();
        skipData.put("playerId", playerId);
        skipData.put("pointsRatio", 0.5);
        eventBusService.publishEvent(roomCode, "LEVEL_SKIP_REQUESTED", skipData);
    }
    
    // These methods now take booster instances as parameters
    // to avoid circular dependency with BoosterService
    
    public boolean blockEffectWithShield(ShieldBooster shield, BoosterType incomingEffect) {
        if (shield != null && shield.isActive()) {
            return shield.blockEffect(incomingEffect);
        }
        return false;
    }
    
    public int applyPointMultiplier(DoubleUpBooster doubleUp, int basePoints) {
        if (doubleUp != null && doubleUp.isActive()) {
            int boostedPoints = doubleUp.applyMultiplier(basePoints);
            doubleUp.reset(); // Double up is single use
            return boostedPoints;
        }
        return basePoints;
    }
}