package com.wordbrain2.controller.api;

import com.wordbrain2.model.entity.Room;
import com.wordbrain2.service.core.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {
    
    private final RoomService roomService;
    
    public RoomController(RoomService roomService) {
        this.roomService = roomService;
    }
    
    @PostMapping("/create")
    public ResponseEntity<?> createRoom(@RequestBody Map<String, String> request) {
        String playerName = request.get("playerName");
        String topic = request.get("topic");
        String sessionId = request.get("sessionId");
        
        if (playerName == null || topic == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
        }
        
        var result = roomService.createRoom(playerName, topic, sessionId != null ? sessionId : "");
        
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        
        return ResponseEntity.badRequest().body(Map.of("error", "Failed to create room"));
    }
    
    @PostMapping("/join")
    public ResponseEntity<?> joinRoom(@RequestBody Map<String, String> request) {
        String roomCode = request.get("roomCode");
        String playerName = request.get("playerName");
        String sessionId = request.get("sessionId");
        
        if (roomCode == null || playerName == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Missing required fields"));
        }
        
        // Validate room code format (must be 6 uppercase characters)
        if (roomCode.length() != 6 || !roomCode.matches("[A-Z0-9]{6}")) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid room code format"));
        }
        
        // Check if room exists
        if (!roomService.roomExists(roomCode)) {
            return ResponseEntity.badRequest().body(Map.of("error", "Room does not exist"));
        }
        
        var result = roomService.joinRoom(roomCode, playerName, sessionId != null ? sessionId : "");
        
        if (result != null) {
            return ResponseEntity.ok(result);
        }
        
        return ResponseEntity.badRequest().body(Map.of("error", "Failed to join room - Room may be full or already started"));
    }
    
    @GetMapping("/{roomCode}")
    public ResponseEntity<?> getRoomInfo(@PathVariable String roomCode) {
        Room room = roomService.getRoom(roomCode);
        
        if (room != null) {
            return ResponseEntity.ok(Map.of(
                "roomCode", room.getRoomCode(),
                "topic", room.getTopic(),
                "players", room.getPlayerCount(),
                "maxPlayers", room.getMaxPlayers(),
                "status", room.getStatus().toString()
            ));
        }
        
        return ResponseEntity.notFound().build();
    }
    
    @GetMapping
    public ResponseEntity<?> listRooms() {
        var rooms = roomService.getAllRooms().values().stream()
            .filter(room -> room.getPlayerCount() > 0)
            .map(room -> Map.of(
                "roomCode", room.getRoomCode(),
                "topic", room.getTopic(),
                "players", room.getPlayerCount(),
                "maxPlayers", room.getMaxPlayers(),
                "status", room.getStatus().toString()
            ))
            .collect(Collectors.toList());
        
        return ResponseEntity.ok(rooms);
    }
}