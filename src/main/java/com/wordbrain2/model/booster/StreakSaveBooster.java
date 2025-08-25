package com.wordbrain2.model.booster;

import com.wordbrain2.model.enums.BoosterType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class StreakSaveBooster extends Booster {
    
    public StreakSaveBooster() {
        this.type = BoosterType.STREAK_SAVE;
        this.name = "Streak Saver";
        this.description = "Protect your streak from one wrong answer";
        this.cooldownSeconds = 0;
        this.duration = -1;
        this.maxUses = 1;
        this.usesRemaining = 1;
    }
    
    @Override
    public void activate() {
        System.out.println("Streak protection activated for player: " + playerId);
    }
    
    @Override
    public void deactivate() {
        System.out.println("Streak protection used for player: " + playerId);
    }
    
    @Override
    public boolean canUse() {
        return usesRemaining > 0 && !isActive;
    }
    
    public boolean consumeProtection() {
        if (isActive) {
            deactivate();
            isActive = false;
            return true;
        }
        return false;
    }
}