package com.wordbrain2.service.core;

import com.wordbrain2.config.GameConfig;
import com.wordbrain2.model.entity.Player;
import com.wordbrain2.model.entity.Room;
import com.wordbrain2.model.enums.RoomStatus;
import com.wordbrain2.model.enums.BoosterType;
import com.wordbrain2.model.dto.request.CreateRoomRequest;
import com.wordbrain2.model.dto.response.RoomResponse;
import com.wordbrain2.model.entity.GameSession;
import com.wordbrain2.util.RandomUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.stream.Collectors;
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
    
    public Optional<Room> findByCode(String roomCode) {
        return Optional.ofNullable(rooms.get(roomCode));
    }
    
    public boolean roomExists(String roomCode) {
        return rooms.containsKey(roomCode);
    }
    
    public Map<String, Room> getAllRooms() {
        return new ConcurrentHashMap<>(rooms);
    }
    
    
    public boolean areAllPlayersReady(String roomCode) {
        Room room = rooms.get(roomCode);
        if (room != null) {
            return room.getPlayerReady().values().stream().allMatch(ready -> ready);
        }
        return false;
    }
    
    public RoomResponse getRoomInfo(String roomCode) {
        Room room = rooms.get(roomCode);
        if (room != null) {
            RoomResponse response = new RoomResponse();
            response.setRoomCode(room.getRoomCode());
            response.setHostId(room.getHostId());
            response.setTopic(room.getTopic());
            response.setMaxPlayers(room.getMaxPlayers());
            response.setCurrentPlayers(room.getPlayers().size());
            response.setStatus(room.getStatus());
            response.setPlayers(room.getPlayers());
            response.setPlayerReady(room.getPlayerReady());
            response.setEnabledBoosters(room.getAllowedBoosters());
            response.setSuccess(true);
            return response;
        }
        return null;
    }
    
    public String generateRoomCode() {
        return RandomUtils.generateRoomCode(6);
    }
    
    public boolean isRoomInGame(String roomCode) {
        Room room = rooms.get(roomCode);
        return room != null && room.getStatus() == RoomStatus.IN_GAME;
    }
    
    public boolean isBoosterEnabled(String roomCode, BoosterType type) {
        Room room = rooms.get(roomCode);
        return room != null && room.getAllowedBoosters() != null && 
               room.getAllowedBoosters().contains(type);
    }
    
    public Map<String, Object> getRoomState(String roomCode) {
        Room room = rooms.get(roomCode);
        if (room == null) {
            return null;
        }
        
        Map<String, Object> state = new HashMap<>();
        state.put("roomCode", roomCode);
        state.put("status", room.getStatus());
        state.put("players", room.getPlayers());
        state.put("maxPlayers", room.getMaxPlayers());
        state.put("levelCount", room.getLevelCount());
        state.put("levelDuration", room.getLevelDuration());
        state.put("topic", room.getTopic());
        state.put("hostId", room.getHostId());
        
        if (room.getGameSession() != null) {
            GameSession session = room.getGameSession();
            state.put("gamePhase", session.getPhase());
            state.put("currentLevel", session.getCurrentLevelIndex());
        }
        
        return state;
    }
    
    /**
     * Find which room a player is in
     * @param playerId the player ID to search for
     * @return room code if found, null otherwise
     */
    public String getPlayerRoom(String playerId) {
        if (playerId == null) {
            return null;
        }
        
        for (Room room : rooms.values()) {
            if (room.getPlayers().stream().anyMatch(p -> p.getId().equals(playerId))) {
                return room.getRoomCode();
            }
        }
        
        return null;
    }
    
    /**
     * Get all players in a room
     * @param roomCode the room code
     * @return list of player IDs
     */
    public List<String> getPlayersInRoom(String roomCode) {
        Room room = rooms.get(roomCode);
        if (room == null) {
            return List.of();
        }
        
        return room.getPlayers().stream()
            .map(Player::getId)
            .collect(Collectors.toList());
    }
}