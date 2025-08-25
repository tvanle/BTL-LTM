package com.wordbrain2.service.core;

import com.wordbrain2.model.entity.Player;
import com.wordbrain2.model.enums.PlayerStatus;
import com.wordbrain2.model.scoring.Score;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PlayerService {
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    private final Map<String, String> playerRoomMap = new ConcurrentHashMap<>();
    private final Map<String, Score> playerScores = new ConcurrentHashMap<>();
    
    public Player createPlayer(String name, String avatarUrl) {
        String playerId = generatePlayerId();
        Player player = new Player(name, playerId);
        player.setStatus(PlayerStatus.IDLE);
        player.setHost(false);
        
        players.put(playerId, player);
        playerScores.put(playerId, new Score());
        return player;
    }
    
    public Player getPlayer(String playerId) {
        return players.get(playerId);
    }
    
    public void updatePlayerStatus(String playerId, PlayerStatus status) {
        Player player = players.get(playerId);
        if (player != null) {
            player.setStatus(status);
        }
    }
    
    public void setPlayerReady(String playerId, boolean ready) {
        Player player = players.get(playerId);
        if (player != null) {
            // Player doesn't have setReady method, just update status
            if (ready) {
                player.setStatus(PlayerStatus.READY);
            } else {
                player.setStatus(PlayerStatus.IDLE);
            }
        }
    }
    
    public void assignPlayerToRoom(String playerId, String roomCode) {
        playerRoomMap.put(playerId, roomCode);
    }
    
    public void removePlayerFromRoom(String playerId) {
        playerRoomMap.remove(playerId);
    }
    
    public String getPlayerRoom(String playerId) {
        return playerRoomMap.get(playerId);
    }
    
    public Score getPlayerScore(String playerId) {
        return playerScores.computeIfAbsent(playerId, k -> new Score());
    }
    
    public void updatePlayerScore(String playerId, int points) {
        Score score = getPlayerScore(playerId);
        score.addPoints(points);
    }
    
    public void removePlayer(String playerId) {
        players.remove(playerId);
        playerRoomMap.remove(playerId);
        playerScores.remove(playerId);
    }
    
    public List<Player> getPlayersInRoom(String roomCode) {
        List<Player> roomPlayers = new ArrayList<>();
        playerRoomMap.forEach((playerId, room) -> {
            if (room.equals(roomCode)) {
                Player player = players.get(playerId);
                if (player != null) {
                    roomPlayers.add(player);
                }
            }
        });
        return roomPlayers;
    }
    
    private String generatePlayerId() {
        return "player_" + UUID.randomUUID().toString().substring(0, 8);
    }
}