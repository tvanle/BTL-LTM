package com.wordbrain2.controller.api;

import com.wordbrain2.model.dto.request.SubmitWordRequest;
import com.wordbrain2.model.dto.request.UseBoosterRequest;
import com.wordbrain2.service.core.GameEngine;
import com.wordbrain2.service.core.RoomService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import javax.validation.Valid;
import java.util.Map;
import java.util.HashMap;

@RestController
@RequestMapping("/api/game")
@CrossOrigin(origins = "*")
public class GameController {
    @Autowired
    private GameEngine gameEngine;
    
    @Autowired
    private RoomService roomService;
    
    @GetMapping("/{roomCode}/state")
    public ResponseEntity<?> getGameState(@PathVariable String roomCode) {
        // Get game state from room service
        Map<String, Object> state = roomService.getRoomState(roomCode);
        if (state == null) {
            return ResponseEntity.notFound().build();
        }
        return ResponseEntity.ok(state);
    }
    
    @PostMapping("/submit-word")
    public ResponseEntity<?> submitWord(@Valid @RequestBody SubmitWordRequest request) {
        Map<String, Object> result = gameEngine.submitWord(
            request.getRoomCode(),
            request.getPlayerId(),
            request.getPath(),
            request.getWord()
        );
        
        if (result != null && result.get("result") != null) {
            return ResponseEntity.ok(result);
        } else {
            return ResponseEntity.badRequest().body(result);
        }
    }
    
    @PostMapping("/use-booster")
    public ResponseEntity<?> useBooster(@Valid @RequestBody UseBoosterRequest request) {
        Map<String, Object> boosterData = new HashMap<>();
        boosterData.put("boosterType", request.getBoosterType());
        
        Map<String, Object> result = gameEngine.useBooster(
            request.getRoomCode(),
            request.getPlayerId(),
            boosterData
        );
        
        if (result != null && Boolean.TRUE.equals(result.get("success"))) {
            return ResponseEntity.ok(result);
        } else {
            Map<String, Object> response = new HashMap<>();
            response.put("success", false);
            response.put("message", "Failed to use booster");
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    @PostMapping("/{roomCode}/pause")
    public ResponseEntity<?> pauseGame(@PathVariable String roomCode,
                                      @RequestParam String hostId) {
        Map<String, Object> pauseResult = gameEngine.pauseGame(roomCode, hostId);
        boolean success = pauseResult != null;
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("roomCode", roomCode);
        response.put("paused", success);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{roomCode}/resume")
    public ResponseEntity<?> resumeGame(@PathVariable String roomCode,
                                       @RequestParam String hostId) {
        Map<String, Object> resumeResult = gameEngine.resumeGame(roomCode, hostId);
        boolean success = resumeResult != null;
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("roomCode", roomCode);
        response.put("resumed", success);
        return ResponseEntity.ok(response);
    }
    
    @PostMapping("/{roomCode}/end")
    public ResponseEntity<?> endGame(@PathVariable String roomCode,
                                    @RequestParam String hostId) {
        Map<String, Object> endResult = gameEngine.endGame(roomCode, hostId);
        boolean success = endResult != null;
        Map<String, Object> response = new HashMap<>();
        response.put("success", success);
        response.put("roomCode", roomCode);
        response.put("ended", success);
        return ResponseEntity.ok(response);
    }
}