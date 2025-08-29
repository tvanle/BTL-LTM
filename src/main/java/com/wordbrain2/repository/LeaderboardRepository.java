package com.wordbrain2.repository;

import com.wordbrain2.model.scoring.Leaderboard;
import com.wordbrain2.model.scoring.Score;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Repository
public class LeaderboardRepository {
    
    private final Map<String, Leaderboard> leaderboards = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Score>> scoresByRoom = new ConcurrentHashMap<>();
    
    public Leaderboard save(Leaderboard leaderboard) {
        leaderboards.put(leaderboard.getRoomCode(), leaderboard);
        return leaderboard;
    }
    
    public Optional<Leaderboard> findByRoomCode(String roomCode) {
        return Optional.ofNullable(leaderboards.get(roomCode));
    }
    
    public void saveScore(String roomCode, Score score) {
        scoresByRoom.computeIfAbsent(roomCode, k -> new ConcurrentHashMap<>())
            .put(score.getPlayerId(), score);
    }
    
    public Optional<Score> findScore(String roomCode, String playerId) {
        Map<String, Score> roomScores = scoresByRoom.get(roomCode);
        if (roomScores != null) {
            return Optional.ofNullable(roomScores.get(playerId));
        }
        return Optional.empty();
    }
    
    public List<Score> findScoresByRoomCode(String roomCode) {
        Map<String, Score> roomScores = scoresByRoom.get(roomCode);
        if (roomScores != null) {
            return roomScores.values().stream()
                .sorted((a, b) -> Integer.compare(b.getTotalPoints(), a.getTotalPoints()))
                .collect(Collectors.toList());
        }
        return List.of();
    }
    
    public void updateScore(String roomCode, String playerId, int points) {
        Map<String, Score> roomScores = scoresByRoom.get(roomCode);
        if (roomScores != null) {
            Score score = roomScores.get(playerId);
            if (score != null) {
                score.addPoints(points);
            }
        }
    }
    
    public void deleteByRoomCode(String roomCode) {
        leaderboards.remove(roomCode);
        scoresByRoom.remove(roomCode);
    }
    
    public void clear() {
        leaderboards.clear();
        scoresByRoom.clear();
    }
}