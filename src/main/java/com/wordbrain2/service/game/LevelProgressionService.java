package com.wordbrain2.service.game;

import com.wordbrain2.model.game.Level;
import com.wordbrain2.model.game.Grid;
import com.wordbrain2.model.game.Shape;
import com.wordbrain2.model.enums.GamePhase;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.*;

@Service
public class LevelProgressionService {
    @Autowired
    private GridGeneratorService gridGeneratorService;
    
    @Autowired
    private DictionaryService dictionaryService;
    
    private final Map<String, List<Level>> roomLevels = new HashMap<>();
    private final Map<String, Integer> currentLevelIndex = new HashMap<>();
    
    public List<Level> generateLevels(String roomCode, String topic, int levelCount) {
        List<Level> levels = new ArrayList<>();
        
        for (int i = 1; i <= levelCount; i++) {
            Level level = createLevel(i, topic, levelCount);
            levels.add(level);
        }
        
        roomLevels.put(roomCode, levels);
        currentLevelIndex.put(roomCode, 0);
        return levels;
    }
    
    public Level getCurrentLevel(String roomCode) {
        List<Level> levels = roomLevels.get(roomCode);
        Integer index = currentLevelIndex.get(roomCode);
        
        if (levels != null && index != null && index < levels.size()) {
            return levels.get(index);
        }
        return null;
    }
    
    public Level getNextLevel(String roomCode) {
        Integer index = currentLevelIndex.get(roomCode);
        if (index != null) {
            currentLevelIndex.put(roomCode, index + 1);
            return getCurrentLevel(roomCode);
        }
        return null;
    }
    
    public boolean hasNextLevel(String roomCode) {
        List<Level> levels = roomLevels.get(roomCode);
        Integer index = currentLevelIndex.get(roomCode);
        
        return levels != null && index != null && (index + 1) < levels.size();
    }
    
    public void resetLevels(String roomCode) {
        currentLevelIndex.put(roomCode, 0);
    }
    
    public void clearRoomLevels(String roomCode) {
        roomLevels.remove(roomCode);
        currentLevelIndex.remove(roomCode);
    }
    
    private Level createLevel(int levelNumber, String topic, int totalLevels) {
        // Calculate difficulty based on level number
        int gridSize = calculateGridSize(levelNumber, totalLevels);
        int wordCount = calculateWordCount(levelNumber);
        int duration = calculateDuration(levelNumber);
        
        // Generate grid for this level
        Grid grid = gridGeneratorService.generateGrid(gridSize, gridSize, levelNumber);
        Shape shape = generateShape(gridSize, levelNumber);
        grid.setShape(shape);
        
        Level level = new Level(levelNumber);
        level.setGrid(grid);
        level.setDuration(duration);
        level.setTargetWordCount(wordCount);
        // The following fields don't exist in Level class:
        // minWordLength, maxWordLength, pointsMultiplier, bonusPoints
        // These should be handled in the scoring/validation services instead
        return level;
    }
    
    private int calculateGridSize(int levelNumber, int totalLevels) {
        // Start with 4x4, increase gradually
        int minSize = 4;
        int maxSize = 8;
        double progress = (double) levelNumber / totalLevels;
        return Math.min(maxSize, minSize + (int)(progress * (maxSize - minSize)));
    }
    
    private int calculateWordCount(int levelNumber) {
        // Start with 1 word, increase every 3 levels
        return 1 + (levelNumber / 3);
    }
    
    private int calculateDuration(int levelNumber) {
        // Start with 30 seconds, add 5 seconds every 2 levels
        return 30 + ((levelNumber - 1) / 2) * 5;
    }
    
    private Shape generateShape(int gridSize, int levelNumber) {
        boolean[][] mask = new boolean[gridSize][gridSize];
        int cellCount = 0;
        
        // Create different shapes based on level
        if (levelNumber <= 3) {
            // Full grid for early levels
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    mask[i][j] = true;
                    cellCount++;
                }
            }
        } else if (levelNumber <= 6) {
            // Diamond shape for mid levels
            int center = gridSize / 2;
            for (int i = 0; i < gridSize; i++) {
                for (int j = 0; j < gridSize; j++) {
                    int distance = Math.abs(i - center) + Math.abs(j - center);
                    if (distance <= center) {
                        mask[i][j] = true;
                        cellCount++;
                    }
                }
            }
        } else {
            // Random complex shape for higher levels
            Random random = new Random();
            int targetCells = (gridSize * gridSize * 3) / 4; // 75% of grid
            while (cellCount < targetCells) {
                int row = random.nextInt(gridSize);
                int col = random.nextInt(gridSize);
                if (!mask[row][col]) {
                    mask[row][col] = true;
                    cellCount++;
                }
            }
        }
        
        Shape shape = new Shape(gridSize, gridSize, Shape.ShapeType.CUSTOM);
        shape.setMask(mask);
        shape.setCellCount(cellCount);
        return shape;
    }
    
    public int getCurrentLevelNumber(String roomCode) {
        Integer index = currentLevelIndex.get(roomCode);
        return index != null ? index + 1 : 0;
    }
    
    public int getTotalLevels(String roomCode) {
        List<Level> levels = roomLevels.get(roomCode);
        return levels != null ? levels.size() : 0;
    }
}