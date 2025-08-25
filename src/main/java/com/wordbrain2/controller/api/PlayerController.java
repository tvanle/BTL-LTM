package com.wordbrain2.controller.api;

import com.wordbrain2.model.entity.Player;
import com.wordbrain2.model.scoring.Score;
import com.wordbrain2.service.core.PlayerService;
import com.wordbrain2.service.scoring.LeaderboardService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/players")
@CrossOrigin(origins = "*")
public class PlayerController {
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private LeaderboardService leaderboardService;
    
    @GetMapping("/{playerId}")
    public ResponseEntity<?> getPlayer(@PathVariable String playerId) {
        Player player = playerService.getPlayer(playerId);
        if (player == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(player);
    }
    
    @GetMapping("/{playerId}/score")
    public ResponseEntity<?> getPlayerScore(@PathVariable String playerId, 
                                           @RequestParam(required = false) String roomCode) {
        if (roomCode != null) {
            Score score = leaderboardService.getPlayerScore(roomCode, playerId);
            return ResponseEntity.ok(score);
        } else {
            Score score = playerService.getPlayerScore(playerId);
            return ResponseEntity.ok(score);
        }
    }
    
    @GetMapping("/{playerId}/statistics")
    public ResponseEntity<?> getPlayerStatistics(@PathVariable String playerId) {
        Player player = playerService.getPlayer(playerId);
        if (player == null) {
            return ResponseEntity.notFound().build();
        }
        
        Map<String, Object> stats = new HashMap<>();
        stats.put("totalScore", player.getTotalScore());
        stats.put("currentStreak", player.getCurrentStreak());
        stats.put("maxStreak", player.getMaxStreak());
        stats.put("correctWords", player.getCorrectWords());
        stats.put("boostersUsed", player.getBoostersUsed());
        return ResponseEntity.ok(stats);
    }
    
    @PutMapping("/{playerId}/ready")
    public ResponseEntity<?> setPlayerReady(@PathVariable String playerId,
                                           @RequestParam boolean ready) {
        playerService.setPlayerReady(playerId, ready);
        Map<String, Object> response = new HashMap<>();
        response.put("playerId", playerId);
        response.put("ready", ready);
        response.put("success", true);
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/{playerId}/room")
    public ResponseEntity<?> getPlayerRoom(@PathVariable String playerId) {
        String roomCode = playerService.getPlayerRoom(playerId);
        if (roomCode == null) {
            return ResponseEntity.notFound().build();
        }
        Map<String, Object> response = new HashMap<>();
        response.put("playerId", playerId);
        response.put("roomCode", roomCode);
        return ResponseEntity.ok(response);
    }
}