package com.crossword.websocket;

import com.crossword.model.*;
import com.crossword.service.GameService;
import com.crossword.service.SessionService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.UUID;

@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    
    @Autowired
    private GameService gameService;
    
    @Autowired
    private SessionService sessionService;
    
    private final Gson gson = new Gson();
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        System.out.println("WebSocket connection established: " + session.getId());
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            JsonObject jsonMessage = JsonParser.parseString(message.getPayload()).getAsJsonObject();
            String type = jsonMessage.get("type").getAsString();
            
            switch (type) {
                case "JOIN_GAME":
                    handleJoinGame(session, jsonMessage);
                    break;
                case "PLACE_CHAR":
                    handlePlaceChar(session, jsonMessage);
                    break;
                case "PING":
                    handlePing(session, jsonMessage);
                    break;
                default:
                    sendError(session, "Unknown message type: " + type);
            }
        } catch (Exception e) {
            System.err.println("Error handling WebSocket message: " + e.getMessage());
            sendError(session, "Invalid message format");
        }
    }
    
    private void handleJoinGame(WebSocketSession session, JsonObject jsonMessage) throws IOException {
        String gameId = jsonMessage.get("gameId").getAsString();
        String playerName = jsonMessage.get("playerName").getAsString();
        
        // Create new player
        String playerId = UUID.randomUUID().toString();
        Player player = new Player(playerId, playerName);
        
        // Add to session management
        sessionService.addSession(session.getId(), session, player);
        
        // Join or create game
        GameState game = gameService.findGameById(gameId);
        if (game == null) {
            game = gameService.createNewGame();
        }
        
        game = gameService.joinGame(game.getGameId(), player);
        
        if (game != null) {
            // Send game state to new player
            GameMessage response = new GameMessage(GameMessage.MessageType.GAME_STATE, playerId, game);
            session.sendMessage(new TextMessage(gson.toJson(response)));
            
            // Notify other players
            broadcastToOtherPlayers(game.getGameId(), playerId, 
                new GameMessage(GameMessage.MessageType.PLAYER_JOINED, playerId, player));
            
            // Start game if 2 players
            if (game.getPlayers().size() == 2 && game.getStatus() == GameState.GameStatus.IN_PROGRESS) {
                broadcastToAllPlayers(game.getGameId(), 
                    new GameMessage(GameMessage.MessageType.GAME_STARTED, null, game));
            }
        } else {
            sendError(session, "Could not join game");
        }
    }
    
    private void handlePlaceChar(WebSocketSession session, JsonObject jsonMessage) throws IOException {
        String gameId = jsonMessage.get("gameId").getAsString();
        String playerId = jsonMessage.get("playerId").getAsString();
        int row = jsonMessage.get("row").getAsInt();
        int col = jsonMessage.get("col").getAsInt();
        char character = jsonMessage.get("character").getAsString().charAt(0);
        
        // Update player activity
        sessionService.updatePlayerActivity(playerId);
        
        // Validate move
        boolean valid = gameService.validateMove(gameId, playerId, row, col, character);
        
        GameState game = gameService.findGameById(gameId);
        if (game != null) {
            if (valid) {
                // Send validation success to all players
                GameMessage validatedMsg = new GameMessage(GameMessage.MessageType.CHAR_VALIDATED, playerId, 
                    new CharMove(row, col, character, playerId));
                broadcastToAllPlayers(gameId, validatedMsg);
                
                // Send score update
                GameMessage scoreMsg = new GameMessage(GameMessage.MessageType.SCORE_UPDATE, playerId, game.getPlayers());
                broadcastToAllPlayers(gameId, scoreMsg);
                
                // Check if game ended
                if (game.getStatus() == GameState.GameStatus.FINISHED) {
                    GameMessage endMsg = new GameMessage(GameMessage.MessageType.GAME_ENDED, null, game);
                    broadcastToAllPlayers(gameId, endMsg);
                }
            } else {
                // Send rejection to player
                GameMessage rejectedMsg = new GameMessage(GameMessage.MessageType.CHAR_REJECTED, playerId, 
                    new CharMove(row, col, character, playerId));
                session.sendMessage(new TextMessage(gson.toJson(rejectedMsg)));
            }
        }
    }
    
    private void handlePing(WebSocketSession session, JsonObject jsonMessage) throws IOException {
        String playerId = jsonMessage.get("playerId").getAsString();
        sessionService.updatePlayerActivity(playerId);
        
        GameMessage pongMsg = new GameMessage(GameMessage.MessageType.PONG, playerId, System.currentTimeMillis());
        session.sendMessage(new TextMessage(gson.toJson(pongMsg)));
    }
    
    private void broadcastToAllPlayers(String gameId, GameMessage message) {
        GameState game = gameService.findGameById(gameId);
        if (game != null) {
            for (String playerId : game.getPlayers().keySet()) {
                WebSocketSession playerSession = sessionService.getSessionByPlayerId(playerId);
                if (playerSession != null && playerSession.isOpen()) {
                    try {
                        playerSession.sendMessage(new TextMessage(gson.toJson(message)));
                    } catch (IOException e) {
                        System.err.println("Error sending message to player " + playerId + ": " + e.getMessage());
                    }
                }
            }
        }
    }
    
    private void broadcastToOtherPlayers(String gameId, String excludePlayerId, GameMessage message) {
        GameState game = gameService.findGameById(gameId);
        if (game != null) {
            for (String playerId : game.getPlayers().keySet()) {
                if (!playerId.equals(excludePlayerId)) {
                    WebSocketSession playerSession = sessionService.getSessionByPlayerId(playerId);
                    if (playerSession != null && playerSession.isOpen()) {
                        try {
                            playerSession.sendMessage(new TextMessage(gson.toJson(message)));
                        } catch (IOException e) {
                            System.err.println("Error sending message to player " + playerId + ": " + e.getMessage());
                        }
                    }
                }
            }
        }
    }
    
    private void sendError(WebSocketSession session, String errorMessage) throws IOException {
        GameMessage errorMsg = new GameMessage(GameMessage.MessageType.ERROR, null, errorMessage);
        session.sendMessage(new TextMessage(gson.toJson(errorMsg)));
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        Player player = sessionService.getPlayerBySessionId(session.getId());
        if (player != null) {
            System.out.println("Player disconnected: " + player.getName());
            
            // Find game and notify other players
            // This is a simplified implementation - in a real app you'd track game-player relationships
            sessionService.removeSession(session.getId());
        }
        
        System.out.println("WebSocket connection closed: " + session.getId());
    }
    
    // Helper class for character move data
    public static class CharMove {
        private int row;
        private int col;
        private char character;
        private String playerId;
        
        public CharMove(int row, int col, char character, String playerId) {
            this.row = row;
            this.col = col;
            this.character = character;
            this.playerId = playerId;
        }
        
        // Getters
        public int getRow() { return row; }
        public int getCol() { return col; }
        public char getCharacter() { return character; }
        public String getPlayerId() { return playerId; }
    }
}