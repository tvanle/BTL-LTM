package com.wordbrain2.model.booster;

import com.wordbrain2.model.enums.BoosterType;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class FreezeBooster extends Booster {
    private List<String> targetPlayerIds;
    private int freezeDuration = 3000;
    
    public FreezeBooster() {
        this.type = BoosterType.FREEZE;
        this.name = "Freeze Opponents";
        this.description = "Freeze all opponents for 3 seconds";
        this.cooldownSeconds = 5;
        this.duration = 3;
        this.maxUses = 1;
        this.usesRemaining = 1;
    }
    
    @Override
    public void activate() {
        System.out.println("Freeze activated by player: " + playerId);
    }
    
    @Override
    public void deactivate() {
        System.out.println("Freeze effect ended");
    }
    
    @Override
    public boolean canUse() {
        return usesRemaining > 0 && !isActive;
    }
    
    public boolean isPlayerFrozen(String playerId) {
        return isActive && targetPlayerIds != null && targetPlayerIds.contains(playerId);
    }
}