package com.wordbrain2.service.game;

import com.wordbrain2.model.game.Cell;
import com.wordbrain2.model.game.Grid;
import com.wordbrain2.model.game.Path;
import com.wordbrain2.model.game.Shape;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class PathValidatorService {
    
    public boolean isValidPath(Path path, Grid grid) {
        List<Cell> cells = path.getCells();
        
        if (cells.isEmpty()) {
            return false;
        }
        
        return areAllCellsValid(cells, grid) &&
               hasNoDuplicates(cells) &&
               isWithinShape(cells, grid.getShape());
    }
    
    public boolean isValidPath(List<Cell> cells, Grid grid) {
        if (cells.isEmpty()) {
            return false;
        }
        
        return areAllCellsValid(cells, grid) &&
               hasNoDuplicates(cells) &&
               isWithinShape(cells, grid.getShape());
    }
    
    public boolean areAllCellsValid(List<Cell> cells, Grid grid) {
        return cells.stream()
            .allMatch(cell -> isValidCell(cell, grid));
    }
    
    public boolean isValidCell(Cell cell, Grid grid) {
        int row = cell.getRow();
        int col = cell.getCol();
        
        return row >= 0 && row < grid.getRows() &&
               col >= 0 && col < grid.getCols() &&
               grid.getCells()[row][col] != null;
    }
    
    public boolean areAllCellsAdjacent(List<Cell> cells) {
        for (int i = 1; i < cells.size(); i++) {
            if (!areAdjacent(cells.get(i - 1), cells.get(i))) {
                return false;
            }
        }
        return true;
    }
    
    public boolean areAdjacent(Cell cell1, Cell cell2) {
        int rowDiff = Math.abs(cell1.getRow() - cell2.getRow());
        int colDiff = Math.abs(cell1.getCol() - cell2.getCol());
        
        return rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff) > 0;
    }
    
    public boolean hasNoDuplicates(List<Cell> cells) {
        return cells.stream()
            .map(cell -> cell.getRow() + "," + cell.getCol())
            .distinct()
            .count() == cells.size();
    }
    
    public boolean isWithinShape(List<Cell> cells, Shape shape) {
        if (shape == null) {
            return true;
        }
        
        boolean[][] mask = shape.getMask();
        return cells.stream()
            .allMatch(cell -> {
                int row = cell.getRow();
                int col = cell.getCol();
                return row < mask.length && 
                       col < mask[0].length && 
                       mask[row][col];
            });
    }
    
    public String getPathAsString(Path path, Grid grid) {
        return path.getCells().stream()
            .map(cell -> grid.getCells()[cell.getRow()][cell.getCol()].getCharacter())
            .map(String::valueOf)
            .reduce("", String::concat);
    }
    
    public int calculatePathLength(Path path) {
        return path.getCells().size();
    }
    
    public boolean isPathComplete(Path path, int minLength) {
        return path.getCells().size() >= minLength;
    }
}