package com.crossword.controller;

import com.crossword.model.GameState;
import com.crossword.model.Player;
import com.crossword.service.GameService;
import com.crossword.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api")
public class GameController {
    
    @Autowired
    private GameService gameService;
    
    @Autowired
    private SessionService sessionService;
    
    @PostMapping("/games")
    public ResponseEntity<Map<String, Object>> createGame() {
        GameState game = gameService.createNewGame();
        
        Map<String, Object> response = new HashMap<>();
        response.put("gameId", game.getGameId());
        response.put("status", game.getStatus());
        response.put("playersCount", game.getPlayers().size());
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/games/{gameId}")
    public ResponseEntity<GameState> getGame(@PathVariable String gameId) {
        GameState game = gameService.findGameById(gameId);
        if (game != null) {
            return ResponseEntity.ok(game);
        }
        return ResponseEntity.notFound().build();
    }
    
    @PostMapping("/games/{gameId}/join")
    public ResponseEntity<Map<String, Object>> joinGame(
            @PathVariable String gameId,
            @RequestBody Map<String, String> request) {
        
        String playerName = request.get("playerName");
        if (playerName == null || playerName.trim().isEmpty()) {
            return ResponseEntity.badRequest().build();
        }
        
        Player player = new Player(UUID.randomUUID().toString(), playerName.trim());
        GameState game = gameService.joinGame(gameId, player);
        
        if (game != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("playerId", player.getId());
            response.put("playerName", player.getName());
            response.put("gameStatus", game.getStatus());
            response.put("playersCount", game.getPlayers().size());
            
            return ResponseEntity.ok(response);
        }
        
        return ResponseEntity.badRequest().build();
    }
    
    @PostMapping("/games/{gameId}/move")
    public ResponseEntity<Map<String, Object>> makeMove(
            @PathVariable String gameId,
            @RequestBody Map<String, Object> request) {
        
        String playerId = (String) request.get("playerId");
        Integer row = (Integer) request.get("row");
        Integer col = (Integer) request.get("col");
        String charStr = (String) request.get("character");
        
        if (playerId == null || row == null || col == null || 
            charStr == null || charStr.length() != 1) {
            return ResponseEntity.badRequest().build();
        }
        
        char character = charStr.charAt(0);
        boolean valid = gameService.validateMove(gameId, playerId, row, col, character);
        
        Map<String, Object> response = new HashMap<>();
        response.put("valid", valid);
        response.put("row", row);
        response.put("col", col);
        response.put("character", character);
        
        if (valid) {
            GameState game = gameService.findGameById(gameId);
            if (game != null) {
                response.put("gameState", game);
            }
        }
        
        return ResponseEntity.ok(response);
    }
    
    @GetMapping("/games/{gameId}/status")
    public ResponseEntity<Map<String, Object>> getGameStatus(@PathVariable String gameId) {
        GameState game = gameService.findGameById(gameId);
        if (game != null) {
            Map<String, Object> response = new HashMap<>();
            response.put("status", game.getStatus());
            response.put("playersCount", game.getPlayers().size());
            response.put("remainingTime", game.getRemainingTimeSeconds());
            response.put("players", game.getPlayers());
            
            return ResponseEntity.ok(response);
        }
        return ResponseEntity.notFound().build();
    }
}