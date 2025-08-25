package com.wordbrain2.util;

import com.wordbrain2.model.game.Cell;
import com.wordbrain2.model.game.Grid;
import com.wordbrain2.model.game.Word;
import java.util.*;

public class GridGenerator {
    private static final Random random = new Random();
    private static final String VIETNAMESE_CHARS = "AĂÂBCDĐEÊGHIKLMNOÔƠPQRSTUƯVXY";
    private static final String ENGLISH_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    
    public static Grid generateGrid(int rows, int cols, List<String> words, String language) {
        Cell[][] cells = new Cell[rows][cols];
        Grid grid = new Grid(rows, cols);  // Use constructor with dimensions
        
        // Initialize empty grid
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                cells[i][j] = new Cell();
                cells[i][j].setRow(i);
                cells[i][j].setCol(j);
                cells[i][j].setEmpty(true);
            }
        }
        
        // Place words in grid
        List<Word> placedWords = new ArrayList<>();
        for (String word : words) {
            if (placeWord(cells, word, rows, cols)) {
                Word gridWord = new Word(word);  // Use constructor with text
                placedWords.add(gridWord);
            }
        }
        
        // Fill empty cells with random characters
        fillEmptyCells(cells, rows, cols, language);
        
        grid.setCells(cells);
        grid.setSolutions(placedWords);
        grid.setTotalCells(rows * cols);
        
        return grid;
    }
    
    private static boolean placeWord(Cell[][] cells, String word, int rows, int cols) {
        int attempts = 50;
        
        while (attempts > 0) {
            int startRow = random.nextInt(rows);
            int startCol = random.nextInt(cols);
            Direction direction = Direction.values()[random.nextInt(Direction.values().length)];
            
            if (canPlaceWord(cells, word, startRow, startCol, direction, rows, cols)) {
                doPlaceWord(cells, word, startRow, startCol, direction);
                return true;
            }
            
            attempts--;
        }
        
        return false;
    }
    
    private static boolean canPlaceWord(Cell[][] cells, String word, int row, int col, 
                                       Direction dir, int rows, int cols) {
        for (int i = 0; i < word.length(); i++) {
            int newRow = row + dir.rowDelta * i;
            int newCol = col + dir.colDelta * i;
            
            if (newRow < 0 || newRow >= rows || newCol < 0 || newCol >= cols) {
                return false;
            }
            
            Cell cell = cells[newRow][newCol];
            if (!cell.isEmpty() && !cell.getValue().equals(String.valueOf(word.charAt(i)))) {
                return false;
            }
        }
        
        return true;
    }
    
    private static void doPlaceWord(Cell[][] cells, String word, int row, int col, Direction dir) {
        for (int i = 0; i < word.length(); i++) {
            int newRow = row + dir.rowDelta * i;
            int newCol = col + dir.colDelta * i;
            
            Cell cell = cells[newRow][newCol];
            cell.setValue(String.valueOf(word.charAt(i)));
            cell.setEmpty(false);
        }
    }
    
    private static void fillEmptyCells(Cell[][] cells, int rows, int cols, String language) {
        String charset = "vietnamese".equals(language) ? VIETNAMESE_CHARS : ENGLISH_CHARS;
        
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j].isEmpty()) {
                    char randomChar = charset.charAt(random.nextInt(charset.length()));
                    cells[i][j].setValue(String.valueOf(randomChar));
                    cells[i][j].setEmpty(false);
                }
            }
        }
    }
    
    public static void shuffleGrid(Cell[][] cells, int rows, int cols) {
        List<String> values = new ArrayList<>();
        
        // Collect all values
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j] != null && cells[i][j].getValue() != null) {
                    values.add(cells[i][j].getValue());
                }
            }
        }
        
        // Shuffle values
        Collections.shuffle(values);
        
        // Reassign values
        int index = 0;
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                if (cells[i][j] != null && index < values.size()) {
                    cells[i][j].setValue(values.get(index++));
                }
            }
        }
    }
    
    private enum Direction {
        HORIZONTAL(0, 1),
        VERTICAL(1, 0),
        DIAGONAL_DOWN(1, 1),
        DIAGONAL_UP(-1, 1);
        
        final int rowDelta;
        final int colDelta;
        
        Direction(int rowDelta, int colDelta) {
            this.rowDelta = rowDelta;
            this.colDelta = colDelta;
        }
    }
}