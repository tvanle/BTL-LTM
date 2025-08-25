package com.wordbrain2.util;

import com.wordbrain2.model.game.Cell;
import java.util.*;

public class PathValidator {
    
    public static boolean isValidPath(List<Cell> path) {
        if (path == null || path.size() < 3) {
            return false;
        }
        
        // Check for duplicate cells
        Set<String> visited = new HashSet<>();
        for (Cell cell : path) {
            String key = cell.getRow() + "," + cell.getCol();
            if (visited.contains(key)) {
                return false;
            }
            visited.add(key);
        }
        
        // Check if cells are adjacent
        for (int i = 1; i < path.size(); i++) {
            if (!areAdjacent(path.get(i - 1), path.get(i))) {
                return false;
            }
        }
        
        return true;
    }
    
    public static boolean areAdjacent(Cell c1, Cell c2) {
        int rowDiff = Math.abs(c1.getRow() - c2.getRow());
        int colDiff = Math.abs(c1.getCol() - c2.getCol());
        return rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff) > 0;
    }
    
    public static boolean isConnectedPath(List<Cell> path) {
        if (path.size() < 2) {
            return true;
        }
        
        for (int i = 1; i < path.size(); i++) {
            if (!areAdjacent(path.get(i - 1), path.get(i))) {
                return false;
            }
        }
        
        return true;
    }
    
    public static String extractWord(List<Cell> path) {
        StringBuilder word = new StringBuilder();
        for (Cell cell : path) {
            if (cell.getValue() != null) {
                word.append(cell.getValue());
            }
        }
        return word.toString();
    }
    
    public static boolean pathCoversAllCells(List<Cell> path, Set<Cell> targetCells) {
        Set<String> pathCells = new HashSet<>();
        for (Cell cell : path) {
            pathCells.add(cell.getRow() + "," + cell.getCol());
        }
        
        for (Cell target : targetCells) {
            if (!pathCells.contains(target.getRow() + "," + target.getCol())) {
                return false;
            }
        }
        
        return true;
    }
}