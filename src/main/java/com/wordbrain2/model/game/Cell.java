package com.wordbrain2.model.game;

import lombok.Data;

@Data
public class Cell {
    private int row;
    private int col;
    private char character;
    private boolean active;
    private boolean selected;
    private String ownerId;
    private String value;
    private boolean empty;
    
    public Cell() {
        this.character = ' ';
        this.active = true;
        this.selected = false;
        this.empty = true;
    }
    
    public Cell(int row, int col) {
        this();
        this.row = row;
        this.col = col;
    }
    
    public Cell(int row, int col, char character) {
        this(row, col);
        this.character = character;
    }
    
    public boolean isOccupied() {
        return character != ' ';
    }
    
    public void reset() {
        this.selected = false;
        this.ownerId = null;
    }
    
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Cell cell = (Cell) o;
        return row == cell.row && col == cell.col;
    }
    
    @Override
    public int hashCode() {
        return 31 * row + col;
    }
}