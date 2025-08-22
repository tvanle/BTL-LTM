package com.crossword.model;

import java.util.List;
import java.util.ArrayList;

public class Word {
    private String word;
    private String clue;
    private Direction direction;
    private int startRow;
    private int startCol;
    private List<Position> positions;
    private boolean completed;
    
    public enum Direction {
        HORIZONTAL, VERTICAL
    }
    
    public static class Position {
        public final int row;
        public final int col;
        
        public Position(int row, int col) {
            this.row = row;
            this.col = col;
        }
    }
    
    public Word() {}
    
    public Word(String word, String clue, Direction direction, int startRow, int startCol) {
        this.word = word.toUpperCase();
        this.clue = clue;
        this.direction = direction;
        this.startRow = startRow;
        this.startCol = startCol;
        this.positions = new ArrayList<>();
        this.completed = false;
        generatePositions();
    }
    
    private void generatePositions() {
        for (int i = 0; i < word.length(); i++) {
            int row = startRow;
            int col = startCol;
            
            if (direction == Direction.HORIZONTAL) {
                col += i;
            } else {
                row += i;
            }
            
            positions.add(new Position(row, col));
        }
    }
    
    public boolean containsPosition(int row, int col) {
        return positions.stream().anyMatch(pos -> pos.row == row && pos.col == col);
    }
    
    public char getCharAt(int row, int col) {
        for (int i = 0; i < positions.size(); i++) {
            Position pos = positions.get(i);
            if (pos.row == row && pos.col == col) {
                return word.charAt(i);
            }
        }
        return '\0';
    }
    
    // Getters and setters
    public String getWord() { return word; }
    public void setWord(String word) { this.word = word; }
    
    public String getClue() { return clue; }
    public void setClue(String clue) { this.clue = clue; }
    
    public Direction getDirection() { return direction; }
    public void setDirection(Direction direction) { this.direction = direction; }
    
    public int getStartRow() { return startRow; }
    public void setStartRow(int startRow) { this.startRow = startRow; }
    
    public int getStartCol() { return startCol; }
    public void setStartCol(int startCol) { this.startCol = startCol; }
    
    public List<Position> getPositions() { return positions; }
    public void setPositions(List<Position> positions) { this.positions = positions; }
    
    public boolean isCompleted() { return completed; }
    public void setCompleted(boolean completed) { this.completed = completed; }
}