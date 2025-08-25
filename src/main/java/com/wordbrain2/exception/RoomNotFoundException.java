package com.wordbrain2.exception;

public class RoomNotFoundException extends GameException {
    public RoomNotFoundException(String roomCode) {
        super("Room not found: " + roomCode, "ROOM_NOT_FOUND");
    }
    
    public RoomNotFoundException(String roomCode, String message) {
        super(message, "ROOM_NOT_FOUND", roomCode);
    }
}