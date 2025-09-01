package com.wordbrain2.controller.websocket;

import com.google.gson.Gson;
import com.wordbrain2.model.enums.MessageType;
import com.wordbrain2.service.core.GameEngine;
import com.wordbrain2.service.core.RoomService;
import com.wordbrain2.service.messaging.MessageBroadcastService;
import com.wordbrain2.websocket.handler.RoomMessageHandler;
import com.wordbrain2.websocket.handler.GameMessageHandler;
import com.wordbrain2.websocket.handler.BoosterMessageHandler;
import com.wordbrain2.websocket.message.BaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class MessageRouter {
    
    @Autowired
    private RoomMessageHandler roomMessageHandler;
    
    @Autowired
    private GameMessageHandler gameMessageHandler;
    
    @Autowired
    private BoosterMessageHandler boosterMessageHandler;
    
    @Autowired
    private ConnectionManager connectionManager;
    
    @Autowired
    private GameEngine gameEngine;
    
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private MessageBroadcastService broadcastService;
    
    private final Gson gson = new Gson();
    
    public void routeMessage(WebSocketSession session, TextMessage message) {
        try {
            String payload = message.getPayload();
            log.debug("Routing message: {}", payload);
            
            BaseMessage gameMessage = gson.fromJson(payload, BaseMessage.class);
            Map<String, Object> result = null;
            MessageType responseType = null;
            
            // Get MessageType enum from message
            MessageType messageType = gameMessage.getMessageType();
            if (messageType == null) {
                log.warn("Unknown message type: {}", gameMessage.getType());
                sendError(session, "Unknown message type: " + gameMessage.getType());
                return;
            }
            
            switch (messageType) {
                case CREATE_ROOM:
                    result = roomMessageHandler.handleCreateRoom(session, gameMessage);
                    responseType = result != null ? MessageType.ROOM_CREATED : null;
                    if (result != null) {
                        String roomCode = (String) result.get("roomCode");
                        broadcastRoomState(roomCode);
                    }
                    break;
                    
                case JOIN_ROOM:
                    result = roomMessageHandler.handleJoinRoom(session, gameMessage);
                    responseType = result != null ? MessageType.ROOM_JOINED : null;
                    if (result != null) {
                        String roomCode = (String) result.get("roomCode");
                        String playerId = (String) result.get("playerId");
                        String playerName = (String) result.get("playerName");
                        
                        // Notify other players
                        broadcastToRoom(roomCode, MessageType.PLAYER_JOINED, Map.of(
                            "playerId", playerId,
                            "playerName", playerName
                        ), session.getId());
                        broadcastRoomState(roomCode);
                    }
                    break;
                    
                case LEAVE_ROOM:
                    result = roomMessageHandler.handleLeaveRoom(session, gameMessage);
                    if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                        String roomCode = (String) result.get("roomCode");
                        String playerId = (String) result.get("playerId");
                        
                        broadcastToRoom(roomCode, MessageType.PLAYER_LEFT, Map.of(
                            "playerId", playerId
                        ));
                        broadcastRoomState(roomCode);
                    }
                    break;
                    
                case PLAYER_READY:
                    result = roomMessageHandler.handlePlayerReady(session, gameMessage);
                    if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                        String playerId = (String) result.get("playerId");
                        String roomCode = roomMessageHandler.getRoomForPlayer(playerId);
                        boolean ready = (boolean) result.get("ready");
                        
                        broadcastToRoom(roomCode, MessageType.PLAYER_READY, Map.of(
                            "playerId", playerId,
                            "ready", ready
                        ));
                        broadcastRoomState(roomCode);
                    }
                    break;
                    
                case START_GAME:
                    result = gameMessageHandler.handleStartGame(session, gameMessage);
                    if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                        String playerId = roomMessageHandler.getPlayerIdForSession(session.getId());
                        String roomCode = roomMessageHandler.getRoomForPlayer(playerId);
                        
                        broadcastToRoom(roomCode, MessageType.GAME_STARTING, Map.of(
                            "countdown", result.get("countdown"),
                            "message", result.get("message")
                        ));
                        
                        // Schedule actual game start
                        scheduleGameStart(roomCode);
                    } else if (result != null && result.containsKey("error")) {
                        sendInvalidAction(session, (String) result.get("error"));
                        return;
                    }
                    break;
                    
                case SUBMIT_WORD:
                    result = gameMessageHandler.handleSubmitWord(session, gameMessage);
                    if (result != null) {
                        boolean correct = Boolean.TRUE.equals(result.get("correct"));
                        String playerId = roomMessageHandler.getPlayerIdForSession(session.getId());
                        String roomCode = roomMessageHandler.getRoomForPlayer(playerId);
                        
                        if (correct) {
                            sendMessage(session, MessageType.WORD_ACCEPTED, result);
                            broadcastToRoom(roomCode, MessageType.OPPONENT_SCORED, Map.of(
                                "playerId", playerId,
                                "points", result.get("points"),
                                "word", result.get("word")
                            ), session.getId());
                        } else {
                            sendMessage(session, MessageType.WORD_REJECTED, result);
                        }
                        
                        // Update leaderboard
                        var leaderboard = gameEngine.getLeaderboard(roomCode);
                        broadcastToRoom(roomCode, MessageType.LEADERBOARD_UPDATE, leaderboard);
                    }
                    break;
                    
                case USE_BOOSTER:
                    result = boosterMessageHandler.handleUseBooster(session, gameMessage);
                    if (result != null && Boolean.TRUE.equals(result.get("success"))) {
                        sendMessage(session, MessageType.BOOSTER_APPLIED, result);
                        
                        // If booster affects others, notify them
                        if (Boolean.TRUE.equals(result.get("affectsOthers"))) {
                            String playerId = roomMessageHandler.getPlayerIdForSession(session.getId());
                            String roomCode = roomMessageHandler.getRoomForPlayer(playerId);
                            
                            broadcastToRoom(roomCode, MessageType.EFFECT_RECEIVED, Map.of(
                                "effect", result.get("effectType"),
                                "duration", result.get("effectDuration"),
                                "fromPlayer", result.get("fromPlayer")
                            ), session.getId());
                        }
                    }
                    break;
                    
                case REQUEST_HINT:
                    result = gameMessageHandler.handleRequestHint(session, gameMessage);
                    if (result != null) {
                        sendMessage(session, MessageType.HINT_RESPONSE, result);
                    }
                    break;
                    
            }
            
            // Send response if there's an error
            if (result != null && result.containsKey("error") && !Boolean.TRUE.equals(result.get("success"))) {
                sendError(session, (String) result.get("error"));
            } else if (result != null && responseType != null) {
                sendMessage(session, responseType, result);
            }
            
        } catch (Exception e) {
            log.error("Error routing message", e);
            sendError(session, "Error processing message: " + e.getMessage());
        }
    }
    
    public void handleDisconnect(String sessionId) {
        // Get player info before cleanup
        String playerId = roomMessageHandler.getPlayerIdForSession(sessionId);
        String roomCode = playerId != null ? roomMessageHandler.getRoomForPlayer(playerId) : null;
        
        // IMPORTANT: Remove player from room when disconnected
        if (playerId != null && roomCode != null) {
            log.info("Player {} disconnected from room {} - removing from room", playerId, roomCode);
            
            // Remove player from room
            roomService.removePlayer(roomCode, playerId);
            
            // Notify other players
            broadcastToRoom(roomCode, MessageType.PLAYER_LEFT, Map.of(
                "playerId", playerId,
                "message", "Player disconnected"
            ));
            broadcastRoomState(roomCode);
        }
        
        // Clean up connection manager state
        connectionManager.removeSession(sessionId);
    }
    
    private void scheduleGameStart(String roomCode) {
        new Thread(() -> {
            try {
                Thread.sleep(5000);
                var levelData = gameEngine.startLevel(roomCode, 1);
                broadcastToRoom(roomCode, MessageType.LEVEL_START, levelData);
            } catch (InterruptedException e) {
                log.error("Game start interrupted", e);
            } catch (Exception ex) {
                log.error("Failed to start level", ex);
                broadcastToRoom(roomCode, MessageType.ERROR, 
                    Map.of("error", "Không thể bắt đầu level: " + ex.getMessage()));
            }
        }).start();
    }
    
    private void sendMessage(WebSocketSession session, MessageType type, Object data) {
        broadcastService.sendMessage(session, type, data);
    }
    
    private void sendError(WebSocketSession session, String error) {
        broadcastService.sendError(session, error);
    }

    private void sendInvalidAction(WebSocketSession session, String reason) {
        broadcastService.sendInvalidAction(session, reason);
    }
    
    private void broadcastToRoom(String roomCode, MessageType type, Object data) {
        broadcastService.broadcastToRoom(roomCode, type, data);
    }
    
    private void broadcastToRoom(String roomCode, MessageType type, Object data, String excludeSessionId) {
        broadcastService.broadcastToRoom(roomCode, type, data, excludeSessionId);
    }

    private void broadcastRoomState(String roomCode) {
        broadcastService.   broadcastRoomState(roomCode);
    }
}