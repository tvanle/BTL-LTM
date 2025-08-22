package com.crossword.service;

import com.crossword.model.*;
import com.crossword.repository.GameRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
public class GameService {
    
    @Autowired
    private GameRepository gameRepository;
    
    @Autowired
    private DictionaryService dictionaryService;
    
    @Value("${game.grid-size:15}")
    private int gridSize;
    
    @Value("${game.duration-seconds:180}")
    private int gameDuration;
    
    public GameState createNewGame() {
        String gameId = UUID.randomUUID().toString();
        CrosswordGrid grid = createSampleGrid(gridSize, gridSize);
        GameState gameState = new GameState(gameId, grid);
        gameState.setGameDurationSeconds(gameDuration);
        
        return gameRepository.save(gameState);
    }
    
    public GameState findGameById(String gameId) {
        return gameRepository.findById(gameId);
    }
    
    public GameState joinGame(String gameId, Player player) {
        GameState game = gameRepository.findById(gameId);
        if (game != null && game.canAcceptPlayers()) {
            game.getPlayers().put(player.getId(), player);
            
            // Start game when 2 players join
            if (game.getPlayers().size() == 2) {
                game.startGame();
            }
            
            return gameRepository.save(game);
        }
        return null;
    }
    
    public boolean validateMove(String gameId, String playerId, int row, int col, char character) {
        GameState game = gameRepository.findById(gameId);
        if (game == null || game.getStatus() != GameState.GameStatus.IN_PROGRESS) {
            return false;
        }
        
        CrosswordGrid grid = game.getGrid();
        
        // Check if move is valid
        if (!grid.isValidMove(row, col, character)) {
            return false;
        }
        
        // Check if character matches expected words
        if (!grid.validateCellWithWords(row, col, character)) {
            return false;
        }
        
        // Apply the move
        if (grid.setCellChar(row, col, character, playerId)) {
            Cell cell = grid.getCell(row, col);
            cell.setStatus(Cell.CellStatus.CORRECT);
            
            // Update player score
            Player player = game.getPlayers().get(playerId);
            if (player != null) {
                player.addCellScore();
                
                // Check for completed words
                checkCompletedWords(game, playerId, row, col);
            }
            
            // Check if game should end
            if (game.isGameExpired()) {
                game.endGame();
            }
            
            gameRepository.save(game);
            return true;
        }
        
        return false;
    }
    
    private void checkCompletedWords(GameState game, String playerId, int row, int col) {
        CrosswordGrid grid = game.getGrid();
        List<Word> wordsAtPosition = grid.getWordsContainingPosition(row, col);
        
        for (Word word : wordsAtPosition) {
            if (!word.isCompleted() && isWordCompleted(grid, word)) {
                word.setCompleted(true);
                Player player = game.getPlayers().get(playerId);
                if (player != null) {
                    player.addWordBonus(word.getWord().length());
                }
            }
        }
    }
    
    private boolean isWordCompleted(CrosswordGrid grid, Word word) {
        for (Word.Position pos : word.getPositions()) {
            Cell cell = grid.getCell(pos.row, pos.col);
            if (cell == null || 
                cell.getStatus() != Cell.CellStatus.CORRECT ||
                cell.getCharacter() != word.getCharAt(pos.row, pos.col)) {
                return false;
            }
        }
        return true;
    }
    
    public void endGame(String gameId) {
        GameState game = gameRepository.findById(gameId);
        if (game != null && game.getStatus() == GameState.GameStatus.IN_PROGRESS) {
            game.endGame();
            gameRepository.save(game);
        }
    }
    
    private CrosswordGrid createSampleGrid(int rows, int cols) {
        CrosswordGrid grid = new CrosswordGrid(rows, cols);
        
        // Add sample words for testing
        Word word1 = new Word("JAVA", "Programming language", Word.Direction.HORIZONTAL, 5, 5);
        Word word2 = new Word("GAME", "Entertainment software", Word.Direction.VERTICAL, 5, 7);
        Word word3 = new Word("WEB", "Internet platform", Word.Direction.HORIZONTAL, 7, 5);
        
        grid.addWord(word1);
        grid.addWord(word2);
        grid.addWord(word3);
        
        // Set some cells as blocked or locked for demo
        for (int i = 0; i < rows; i++) {
            for (int j = 0; j < cols; j++) {
                Cell cell = grid.getCell(i, j);
                
                // Mark cells that are part of words as available
                boolean partOfWord = false;
                for (Word word : grid.getWords()) {
                    if (word.containsPosition(i, j)) {
                        partOfWord = true;
                        break;
                    }
                }
                
                if (!partOfWord) {
                    // Block cells that are not part of any word
                    cell.setType(Cell.CellType.BLOCKED);
                }
            }
        }
        
        return grid;
    }
}