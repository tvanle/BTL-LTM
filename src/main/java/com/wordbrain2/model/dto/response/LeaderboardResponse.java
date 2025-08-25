package com.wordbrain2.model.dto.response;

import com.wordbrain2.model.scoring.Score;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LeaderboardResponse {
    private String roomCode;
    private List<Score> scores;
    private int currentLevel;
    private int totalLevels;
    private int timeRemaining;
    private String winnerId;
    private String winnerName;
    private long timestamp;
    private boolean gameEnded;
}