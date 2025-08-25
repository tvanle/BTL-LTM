package com.wordbrain2.service.booster;

import com.wordbrain2.model.booster.*;
import com.wordbrain2.model.enums.BoosterType;
import com.wordbrain2.model.entity.Player;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class BoosterService {
    @Autowired
    private BoosterValidator boosterValidator;
    
    @Autowired
    private BoosterEffectApplier boosterEffectApplier;
    
    private final Map<String, Map<BoosterType, Booster>> playerBoosters = new ConcurrentHashMap<>();
    private final Map<String, List<BoosterType>> roomEnabledBoosters = new ConcurrentHashMap<>();
    
    public void initializePlayerBoosters(String playerId, String roomCode) {
        List<BoosterType> enabledTypes = roomEnabledBoosters.get(roomCode);
        if (enabledTypes == null) {
            enabledTypes = getDefaultBoosters();
        }
        
        Map<BoosterType, Booster> boosters = new HashMap<>();
        for (BoosterType type : enabledTypes) {
            Booster booster = createBooster(type);
            booster.setPlayerId(playerId);
            boosters.put(type, booster);
        }
        
        playerBoosters.put(playerId, boosters);
    }
    
    public void setRoomBoosters(String roomCode, List<BoosterType> enabledBoosters) {
        roomEnabledBoosters.put(roomCode, enabledBoosters);
    }
    
    public boolean useBooster(String playerId, BoosterType type, String roomCode) {
        Map<BoosterType, Booster> boosters = playerBoosters.get(playerId);
        if (boosters == null) {
            return false;
        }
        
        Booster booster = boosters.get(type);
        if (booster == null || !booster.canUse()) {
            return false;
        }
        
        // Do basic validation here instead of in validator
        if (!boosterValidator.canUseBooster(playerId, type, roomCode)) {
            return false;
        }
        
        booster.use();
        boosterEffectApplier.applyEffect(playerId, type, roomCode);
        return true;
    }
    
    public Booster getBooster(String playerId, BoosterType type) {
        Map<BoosterType, Booster> boosters = playerBoosters.get(playerId);
        return boosters != null ? boosters.get(type) : null;
    }
    
    public Map<BoosterType, Booster> getPlayerBoosters(String playerId) {
        return playerBoosters.getOrDefault(playerId, new HashMap<>());
    }
    
    public void resetPlayerBoosters(String playerId) {
        Map<BoosterType, Booster> boosters = playerBoosters.get(playerId);
        if (boosters != null) {
            boosters.values().forEach(Booster::reset);
        }
    }
    
    public void clearPlayerBoosters(String playerId) {
        playerBoosters.remove(playerId);
    }
    
    public void clearRoomBoosters(String roomCode) {
        roomEnabledBoosters.remove(roomCode);
    }
    
    private Booster createBooster(BoosterType type) {
        switch (type) {
            case DOUBLE_UP:
                return new DoubleUpBooster();
            case FREEZE:
                return new FreezeBooster();
            case REVEAL:
                return new RevealBooster();
            case TIME_PLUS:
                return new TimeBooster();
            case SHIELD:
                return new ShieldBooster();
            case STREAK_SAVE:
                return new StreakSaveBooster();
            case SKIP_HALF:
                return new SkipHalfBooster();
            default:
                throw new IllegalArgumentException("Unknown booster type: " + type);
        }
    }
    
    private List<BoosterType> getDefaultBoosters() {
        return Arrays.asList(
            BoosterType.DOUBLE_UP,
            BoosterType.FREEZE,
            BoosterType.REVEAL,
            BoosterType.TIME_PLUS
        );
    }
    
    public boolean isBoosterActive(String playerId, BoosterType type) {
        Booster booster = getBooster(playerId, type);
        return booster != null && booster.isActive();
    }
    
    public void checkAndExpireBoosters(String playerId) {
        Map<BoosterType, Booster> boosters = playerBoosters.get(playerId);
        if (boosters != null) {
            boosters.values().forEach(booster -> {
                if (booster.isExpired()) {
                    booster.reset();
                }
            });
        }
    }
}