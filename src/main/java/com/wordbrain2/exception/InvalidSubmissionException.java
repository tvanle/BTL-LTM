package com.wordbrain2.exception;

public class InvalidSubmissionException extends GameException {
    public InvalidSubmissionException(String reason) {
        super("Invalid submission: " + reason, "INVALID_SUBMISSION");
    }
    
    public InvalidSubmissionException(String word, String reason) {
        super("Invalid submission for word '" + word + "': " + reason, "INVALID_SUBMISSION", word);
    }
}