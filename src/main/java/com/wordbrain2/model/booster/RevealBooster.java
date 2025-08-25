package com.wordbrain2.model.booster;

import com.wordbrain2.model.enums.BoosterType;
import com.wordbrain2.model.game.Cell;
import lombok.Data;
import lombok.EqualsAndHashCode;
import java.util.List;

@Data
@EqualsAndHashCode(callSuper = true)
public class RevealBooster extends Booster {
    private List<Cell> revealedCells;
    private String revealedWord;
    private int costPoints = 100;
    
    public RevealBooster() {
        this.type = BoosterType.REVEAL;
        this.name = "Reveal Hint";
        this.description = "Reveal one word in the grid";
        this.cooldownSeconds = 0;
        this.duration = 5;
        this.maxUses = 2;
        this.usesRemaining = 2;
    }
    
    @Override
    public void activate() {
        System.out.println("Revealing hint for player: " + playerId);
    }
    
    @Override
    public void deactivate() {
        revealedCells = null;
        revealedWord = null;
    }
    
    @Override
    public boolean canUse() {
        return usesRemaining > 0 && !isActive;
    }
    
    public void setHint(List<Cell> cells, String word) {
        this.revealedCells = cells;
        this.revealedWord = word;
    }
}