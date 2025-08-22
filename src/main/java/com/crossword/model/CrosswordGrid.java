package com.crossword.model;

import java.util.List;
import java.util.ArrayList;

public class CrosswordGrid {
    private Cell[][] grid;
    private int rows;
    private int cols;
    private List<Word> words;
    
    public CrosswordGrid() {}
    
    public CrosswordGrid(int rows, int cols) {
        this.rows = rows;
        this.cols = cols;
        this.grid = new Cell[rows][cols];
        this.words = new ArrayList<>();
        initializeGrid();
    }
    
    private void initializeGrid() {
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                grid[i][j] = new Cell(i, j);
            }
        }
    }
    
    public Cell getCell(int row, int col) {
        if (row >= 0 && row < rows && col >= 0 && col < cols) {
            return grid[row][col];
        }
        return null;
    }
    
    public boolean setCellChar(int row, int col, char ch, String playerId) {
        Cell cell = getCell(row, col);
        if (cell != null && cell.getType() == Cell.CellType.EMPTY) {
            cell.setCharacter(ch);
            cell.setOwnerId(playerId);
            cell.setStatus(Cell.CellStatus.PENDING);
            return true;
        }
        return false;
    }
    
    public boolean isValidMove(int row, int col, char ch) {
        Cell cell = getCell(row, col);
        return cell != null && 
               cell.getType() == Cell.CellType.EMPTY && 
               cell.getCharacter() == '\0';
    }
    
    public boolean validateCellWithWords(int row, int col, char ch) {
        for (Word word : words) {
            if (word.containsPosition(row, col)) {
                char expectedChar = word.getCharAt(row, col);
                if (expectedChar != ch) {
                    return false;
                }
            }
        }
        return true;
    }
    
    public List<Word> getWordsContainingPosition(int row, int col) {
        List<Word> result = new ArrayList<>();
        for (Word word : words) {
            if (word.containsPosition(row, col)) {
                result.add(word);
            }
        }
        return result;
    }
    
    // Getters and setters
    public int getRows() { return rows; }
    public void setRows(int rows) { this.rows = rows; }
    
    public int getCols() { return cols; }
    public void setCols(int cols) { this.cols = cols; }
    
    public Cell[][] getGrid() { return grid; }
    public void setGrid(Cell[][] grid) { this.grid = grid; }
    
    public List<Word> getWords() { return words; }
    public void setWords(List<Word> words) { this.words = words; }
    
    public void addWord(Word word) {
        words.add(word);
    }
}