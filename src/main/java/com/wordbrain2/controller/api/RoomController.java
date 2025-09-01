package com.wordbrain2.controller.api;

import com.wordbrain2.model.entity.Room;
import com.wordbrain2.service.core.MatchmakingService;
import com.wordbrain2.service.core.RoomService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * REST Controller for Room information queries only.
 * Room creation and joining are handled via WebSocket.
 */
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
    
    // CREATE and JOIN endpoints removed - now handled via WebSocket
    // Use WebSocket messages CREATE_ROOM and JOIN_ROOM instead
    
    /**
     * Get room information by room code
     * Used for refreshing room state in UI
     */
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
    
    /**
     * List all active rooms
     * Used for room browser/discovery
     */
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
    
    // QUICKMATCH endpoint removed - implement via WebSocket if needed
    
    /**
     * Find available room by topic and skill level
     * Used for matchmaking suggestions
     */
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