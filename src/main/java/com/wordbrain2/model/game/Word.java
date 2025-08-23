package com.wordbrain2.model.game;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class Word {
    private String text;
    private int startRow;
    private int startCol;
    private Direction direction;
    private List<Cell> path;
    
    public enum Direction {
        HORIZONTAL,
        VERTICAL,
        DIAGONAL_DOWN,
        DIAGONAL_UP,
        CUSTOM_PATH
    }
    
    public Word(String text) {
        this.text = text.toUpperCase();
        this.path = new ArrayList<>();
    }
    
    public Word(String text, int startRow, int startCol, Direction direction) {
        this(text);
        this.startRow = startRow;
        this.startCol = startCol;
        this.direction = direction;
    }
    
    public Word(String text, List<Cell> path) {
        this(text);
        this.path = path;
        this.direction = Direction.CUSTOM_PATH;
        
        if (!path.isEmpty()) {
            this.startRow = path.get(0).getRow();
            this.startCol = path.get(0).getCol();
        }
    }
    
    public int getLength() {
        return text.length();
    }
    
    public boolean matchesPath(List<Cell> otherPath) {
        if (path.size() != otherPath.size()) {
            return false;
        }
        
        StringBuilder word = new StringBuilder();
        for (Cell cell : otherPath) {
            word.append(cell.getCharacter());
        }
        
        return text.equals(word.toString());
    }
}