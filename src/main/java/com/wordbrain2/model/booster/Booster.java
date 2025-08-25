package com.wordbrain2.model.booster;

import com.wordbrain2.model.enums.BoosterType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Booster {
    protected String id;
    protected BoosterType type;
    protected String name;
    protected String description;
    protected String iconUrl;
    protected int cooldownSeconds;
    protected int duration;
    protected int maxUses;
    protected int usesRemaining;
    protected boolean isActive;
    protected long activatedAt;
    protected String playerId;
    
    public abstract void activate();
    public abstract void deactivate();
    public abstract boolean canUse();
    
    public void use() {
        if (canUse()) {
            usesRemaining--;
            isActive = true;
            activatedAt = System.currentTimeMillis();
            activate();
        }
    }
    
    public boolean isExpired() {
        if (!isActive) return false;
        return System.currentTimeMillis() - activatedAt > duration * 1000L;
    }
    
    public void reset() {
        isActive = false;
        activatedAt = 0;
        deactivate();
    }
}