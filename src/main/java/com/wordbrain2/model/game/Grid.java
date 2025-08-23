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
        // This is a simplified version - in production, you'd want more sophisticated letter placement
        Random random = new Random();
        String alphabet = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].isActive()) {
                    cells[i][j].setCharacter(alphabet.charAt(random.nextInt(26)));
                }
            }
        }
        
        // Place actual words in the grid (simplified)
        if (!words.isEmpty()) {
            placeWord(words.get(0));
        }
    }
    
    private void placeWord(String word) {
        // Simplified word placement - place horizontally if possible
        if (word.length() <= cols) {
            int row = new Random().nextInt(rows);
            int startCol = new Random().nextInt(cols - word.length() + 1);
            
            for (int i = 0; i < word.length(); i++) {
                if (cells[row][startCol + i].isActive()) {
                    cells[row][startCol + i].setCharacter(word.charAt(i));
                }
            }
            
            solutions.add(new Word(word, row, startCol, Word.Direction.HORIZONTAL));
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
}