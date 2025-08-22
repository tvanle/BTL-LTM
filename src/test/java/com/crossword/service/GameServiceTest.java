package com.crossword.service;

import com.crossword.model.*;
import com.crossword.repository.GameRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class GameServiceTest {

    @Mock
    private GameRepository gameRepository;

    @Mock
    private DictionaryService dictionaryService;

    @InjectMocks
    private GameService gameService;

    private GameState testGame;
    private Player testPlayer1;
    private Player testPlayer2;

    @BeforeEach
    void setUp() {
        testPlayer1 = new Player("player1", "Alice");
        testPlayer2 = new Player("player2", "Bob");
        
        CrosswordGrid grid = new CrosswordGrid(15, 15);
        testGame = new GameState("test-game-id", grid);
    }

    @Test
    void createNewGame_ShouldReturnGameWithUniqueId() {
        // Given
        when(gameRepository.save(any(GameState.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        GameState result = gameService.createNewGame();

        // Then
        assertNotNull(result);
        assertNotNull(result.getGameId());
        assertEquals(GameState.GameStatus.WAITING, result.getStatus());
        verify(gameRepository).save(any(GameState.class));
    }

    @Test
    void joinGame_WithValidGameAndPlayer_ShouldAddPlayerToGame() {
        // Given
        when(gameRepository.findById("test-game-id")).thenReturn(testGame);
        when(gameRepository.save(any(GameState.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        GameState result = gameService.joinGame("test-game-id", testPlayer1);

        // Then
        assertNotNull(result);
        assertEquals(1, result.getPlayers().size());
        assertTrue(result.getPlayers().containsKey("player1"));
        verify(gameRepository).save(testGame);
    }

    @Test
    void joinGame_WithTwoPlayers_ShouldStartGame() {
        // Given
        testGame.getPlayers().put("player1", testPlayer1);
        when(gameRepository.findById("test-game-id")).thenReturn(testGame);
        when(gameRepository.save(any(GameState.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        GameState result = gameService.joinGame("test-game-id", testPlayer2);

        // Then
        assertEquals(GameState.GameStatus.IN_PROGRESS, result.getStatus());
        assertEquals(2, result.getPlayers().size());
        assertTrue(result.getStartTime() > 0);
    }

    @Test
    void joinGame_WithFullGame_ShouldReturnNull() {
        // Given
        testGame.getPlayers().put("player1", testPlayer1);
        testGame.getPlayers().put("player2", testPlayer2);
        testGame.setStatus(GameState.GameStatus.IN_PROGRESS);
        when(gameRepository.findById("test-game-id")).thenReturn(testGame);

        // When
        Player newPlayer = new Player("player3", "Charlie");
        GameState result = gameService.joinGame("test-game-id", newPlayer);

        // Then
        assertNull(result);
        verify(gameRepository, never()).save(any());
    }

    @Test
    void validateMove_WithValidMove_ShouldReturnTrue() {
        // Given
        testGame.getPlayers().put("player1", testPlayer1);
        testGame.setStatus(GameState.GameStatus.IN_PROGRESS);
        when(gameRepository.findById("test-game-id")).thenReturn(testGame);
        when(gameRepository.save(any(GameState.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        boolean result = gameService.validateMove("test-game-id", "player1", 5, 5, 'J');

        // Then
        assertTrue(result);
        verify(gameRepository).save(testGame);
    }

    @Test
    void validateMove_WithInvalidGame_ShouldReturnFalse() {
        // Given
        when(gameRepository.findById("invalid-game")).thenReturn(null);

        // When
        boolean result = gameService.validateMove("invalid-game", "player1", 5, 5, 'J');

        // Then
        assertFalse(result);
        verify(gameRepository, never()).save(any());
    }

    @Test
    void validateMove_WithFinishedGame_ShouldReturnFalse() {
        // Given
        testGame.setStatus(GameState.GameStatus.FINISHED);
        when(gameRepository.findById("test-game-id")).thenReturn(testGame);

        // When
        boolean result = gameService.validateMove("test-game-id", "player1", 5, 5, 'J');

        // Then
        assertFalse(result);
        verify(gameRepository, never()).save(any());
    }

    @Test
    void endGame_ShouldSetStatusToFinished() {
        // Given
        testGame.setStatus(GameState.GameStatus.IN_PROGRESS);
        when(gameRepository.findById("test-game-id")).thenReturn(testGame);
        when(gameRepository.save(any(GameState.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // When
        gameService.endGame("test-game-id");

        // Then
        assertEquals(GameState.GameStatus.FINISHED, testGame.getStatus());
        verify(gameRepository).save(testGame);
    }
}