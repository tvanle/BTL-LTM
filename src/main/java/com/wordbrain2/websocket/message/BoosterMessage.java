package com.wordbrain2.websocket.message;

import com.wordbrain2.model.enums.BoosterType;
import lombok.Data;

import java.util.Map;

public class BoosterMessage extends BaseMessage {
    
    private BoosterType boosterType;
    private String targetPlayerId;
    private long duration;
    private boolean isActive;
    
    public BoosterMessage() {
        super();
    }
    
    public BoosterMessage(String type, String roomCode) {
        super();
        setType(type);
        setRoomCode(roomCode);
    }
    
    // Static factory methods for common booster messages
    public static BoosterMessage boosterUsed(String roomCode, String playerId, BoosterType boosterType) {
        BoosterMessage message = new BoosterMessage("BOOSTER_USED", roomCode);
        message.setPlayerId(playerId);
        message.boosterType = boosterType;
        message.isActive = true;
        message.setData(Map.of(
            "boosterType", boosterType.name(),
            "playerId", playerId,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static BoosterMessage boosterApplied(String roomCode, String playerId, BoosterType boosterType, Map<String, Object> effects) {
        BoosterMessage message = new BoosterMessage("BOOSTER_APPLIED", roomCode);
        message.setPlayerId(playerId);
        message.boosterType = boosterType;
        message.isActive = true;
        message.setData(Map.of(
            "boosterType", boosterType.name(),
            "playerId", playerId,
            "effects", effects,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static BoosterMessage effectReceived(String roomCode, String targetPlayerId, BoosterType boosterType, long duration) {
        BoosterMessage message = new BoosterMessage("EFFECT_RECEIVED", roomCode);
        message.targetPlayerId = targetPlayerId;
        message.boosterType = boosterType;
        message.duration = duration;
        message.isActive = true;
        message.setData(Map.of(
            "boosterType", boosterType.name(),
            "targetPlayerId", targetPlayerId,
            "duration", duration,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static BoosterMessage freezeEffect(String roomCode, String targetPlayerId, long duration) {
        BoosterMessage message = effectReceived(roomCode, targetPlayerId, BoosterType.FREEZE, duration);
        message.setType("PLAYER_FROZEN");
        return message;
    }
    
    public static BoosterMessage shieldActivated(String roomCode, String playerId) {
        BoosterMessage message = new BoosterMessage("SHIELD_ACTIVATED", roomCode);
        message.setPlayerId(playerId);
        message.boosterType = BoosterType.SHIELD;
        message.isActive = true;
        message.setData(Map.of(
            "playerId", playerId,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static BoosterMessage doublePointsActivated(String roomCode, String playerId, int wordsRemaining) {
        BoosterMessage message = new BoosterMessage("DOUBLE_POINTS_ACTIVATED", roomCode);
        message.setPlayerId(playerId);
        message.boosterType = BoosterType.DOUBLE_UP;
        message.isActive = true;
        message.setData(Map.of(
            "playerId", playerId,
            "wordsRemaining", wordsRemaining,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static BoosterMessage timeBonus(String roomCode, String playerId, long bonusTime) {
        BoosterMessage message = new BoosterMessage("TIME_BONUS_APPLIED", roomCode);
        message.setPlayerId(playerId);
        message.boosterType = BoosterType.TIME_PLUS;
        message.duration = bonusTime;
        message.setData(Map.of(
            "playerId", playerId,
            "bonusTime", bonusTime,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static BoosterMessage hintRevealed(String roomCode, String playerId, String hint) {
        BoosterMessage message = new BoosterMessage("HINT_REVEALED", roomCode);
        message.setPlayerId(playerId);
        message.boosterType = BoosterType.REVEAL;
        message.setData(Map.of(
            "playerId", playerId,
            "hint", hint,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static BoosterMessage streakSaved(String roomCode, String playerId, int savedStreak) {
        BoosterMessage message = new BoosterMessage("STREAK_SAVED", roomCode);
        message.setPlayerId(playerId);
        message.boosterType = BoosterType.STREAK_SAVE;
        message.setData(Map.of(
            "playerId", playerId,
            "savedStreak", savedStreak,
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static BoosterMessage boosterBlocked(String roomCode, String targetPlayerId, BoosterType blockedBooster) {
        BoosterMessage message = new BoosterMessage("BOOSTER_BLOCKED", roomCode);
        message.targetPlayerId = targetPlayerId;
        message.boosterType = blockedBooster;
        message.setData(Map.of(
            "targetPlayerId", targetPlayerId,
            "blockedBooster", blockedBooster.name(),
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public static BoosterMessage effectExpired(String roomCode, String playerId, BoosterType boosterType) {
        BoosterMessage message = new BoosterMessage("EFFECT_EXPIRED", roomCode);
        message.setPlayerId(playerId);
        message.boosterType = boosterType;
        message.isActive = false;
        message.setData(Map.of(
            "playerId", playerId,
            "boosterType", boosterType.name(),
            "timestamp", System.currentTimeMillis()
        ));
        return message;
    }
    
    public boolean isActive() { return isActive; }
    public void setActive(boolean active) { this.isActive = active; }
    
    @Override
    public String toString() {
        return "BoosterMessage{" +
               "type='" + getType() + '\'' +
               ", roomCode='" + getRoomCode() + '\'' +
               ", boosterType=" + boosterType +
               ", targetPlayerId='" + targetPlayerId + '\'' +
               ", duration=" + duration +
               ", isActive=" + isActive +
               '}';
    }
}