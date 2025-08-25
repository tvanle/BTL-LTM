package com.wordbrain2.service.booster;

import com.wordbrain2.model.booster.Booster;
import com.wordbrain2.model.enums.BoosterType;
import com.wordbrain2.model.entity.Player;
import com.wordbrain2.service.core.PlayerService;
import com.wordbrain2.service.core.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class BoosterValidator {
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private RoomService roomService;
    
    // Remove circular dependency - use method parameter instead
    
    public boolean canUseBooster(String playerId, BoosterType type, String roomCode) {
        // Check if player exists
        Player player = playerService.getPlayer(playerId);
        if (player == null) {
            return false;
        }
        
        // Check if player is in the room
        String playerRoom = playerService.getPlayerRoom(playerId);
        if (playerRoom == null || !playerRoom.equals(roomCode)) {
            return false;
        }
        
        // Check if room is in game
        if (!roomService.isRoomInGame(roomCode)) {
            return false;
        }
        
        // Booster validation will be done in BoosterService itself
        // to avoid circular dependency
        
        // Special validation for specific boosters
        switch (type) {
            case FREEZE:
                return validateFreezeBooster(playerId, roomCode);
            case SKIP_HALF:
                return validateSkipBooster(playerId, roomCode);
            case REVEAL:
                return validateRevealBooster(playerId, roomCode);
            default:
                return true;
        }
    }
    
    private boolean validateFreezeBooster(String playerId, String roomCode) {
        // Can't freeze if you're the only player
        int playerCount = roomService.getRoom(roomCode).getPlayers().size();
        return playerCount > 1;
    }
    
    private boolean validateSkipBooster(String playerId, String roomCode) {
        // Can't skip on the last level
        // This would need level information from game state
        return true;
    }
    
    private boolean validateRevealBooster(String playerId, String roomCode) {
        // Check if player has enough points for reveal cost
        int playerPoints = playerService.getPlayerScore(playerId).getTotalPoints();
        return playerPoints >= 100; // Reveal costs 100 points
    }
    
    public boolean isBoosterEnabledInRoom(String roomCode, BoosterType type) {
        return roomService.isBoosterEnabled(roomCode, type);
    }
}