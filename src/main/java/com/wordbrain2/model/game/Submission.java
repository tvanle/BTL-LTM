package com.wordbrain2.model.game;

import com.wordbrain2.model.enums.SubmissionResult;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Submission {
    private String playerId;
    private String playerName;
    private List<Cell> path;
    private String word;
    private long timestamp;
    private SubmissionResult result;
    private int pointsEarned;
    private boolean usedBooster;
    private String boosterType;
    private double responseTime;
    private int levelNumber;
    private String roomCode;
}