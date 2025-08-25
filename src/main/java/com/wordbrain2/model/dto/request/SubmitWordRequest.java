package com.wordbrain2.model.dto.request;

import com.wordbrain2.model.game.Cell;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SubmitWordRequest {
    @NotBlank(message = "Room code is required")
    private String roomCode;
    
    @NotBlank(message = "Player ID is required")
    private String playerId;
    
    @NotNull(message = "Path is required")
    @Size(min = 3, message = "Word must be at least 3 characters")
    private List<Cell> path;
    
    @NotBlank(message = "Word is required")
    @Size(min = 3, message = "Word must be at least 3 characters")
    private String word;
    
    private long timestamp;
    
    private int levelNumber;
}