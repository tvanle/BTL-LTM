package com.wordbrain2.controller.api;

import com.wordbrain2.model.dto.response.LeaderboardResponse;
import com.wordbrain2.model.scoring.Score;
import com.wordbrain2.service.scoring.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/leaderboard")
@CrossOrigin(origins = "*")
public class LeaderboardController {
    @Autowired
    private LeaderboardService leaderboardService;
    
    @GetMapping("/room/{roomCode}")
    public ResponseEntity<?> getRoomLeaderboard(@PathVariable String roomCode,
                                               @RequestParam(defaultValue = "10") int limit) {
        List<Score> topScores = leaderboardService.getTopPlayers(roomCode, limit);
        Map<String, Object> leaderboardData = leaderboardService.getLeaderboardData(roomCode);
        
        LeaderboardResponse response = LeaderboardResponse.builder()
            .roomCode(roomCode)
            .scores(topScores)
            .currentLevel((Integer) leaderboardData.get("currentLevel"))
            .totalLevels((Integer) leaderboardData.get("totalLevels"))
            .timestamp(System.currentTimeMillis())
            .gameEnded(false)
            .build();
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/global")
    public ResponseEntity<?> getGlobalLeaderboard(@RequestParam(defaultValue = "20") int limit) {
        List<Score> topScores = leaderboardService.getGlobalTopPlayers(limit);
        return ResponseEntity.ok(topScores);
    }
    
    @GetMapping("/player/{playerId}/rank")
    public ResponseEntity<?> getPlayerRank(@PathVariable String playerId,
                                          @RequestParam String roomCode) {
        int rank = leaderboardService.getPlayerRank(roomCode, playerId);
        if (rank == -1) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("playerId", playerId);
        response.put("roomCode", roomCode);
        response.put("rank", rank);
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/room/{roomCode}/winner")
    public ResponseEntity<?> getWinner(@PathVariable String roomCode) {
        String winner = leaderboardService.getWinner(roomCode);
        if (winner == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> response = new HashMap<>();
        response.put("roomCode", roomCode);
        response.put("winner", winner);
        
        return ResponseEntity.ok(response);
    }
}