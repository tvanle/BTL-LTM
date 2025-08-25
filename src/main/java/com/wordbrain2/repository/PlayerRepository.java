package com.wordbrain2.repository;

import com.wordbrain2.model.entity.Player;
import org.springframework.stereotype.Repository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Optional;
import java.util.List;
import java.util.ArrayList;
import java.util.stream.Collectors;

@Repository
public class PlayerRepository {
    private final Map<String, Player> players = new ConcurrentHashMap<>();
    
    public Player save(Player player) {
        players.put(player.getId(), player);
        return player;
    }
    
    public Optional<Player> findById(String playerId) {
        return Optional.ofNullable(players.get(playerId));
    }
    
    public List<Player> findAll() {
        return new ArrayList<>(players.values());
    }
    
    public List<Player> findByName(String name) {
        return players.values().stream()
            .filter(player -> player.getName().equalsIgnoreCase(name))
            .collect(Collectors.toList());
    }
    
    public List<Player> findByStatus(String status) {
        return players.values().stream()
            .filter(player -> player.getStatus().name().equals(status))
            .collect(Collectors.toList());
    }
    
    public List<Player> findActivePlayers() {
        return players.values().stream()
            .filter(player -> !player.getStatus().name().equals("DISCONNECTED"))
            .collect(Collectors.toList());
    }
    
    public void deleteById(String playerId) {
        players.remove(playerId);
    }
    
    public boolean existsById(String playerId) {
        return players.containsKey(playerId);
    }
    
    public long count() {
        return players.size();
    }
    
    public void deleteAll() {
        players.clear();
    }
}