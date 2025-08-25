package com.wordbrain2.model.booster;

import com.wordbrain2.model.enums.BoosterType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class SkipHalfBooster extends Booster {
    private double pointsRatio = 0.5;
    
    public SkipHalfBooster() {
        this.type = BoosterType.SKIP_HALF;
        this.name = "Skip Level";
        this.description = "Skip current level with 50% points";
        this.cooldownSeconds = 0;
        this.duration = 0;
        this.maxUses = 1;
        this.usesRemaining = 1;
    }
    
    @Override
    public void activate() {
        System.out.println("Level skipped by player: " + playerId);
    }
    
    @Override
    public void deactivate() {
    }
    
    @Override
    public boolean canUse() {
        return usesRemaining > 0;
    }
    
    public int calculateSkipPoints(int maxPoints) {
        return (int)(maxPoints * pointsRatio);
    }
}