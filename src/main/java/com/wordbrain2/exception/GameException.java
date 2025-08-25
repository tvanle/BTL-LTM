package com.wordbrain2.exception;

public class GameException extends RuntimeException {
    private String errorCode;
    private Object details;
    
    public GameException(String message) {
        super(message);
    }
    
    public GameException(String message, Throwable cause) {
        super(message, cause);
    }
    
    public GameException(String message, String errorCode) {
        super(message);
        this.errorCode = errorCode;
    }
    
    public GameException(String message, String errorCode, Object details) {
        super(message);
        this.errorCode = errorCode;
        this.details = details;
    }
    
    public String getErrorCode() {
        return errorCode;
    }
    
    public Object getDetails() {
        return details;
    }
}