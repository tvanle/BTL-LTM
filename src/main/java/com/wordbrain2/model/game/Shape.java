package com.wordbrain2.model.game;

import lombok.Data;

@Data
public class Shape {
    private boolean[][] mask;
    private int cellCount;
    private ShapeType type;
    
    public enum ShapeType {
        SQUARE,
        CIRCLE,
        DIAMOND,
        L_SHAPE,
        T_SHAPE,
        CROSS,
        CUSTOM
    }
    
    public Shape(int rows, int cols, ShapeType type) {
        this.mask = new boolean[rows][cols];
        this.type = type;
        this.cellCount = 0;
        generateShape(type);
    }
    
    private void generateShape(ShapeType type) {
        int rows = mask.length;
        int cols = mask[0].length;
        int centerRow = rows / 2;
        int centerCol = cols / 2;
        
        switch (type) {
            case SQUARE:
                // Full square
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        mask[i][j] = true;
                        cellCount++;
                    }
                }
                break;
                
            case CIRCLE:
                // Approximate circle shape
                int radius = Math.min(rows, cols) / 2;
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        double distance = Math.sqrt(
                            Math.pow(i - centerRow, 2) + 
                            Math.pow(j - centerCol, 2)
                        );
                        if (distance <= radius) {
                            mask[i][j] = true;
                            cellCount++;
                        }
                    }
                }
                break;
                
            case DIAMOND:
                // Diamond shape
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        int distance = Math.abs(i - centerRow) + Math.abs(j - centerCol);
                        if (distance <= Math.min(centerRow, centerCol)) {
                            mask[i][j] = true;
                            cellCount++;
                        }
                    }
                }
                break;
                
            case L_SHAPE:
                // L shape
                for (int i = 0; i < rows; i++) {
                    mask[i][0] = true;
                    cellCount++;
                }
                for (int j = 1; j < cols; j++) {
                    mask[rows - 1][j] = true;
                    cellCount++;
                }
                break;
                
            case T_SHAPE:
                // T shape
                for (int j = 0; j < cols; j++) {
                    mask[0][j] = true;
                    cellCount++;
                }
                for (int i = 1; i < rows; i++) {
                    mask[i][centerCol] = true;
                    cellCount++;
                }
                break;
                
            case CROSS:
                // Cross/Plus shape
                for (int i = 0; i < rows; i++) {
                    mask[i][centerCol] = true;
                    cellCount++;
                }
                for (int j = 0; j < cols; j++) {
                    if (j != centerCol) {
                        mask[centerRow][j] = true;
                        cellCount++;
                    }
                }
                break;
                
            case CUSTOM:
                // Default to square if custom
                for (int i = 0; i < rows; i++) {
                    for (int j = 0; j < cols; j++) {
                        mask[i][j] = true;
                        cellCount++;
                    }
                }
                break;
        }
    }
    
    public boolean isActive(int row, int col) {
        if (row >= 0 && row < mask.length && col >= 0 && col < mask[0].length) {
            return mask[row][col];
        }
        return false;
    }
    
    public void setActive(int row, int col, boolean active) {
        if (row >= 0 && row < mask.length && col >= 0 && col < mask[0].length) {
            if (mask[row][col] != active) {
                mask[row][col] = active;
                cellCount += active ? 1 : -1;
            }
        }
    }
}