package com.wordbrain2.model.booster;

import com.wordbrain2.model.enums.BoosterType;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
public class ShieldBooster extends Booster {
    private boolean blocksFreezeEffect = true;
    
    public ShieldBooster() {
        this.type = BoosterType.SHIELD;
        this.name = "Protection Shield";
        this.description = "Block one negative effect from opponents";
        this.cooldownSeconds = 0;
        this.duration = -1;
        this.maxUses = 1;
        this.usesRemaining = 1;
    }
    
    @Override
    public void activate() {
        System.out.println("Shield activated for player: " + playerId);
    }
    
    @Override
    public void deactivate() {
        System.out.println("Shield consumed for player: " + playerId);
    }
    
    @Override
    public boolean canUse() {
        return usesRemaining > 0 && !isActive;
    }
    
    public boolean blockEffect(BoosterType incomingEffect) {
        if (isActive && incomingEffect == BoosterType.FREEZE) {
            deactivate();
            isActive = false;
            return true;
        }
        return false;
    }
}