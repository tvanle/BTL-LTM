package com.wordbrain2.controller.api;

import com.wordbrain2.model.entity.Player;
import com.wordbrain2.model.entity.Room;
import com.wordbrain2.service.core.MatchmakingService;
import com.wordbrain2.service.core.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/rooms")
@CrossOrigin(origins = "*")
public class RoomController {
    
    private final RoomService roomService;
    private final MatchmakingService matchmakingService;
    
    public RoomController(RoomService roomService, MatchmakingService matchmakingService) {
        this.roomService = roomService;
        this.matchmakingService = matchmakingService;
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
                "status", room.getStatus().toString(),
                // expose current host to allow clients to toggle Start button correctly
                "hostId", room.getHostId()
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
    
    @PostMapping("quickmatch/")
    public ResponseEntity<?> quickMatch(@RequestBody Map<String, String> request) {
        String playerName = request.get("playerName");
        String sessionId = request.get("sessionId");
        
        if (playerName == null) {
            return ResponseEntity.badRequest().body(Map.of("error", "Player name required"));
        }
        
        Player player = new Player(playerName, sessionId != null ? sessionId : "");
        Optional<Room> room = matchmakingService.quickMatch(player);
        
        if (room.isPresent()) {
            Room foundRoom = room.get();
            var joinResult = roomService.joinRoom(foundRoom.getRoomCode(), playerName, sessionId);
            if (joinResult != null) {
                return ResponseEntity.ok(joinResult);
            }
        }
        
        // No available room found, create new one
        var createResult = roomService.createRoom(playerName, "general", sessionId != null ? sessionId : "");
        return ResponseEntity.ok(createResult);
    }
    
    @GetMapping("/find")
    public ResponseEntity<?> findRoom(@RequestParam String topic, @RequestParam(defaultValue = "0") int skillLevel) {
        Optional<Room> room = matchmakingService.findAvailableRoom(topic, skillLevel);
        
        if (room.isPresent()) {
            Room foundRoom = room.get();
            return ResponseEntity.ok(Map.of(
                "roomCode", foundRoom.getRoomCode(),
                "topic", foundRoom.getTopic(),
                "players", foundRoom.getPlayerCount(),
                "maxPlayers", foundRoom.getMaxPlayers()
            ));
        }
        
        return ResponseEntity.notFound().build();
    }
}