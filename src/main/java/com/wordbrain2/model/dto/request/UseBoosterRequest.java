package com.wordbrain2.model.dto.request;

import com.wordbrain2.model.enums.BoosterType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UseBoosterRequest {
    @NotBlank(message = "Room code is required")
    private String roomCode;
    
    @NotBlank(message = "Player ID is required")
    private String playerId;
    
    @NotNull(message = "Booster type is required")
    private BoosterType boosterType;
    
    private String targetPlayerId;
    
    private long timestamp;
}