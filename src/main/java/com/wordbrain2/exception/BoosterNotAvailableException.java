package com.wordbrain2.exception;

import com.wordbrain2.model.enums.BoosterType;

public class BoosterNotAvailableException extends GameException {
    public BoosterNotAvailableException(BoosterType boosterType) {
        super("Booster not available: " + boosterType, "BOOSTER_NOT_AVAILABLE");
    }
    
    public BoosterNotAvailableException(BoosterType boosterType, String reason) {
        super("Booster " + boosterType + " not available: " + reason, "BOOSTER_NOT_AVAILABLE", boosterType);
    }
}