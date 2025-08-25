package com.wordbrain2.model.dto.request;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class JoinRoomRequest {
    @NotBlank(message = "Room code is required")
    @Pattern(regexp = "^[A-Z0-9]{6}$", message = "Invalid room code format")
    private String roomCode;
    
    @NotBlank(message = "Player name is required")
    private String playerName;
    
    private String password;
    
    private String avatarUrl;
}