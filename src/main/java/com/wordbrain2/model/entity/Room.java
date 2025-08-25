package com.wordbrain2.model.entity;

import com.wordbrain2.model.enums.BoosterType;
import com.wordbrain2.model.enums.RoomStatus;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Data
public class Room {
    private String roomCode;
    private String hostId;
    private String topic;
    private int maxPlayers;
    private int levelCount;
    private int levelDuration;
    private RoomStatus status;
    private GameSession gameSession;
    private Map<String, Player> players;
    private Map<String, Boolean> playerReady;
    private List<BoosterType> allowedBoosters;
    private LocalDateTime createdAt;
    private LocalDateTime lastActivityAt;
    
    public Room() {
        this.roomCode = generateRoomCode();
        this.status = RoomStatus.WAITING;
        this.players = new ConcurrentHashMap<>();
        this.playerReady = new ConcurrentHashMap<>();
        this.allowedBoosters = new ArrayList<>(Arrays.asList(BoosterType.values()));
        this.createdAt = LocalDateTime.now();
        this.lastActivityAt = LocalDateTime.now();
        this.maxPlayers = 20;
        this.levelCount = 10;
        this.levelDuration = 30;
    }
    
    public Room(String hostId, String topic) {
        this();
        this.hostId = hostId;
        this.topic = topic;
    }
    
    private String generateRoomCode() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        Random random = new Random();
        
        for (int i = 0; i < 6; i++) {
            code.append(chars.charAt(random.nextInt(chars.length())));
        }
        
        return code.toString();
    }
    
    public boolean addPlayer(Player player) {
        if (players.size() >= maxPlayers) {
            return false;
        }
        
        players.put(player.getId(), player);
        playerReady.put(player.getId(), false);
        player.setRoomCode(this.roomCode);
        updateActivity();
        return true;
    }
    
    public void removePlayer(String playerId) {
        players.remove(playerId);
        playerReady.remove(playerId);
        updateActivity();
        
        if (players.isEmpty()) {
            this.status = RoomStatus.CLOSED;
        } else if (playerId.equals(hostId) && !players.isEmpty()) {
            // Assign new host
            this.hostId = players.keySet().iterator().next();
        }
    }
    
    public void setPlayerReady(String playerId, boolean ready) {
        playerReady.put(playerId, ready);
        updateActivity();
        checkAllReady();
    }
    
    private void checkAllReady() {
        if (players.size() >= 2 && playerReady.values().stream().allMatch(ready -> ready)) {
            this.status = RoomStatus.READY;
        }
    }
    
    public void updateActivity() {
        this.lastActivityAt = LocalDateTime.now();
    }
    
    public boolean isFull() {
        return players.size() >= maxPlayers;
    }
    
    public int getPlayerCount() {
        return players.size();
    }
    
    public boolean isPrivate() {
        // Room is private if it has a password or is not publicly listed
        // For now, we'll consider all rooms as public
        return false;
    }
    
    public List<Player> getPlayers() {
        return new ArrayList<>(players.values());
    }
}