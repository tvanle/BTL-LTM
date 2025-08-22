package com.crossword.repository;

import com.crossword.model.GameState;
import org.springframework.stereotype.Repository;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class GameRepository {
    
    private final Map<String, GameState> games = new ConcurrentHashMap<>();
    
    public GameState save(GameState gameState) {
        games.put(gameState.getGameId(), gameState);
        return gameState;
    }
    
    public GameState findById(String gameId) {
        return games.get(gameId);
    }
    
    public void deleteById(String gameId) {
        games.remove(gameId);
    }
    
    public boolean existsById(String gameId) {
        return games.containsKey(gameId);
    }
    
    public Map<String, GameState> findAll() {
        return new ConcurrentHashMap<>(games);
    }
    
    public void deleteAll() {
        games.clear();
    }
    
    public int count() {
        return games.size();
    }
}