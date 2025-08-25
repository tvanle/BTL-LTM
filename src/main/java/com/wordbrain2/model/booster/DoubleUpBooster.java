package com.wordbrain2.model.booster;

import com.wordbrain2.model.enums.BoosterType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class DoubleUpBooster extends Booster {
    private double multiplier = 2.0;
    
    public DoubleUpBooster() {
        this.type = BoosterType.DOUBLE_UP;
        this.name = "Double Points";
        this.description = "Double your points for the next correct answer";
        this.cooldownSeconds = 3;
        this.duration = 0;
        this.maxUses = 1;
        this.usesRemaining = 1;
    }
    
    @Override
    public void activate() {
        System.out.println("Double points activated for player: " + playerId);
    }
    
    @Override
    public void deactivate() {
        System.out.println("Double points deactivated for player: " + playerId);
    }
    
    @Override
    public boolean canUse() {
        return usesRemaining > 0 && !isActive;
    }
    
    public int applyMultiplier(int basePoints) {
        if (isActive) {
            return (int)(basePoints * multiplier);
        }
        return basePoints;
    }
}