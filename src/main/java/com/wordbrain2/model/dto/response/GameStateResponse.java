package com.wordbrain2.model.dto.response;

import com.wordbrain2.model.game.Grid;
import com.wordbrain2.model.scoring.Score;
import com.wordbrain2.model.enums.GamePhase;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GameStateResponse {
    
    private String roomCode;
    private GamePhase phase;
    private int currentLevel;
    private int totalLevels;
    private long timeRemaining;
    private Grid currentGrid;
    private List<Score> leaderboard;
    private Map<String, String> playerStatuses;
    private String currentTurn;
    private boolean isPaused;
    @Builder.Default
    private long serverTime = System.currentTimeMillis();
}