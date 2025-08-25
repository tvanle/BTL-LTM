package com.wordbrain2.model.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import javax.validation.constraints.NotBlank;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class StartGameRequest {
    @NotBlank(message = "Room code is required")
    private String roomCode;
    
    @NotBlank(message = "Host ID is required")
    private String hostId;
    
    private int countdownSeconds = 5;
    
    private boolean forceStart = false;
}