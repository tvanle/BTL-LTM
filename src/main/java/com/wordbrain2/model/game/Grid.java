package com.wordbrain2.model.game;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Data
public class Grid {
    private int rows;
    private int cols;
    private Cell[][] cells;
    private Shape shape;
    private List<Word> solutions;
    private int totalCells;
    
    public Grid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.cells = new Cell[rows][cols];
        this.solutions = new ArrayList<>();
        initializeCells();
    }
    
    private void initializeCells() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = new Cell(i, j);
            }
        }
    }
    
    public void applyShape(Shape shape) {
        this.shape = shape;
        this.totalCells = 0;
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (shape.isActive(i, j)) {
                    cells[i][j].setActive(true);
                    totalCells++;
                } else {
                    cells[i][j].setActive(false);
                }
            }
        }
    }
    
    public void fillWithLetters(List<String> words) {
        // WordBrain2 style: combine all word letters and scramble them into the grid
        StringBuilder allLetters = new StringBuilder();
        
        // Combine all letters from target words
        for (String word : words) {
            allLetters.append(word.toUpperCase());
        }
        
        // Convert to list for shuffling
        List<Character> letterList = new ArrayList<>();
        for (char c : allLetters.toString().toCharArray()) {
            letterList.add(c);
        }
        
        // Shuffle the letters
        java.util.Collections.shuffle(letterList);
        
        // Fill active cells with shuffled letters
        int letterIndex = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].isActive() && letterIndex < letterList.size()) {
                    cells[i][j].setCharacter(letterList.get(letterIndex));
                    letterIndex++;
                }
            }
        }
        
        // Store the solution words
        this.solutions = new ArrayList<>();
        for (String word : words) {
            this.solutions.add(new Word(word.toUpperCase()));
        }
    }
    
    
    public Cell getCell(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return cells[row][col];
        }
        return null;
    }
    
    public boolean isValidPath(List<Cell> path) {
        if (path.isEmpty()) return false;
        
        // Check if all cells are active and adjacent
        for (int i = 0; i < path.size() - 1; i++) {
            Cell current = path.get(i);
            Cell next = path.get(i + 1);
            
            if (!current.isActive() || !next.isActive()) {
                return false;
            }
            
            if (!areAdjacent(current, next)) {
                return false;
            }
        }
        
        // Check if path doesn't repeat cells
        for (int i = 0; i < path.size(); i++) {
            for (int j = i + 1; j < path.size(); j++) {
                if (path.get(i).equals(path.get(j))) {
                    return false;
                }
            }
        }
        
        return true;
    }
    
    private boolean areAdjacent(Cell c1, Cell c2) {
        int rowDiff = Math.abs(c1.getRow() - c2.getRow());
        int colDiff = Math.abs(c1.getCol() - c2.getCol());
        return rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff) > 0;
    }
    
    // Remove word and apply gravity effect
    public void removeWordAndApplyGravity(List<Cell> path) {
        // First, clear the cells in the path
        for (Cell cell : path) {
            cells[cell.getRow()][cell.getCol()].setCharacter('\0');
            cells[cell.getRow()][cell.getCol()].setActive(false);
        }
        
        // Apply gravity effect - cells fall down to fill empty spaces
        applyGravity();
    }
    
    private void applyGravity() {
        // Process each column from bottom to top
        for (int col = 0; col < cols; col++) {
            // Find all non-empty cells in this column
            List<Character> activeCells = new ArrayList<>();
            
            for (int row = rows - 1; row >= 0; row--) {
                if (cells[row][col].isActive() && cells[row][col].getCharacter() != '\0') {
                    activeCells.add(cells[row][col].getCharacter());
                }
            }
            
            // Clear the column
            for (int row = 0; row < rows; row++) {
                if (shape.isActive(row, col)) {
                    cells[row][col].setCharacter('\0');
                    cells[row][col].setActive(false);
                }
            }
            
            // Place cells back from bottom
            int activeIndex = 0;
            for (int row = rows - 1; row >= 0 && activeIndex < activeCells.size(); row--) {
                if (shape.isActive(row, col)) {
                    cells[row][col].setCharacter(activeCells.get(activeIndex));
                    cells[row][col].setActive(true);
                    activeIndex++;
                }
            }
        }
    }
    
    // Get grid state as 2D character array for frontend
    public Character[][] getGridState() {
        Character[][] state = new Character[rows][cols];
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].isActive() && cells[i][j].getCharacter() != '\0') {
                    state[i][j] = cells[i][j].getCharacter();
                } else {
                    state[i][j] = null;
                }
            }
        }
        return state;
    }
    
    // Check if grid is empty (level complete)
    public boolean isEmpty() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].isActive() && cells[i][j].getCharacter() != '\0') {
                    return false;
                }
            }
        }
        return true;
    }
}