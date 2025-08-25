package com.wordbrain2.model.scoring;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.stream.Collectors;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Leaderboard {
    private String roomCode;
    private List<Score> scores;
    private long lastUpdated;
    private int currentLevel;
    private int totalLevels;
    
    public Leaderboard(String roomCode) {
        this.roomCode = roomCode;
        this.scores = new ArrayList<>();
        this.lastUpdated = System.currentTimeMillis();
    }
    
    public void updateScore(Score score) {
        scores.removeIf(s -> s.getPlayerId().equals(score.getPlayerId()));
        scores.add(score);
        sortAndRank();
        this.lastUpdated = System.currentTimeMillis();
    }
    
    public void sortAndRank() {
        scores.sort(Comparator.comparing(Score::getTotalPoints).reversed());
        for (int i = 0; i < scores.size(); i++) {
            scores.get(i).setRank(i + 1);
        }
    }
    
    public List<Score> getTopScores(int limit) {
        return scores.stream()
            .limit(limit)
            .collect(Collectors.toList());
    }
    
    public Score getPlayerScore(String playerId) {
        return scores.stream()
            .filter(s -> s.getPlayerId().equals(playerId))
            .findFirst()
            .orElse(null);
    }
}