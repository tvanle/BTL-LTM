package com.wordbrain2.service.core;

import com.wordbrain2.config.GameConfig;
import com.wordbrain2.model.entity.Player;
import com.wordbrain2.model.entity.Room;
import com.wordbrain2.model.enums.RoomStatus;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Service
public class RoomService {
    
    private final Map<String, Room> rooms = new ConcurrentHashMap<>();
    private final GameConfig gameConfig;
    
    public RoomService(GameConfig gameConfig) {
        this.gameConfig = gameConfig;
    }
    
    public Map<String, Object> createRoom(String playerName, String topic, String sessionId) {
        Player host = new Player(playerName, sessionId);
        Room room = new Room(host.getId(), topic);
        
        room.addPlayer(host);
        rooms.put(room.getRoomCode(), room);
        
        log.info("Room created: {} by player: {}", room.getRoomCode(), playerName);
        
        return Map.of(
            "roomCode", room.getRoomCode(),
            "playerId", host.getId(),
            "playerName", playerName,
            "topic", topic,
            "isHost", true
        );
    }
    
    public Map<String, Object> joinRoom(String roomCode, String playerName, String sessionId) {
        Room room = rooms.get(roomCode);
        if (room == null) {
            log.warn("Room not found: {}", roomCode);
            return null;
        }
        
        if (room.isFull()) {
            log.warn("Room is full: {}", roomCode);
            return null;
        }
        
        if (room.getStatus() != RoomStatus.WAITING) {
            log.warn("Room is not accepting players: {}", roomCode);
            return null;
        }
        
        Player player = new Player(playerName, sessionId);
        if (room.addPlayer(player)) {
            log.info("Player {} joined room {}", playerName, roomCode);
            
            return Map.of(
                "roomCode", roomCode,
                "playerId", player.getId(),
                "playerName", playerName,
                "topic", room.getTopic(),
                "isHost", false,
                "players", room.getPlayerCount()
            );
        }
        
        return null;
    }
    
    public void removePlayer(String roomCode, String playerId) {
        Room room = rooms.get(roomCode);
        if (room != null) {
            room.removePlayer(playerId);
            
            if (room.getStatus() == RoomStatus.CLOSED) {
                rooms.remove(roomCode);
                log.info("Room {} closed - no players remaining", roomCode);
            }
        }
    }
    
    public void setPlayerReady(String roomCode, String playerId, boolean ready) {
        Room room = rooms.get(roomCode);
        if (room != null) {
            room.setPlayerReady(playerId, ready);
            log.debug("Player {} ready status: {} in room {}", playerId, ready, roomCode);
        }
    }
    
    public Room getRoom(String roomCode) {
        return rooms.get(roomCode);
    }
    
    public boolean roomExists(String roomCode) {
        return rooms.containsKey(roomCode);
    }
    
    public Map<String, Room> getAllRooms() {
        return new ConcurrentHashMap<>(rooms);
    }
}