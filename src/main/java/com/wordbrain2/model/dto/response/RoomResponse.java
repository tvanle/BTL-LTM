package com.wordbrain2.model.dto.response;

import com.wordbrain2.model.entity.Player;
import com.wordbrain2.model.enums.RoomStatus;
import com.wordbrain2.model.enums.BoosterType;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class RoomResponse {
    private String roomCode;
    private String hostId;
    private String hostName;
    private String topic;
    private int maxPlayers;
    private int currentPlayers;
    private int levelCount;
    private int levelDuration;
    private RoomStatus status;
    private List<Player> players;
    private Map<String, Boolean> playerReady;
    private List<BoosterType> enabledBoosters;
    private boolean isPrivate;
    private long createdAt;
    private String message;
    private boolean success;
}