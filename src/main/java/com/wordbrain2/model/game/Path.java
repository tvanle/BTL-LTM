package com.wordbrain2.model.game;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import java.util.List;
import java.util.ArrayList;

@Data
@AllArgsConstructor
@Builder
public class Path {
    @Builder.Default
    private List<Cell> cells = new ArrayList<>();
    private String formedWord;
    private boolean isValid;
    private boolean coversShape;
    
    public Path() {
        this.cells = new ArrayList<>();
        this.isValid = false;
        this.coversShape = false;
    }
    
    public void addCell(Cell cell) {
        if (cells == null) {
            cells = new ArrayList<>();
        }
        cells.add(cell);
    }
    
    public boolean isAdjacent(Cell cell1, Cell cell2) {
        int rowDiff = Math.abs(cell1.getRow() - cell2.getRow());
        int colDiff = Math.abs(cell1.getCol() - cell2.getCol());
        return rowDiff <= 1 && colDiff <= 1 && (rowDiff + colDiff) > 0;
    }
    
    public boolean containsCell(Cell cell) {
        return cells != null && cells.stream()
            .anyMatch(c -> c.getRow() == cell.getRow() && c.getCol() == cell.getCol());
    }
    
    public int getLength() {
        return cells != null ? cells.size() : 0;
    }
}