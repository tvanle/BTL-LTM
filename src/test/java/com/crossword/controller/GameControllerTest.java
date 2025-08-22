package com.crossword.controller;

import com.crossword.model.GameState;
import com.crossword.model.Player;
import com.crossword.model.CrosswordGrid;
import com.crossword.service.GameService;
import com.crossword.service.SessionService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(GameController.class)
public class GameControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private GameService gameService;

    @MockBean
    private SessionService sessionService;

    @Autowired
    private ObjectMapper objectMapper;

    private GameState testGame;
    private Player testPlayer;

    @BeforeEach
    void setUp() {
        CrosswordGrid grid = new CrosswordGrid(15, 15);
        testGame = new GameState("test-game-id", grid);
        testPlayer = new Player("test-player-id", "TestPlayer");
    }

    @Test
    void createGame_ShouldReturnGameInfo() throws Exception {
        // Given
        when(gameService.createNewGame()).thenReturn(testGame);

        // When & Then
        mockMvc.perform(post("/api/games")
                .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.gameId").value("test-game-id"))
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.playersCount").value(0));
    }

    @Test
    void getGame_WithValidId_ShouldReturnGame() throws Exception {
        // Given
        when(gameService.findGameById("test-game-id")).thenReturn(testGame);

        // When & Then
        mockMvc.perform(get("/api/games/test-game-id"))
                .andExpected(status().isOk())
                .andExpect(jsonPath("$.gameId").value("test-game-id"))
                .andExpect(jsonPath("$.status").value("WAITING"));
    }

    @Test
    void getGame_WithInvalidId_ShouldReturn404() throws Exception {
        // Given
        when(gameService.findGameById("invalid-id")).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/games/invalid-id"))
                .andExpected(status().isNotFound());
    }

    @Test
    void joinGame_WithValidRequest_ShouldReturnPlayerInfo() throws Exception {
        // Given
        testGame.getPlayers().put(testPlayer.getId(), testPlayer);
        when(gameService.joinGame(eq("test-game-id"), any(Player.class))).thenReturn(testGame);

        Map<String, String> joinRequest = new HashMap<>();
        joinRequest.put("playerName", "TestPlayer");

        // When & Then
        mockMvc.perform(post("/api/games/test-game-id/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(joinRequest)))
                .andExpected(status().isOk())
                .andExpect(jsonPath("$.playerName").value("TestPlayer"))
                .andExpect(jsonPath("$.gameStatus").value("WAITING"))
                .andExpect(jsonPath("$.playersCount").value(1));
    }

    @Test
    void joinGame_WithEmptyPlayerName_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, String> joinRequest = new HashMap<>();
        joinRequest.put("playerName", "");

        // When & Then
        mockMvc.perform(post("/api/games/test-game-id/join")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(joinRequest)))
                .andExpected(status().isBadRequest());
    }

    @Test
    void makeMove_WithValidRequest_ShouldReturnMoveResult() throws Exception {
        // Given
        when(gameService.validateMove("test-game-id", "test-player-id", 5, 5, 'A')).thenReturn(true);
        when(gameService.findGameById("test-game-id")).thenReturn(testGame);

        Map<String, Object> moveRequest = new HashMap<>();
        moveRequest.put("playerId", "test-player-id");
        moveRequest.put("row", 5);
        moveRequest.put("col", 5);
        moveRequest.put("character", "A");

        // When & Then
        mockMvc.perform(post("/api/games/test-game-id/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(moveRequest)))
                .andExpected(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.row").value(5))
                .andExpect(jsonPath("$.col").value(5))
                .andExpect(jsonPath("$.character").value("A"));
    }

    @Test
    void makeMove_WithInvalidRequest_ShouldReturnBadRequest() throws Exception {
        // Given
        Map<String, Object> moveRequest = new HashMap<>();
        moveRequest.put("playerId", "test-player-id");
        // Missing row, col, character

        // When & Then
        mockMvc.perform(post("/api/games/test-game-id/move")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(moveRequest)))
                .andExpected(status().isBadRequest());
    }

    @Test
    void getGameStatus_WithValidId_ShouldReturnStatus() throws Exception {
        // Given
        testGame.getPlayers().put(testPlayer.getId(), testPlayer);
        when(gameService.findGameById("test-game-id")).thenReturn(testGame);

        // When & Then
        mockMvc.perform(get("/api/games/test-game-id/status"))
                .andExpected(status().isOk())
                .andExpect(jsonPath("$.status").value("WAITING"))
                .andExpect(jsonPath("$.playersCount").value(1))
                .andExpect(jsonPath("$.remainingTime").exists())
                .andExpect(jsonPath("$.players").exists());
    }

    @Test
    void getGameStatus_WithInvalidId_ShouldReturn404() throws Exception {
        // Given
        when(gameService.findGameById("invalid-id")).thenReturn(null);

        // When & Then
        mockMvc.perform(get("/api/games/invalid-id/status"))
                .andExpected(status().isNotFound());
    }
}