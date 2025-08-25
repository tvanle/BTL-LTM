package com.wordbrain2.model.booster;

import com.wordbrain2.model.enums.BoosterType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class TimeBooster extends Booster {
    private int extraSeconds = 5;
    
    public TimeBooster() {
        this.type = BoosterType.TIME_PLUS;
        this.name = "Extra Time";
        this.description = "Add 5 seconds to the timer";
        this.cooldownSeconds = 0;
        this.duration = 0;
        this.maxUses = 2;
        this.usesRemaining = 2;
    }
    
    @Override
    public void activate() {
        System.out.println("Added " + extraSeconds + " seconds for player: " + playerId);
    }
    
    @Override
    public void deactivate() {
    }
    
    @Override
    public boolean canUse() {
        return usesRemaining > 0;
    }
}