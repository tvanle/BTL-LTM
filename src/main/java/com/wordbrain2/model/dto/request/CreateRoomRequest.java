package com.wordbrain2.model.dto.request;

import com.wordbrain2.model.enums.BoosterType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Min;
import javax.validation.constraints.Max;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateRoomRequest {
    @NotBlank(message = "Host name is required")
    private String hostName;
    
    private String topic;
    
    @Min(2)
    @Max(20)
    private int maxPlayers = 10;
    
    @Min(1)
    @Max(20)
    private int levelCount = 10;
    
    @Min(10)
    @Max(120)
    private int levelDuration = 30;
    
    private List<BoosterType> enabledBoosters;
    
    private boolean isPrivate = false;
    
    private String password;
    
    private String language = "vietnamese";
}