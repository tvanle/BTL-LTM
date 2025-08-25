package com.wordbrain2.service.scoring;

import com.wordbrain2.model.scoring.Leaderboard;
import com.wordbrain2.model.scoring.Score;
import com.wordbrain2.model.entity.Player;
import com.wordbrain2.service.core.PlayerService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Service
public class LeaderboardService {
    @Autowired
    private PlayerService playerService;
    
    private final Map<String, Leaderboard> roomLeaderboards = new ConcurrentHashMap<>();
    private final Leaderboard globalLeaderboard = new Leaderboard("GLOBAL");
    
    public Leaderboard getLeaderboard(String roomCode) {
        return roomLeaderboards.computeIfAbsent(roomCode, k -> new Leaderboard(k));
    }
    
    public void updateScore(String roomCode, String playerId, int points) {
        // Update room leaderboard
        Leaderboard roomBoard = getLeaderboard(roomCode);
        Score score = playerService.getPlayerScore(playerId);
        score.setPlayerId(playerId);
        score.addPoints(points);
        roomBoard.updateScore(score);
        
        // Update global leaderboard
        globalLeaderboard.updateScore(score);
    }
    
    public void recordCorrectAnswer(String roomCode, String playerId) {
        Score score = playerService.getPlayerScore(playerId);
        score.incrementCorrect();
        
        Leaderboard board = getLeaderboard(roomCode);
        board.updateScore(score);
    }
    
    public void recordIncorrectAnswer(String roomCode, String playerId) {
        Score score = playerService.getPlayerScore(playerId);
        score.incrementIncorrect();
        
        Leaderboard board = getLeaderboard(roomCode);
        board.updateScore(score);
    }
    
    public List<Score> getTopPlayers(String roomCode, int limit) {
        Leaderboard board = getLeaderboard(roomCode);
        return board.getTopScores(limit);
    }
    
    public List<Score> getGlobalTopPlayers(int limit) {
        return globalLeaderboard.getTopScores(limit);
    }
    
    public int getPlayerRank(String roomCode, String playerId) {
        Leaderboard board = getLeaderboard(roomCode);
        Score playerScore = board.getPlayerScore(playerId);
        return playerScore != null ? playerScore.getRank() : -1;
    }
    
    public Score getPlayerScore(String roomCode, String playerId) {
        Leaderboard board = getLeaderboard(roomCode);
        return board.getPlayerScore(playerId);
    }
    
    public Map<String, Object> getLeaderboardData(String roomCode) {
        Leaderboard board = getLeaderboard(roomCode);
        Map<String, Object> data = new HashMap<>();
        
        data.put("roomCode", roomCode);
        data.put("scores", board.getScores());
        data.put("lastUpdated", board.getLastUpdated());
        data.put("currentLevel", board.getCurrentLevel());
        data.put("totalLevels", board.getTotalLevels());
        
        // Add player names
        List<Map<String, Object>> enrichedScores = new ArrayList<>();
        for (Score score : board.getScores()) {
            Map<String, Object> scoreData = new HashMap<>();
            Player player = playerService.getPlayer(score.getPlayerId());
            
            scoreData.put("rank", score.getRank());
            scoreData.put("playerId", score.getPlayerId());
            scoreData.put("playerName", player != null ? player.getName() : "Unknown");
            scoreData.put("totalPoints", score.getTotalPoints());
            scoreData.put("correctWords", score.getCorrectWords());
            scoreData.put("currentStreak", score.getCurrentStreak());
            scoreData.put("accuracy", score.getAccuracy());
            
            enrichedScores.add(scoreData);
        }
        
        data.put("leaderboard", enrichedScores);
        return data;
    }
    
    public void resetRoomLeaderboard(String roomCode) {
        Leaderboard board = getLeaderboard(roomCode);
        board.setScores(new ArrayList<>());
        board.setLastUpdated(System.currentTimeMillis());
    }
    
    public void removeRoomLeaderboard(String roomCode) {
        roomLeaderboards.remove(roomCode);
    }
    
    public String getWinner(String roomCode) {
        List<Score> topScores = getTopPlayers(roomCode, 1);
        if (!topScores.isEmpty()) {
            String playerId = topScores.get(0).getPlayerId();
            Player player = playerService.getPlayer(playerId);
            return player != null ? player.getName() : "Unknown";
        }
        return null;
    }
}