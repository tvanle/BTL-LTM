package com.wordbrain2.exception;

public class PlayerNotFoundException extends GameException {
    public PlayerNotFoundException(String playerId) {
        super("Player not found: " + playerId, "PLAYER_NOT_FOUND");
    }
    
    public PlayerNotFoundException(String playerId, String message) {
        super(message, "PLAYER_NOT_FOUND", playerId);
    }
}