package com.wordbrain2.service.game;

import com.wordbrain2.model.game.Cell;
import com.wordbrain2.model.game.Grid;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WordValidationService {
    
    private final PathValidatorService pathValidator;
    private final DictionaryService dictionaryService;
    
    public WordValidationService(PathValidatorService pathValidator, DictionaryService dictionaryService) {
        this.pathValidator = pathValidator;
        this.dictionaryService = dictionaryService;
    }
    
    public boolean validateWord(String word, List<Cell> path, Grid grid, String topic) {
        if (word == null || path == null || path.isEmpty()) {
            return false;
        }
        
        // Check if path is valid using PathValidatorService
        if (!pathValidator.isValidPath(path, grid)) {
            return false;
        }
        
        // Check if letters match the word
        StringBuilder formedWord = new StringBuilder();
        for (Cell cell : path) {
            formedWord.append(cell.getCharacter());
        }
        
        // Verify the formed word matches submitted word
        if (!word.equalsIgnoreCase(formedWord.toString())) {
            return false;
        }
        
        // Check if word exists in topic dictionary
        return dictionaryService.isValidWord(word, topic);
    }
    
    public boolean coversShape(List<Cell> path, Grid grid) {
        // Check if the path covers all active cells in the shape
        int activeCells = 0;
        for (int i = 0; i < grid.getRows(); i++) {
            for (int j = 0; j < grid.getCols(); j++) {
                if (grid.getCell(i, j).isActive()) {
                    activeCells++;
                }
            }
        }
        
        return path.size() == activeCells;
    }
}