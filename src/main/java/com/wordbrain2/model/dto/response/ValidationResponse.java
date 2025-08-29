package com.wordbrain2.model.dto.response;

import com.wordbrain2.model.enums.SubmissionResult;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidationResponse {
    
    private boolean isValid;
    private SubmissionResult result;
    private String word;
    private int pointsEarned;
    private String message;
    private List<String> errors;
    private int streakCount;
    private boolean streakBroken;
    private long responseTime;
    
    // Static factory methods
    public static ValidationResponse valid(String word, int points) {
        return ValidationResponse.builder()
            .isValid(true)
            .result(SubmissionResult.CORRECT)
            .word(word)
            .pointsEarned(points)
            .message("Word accepted!")
            .build();
    }
    
    public static ValidationResponse invalid(String word, String message) {
        return ValidationResponse.builder()
            .isValid(false)
            .result(SubmissionResult.INCORRECT)
            .word(word)
            .message(message)
            .build();
    }
    
    public static ValidationResponse partial(String word, String message) {
        return ValidationResponse.builder()
            .isValid(false)
            .result(SubmissionResult.PARTIAL)
            .word(word)
            .message(message)
            .build();
    }
}