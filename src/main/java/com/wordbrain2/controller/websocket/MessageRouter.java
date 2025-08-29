package com.wordbrain2.controller.websocket;

import com.google.gson.Gson;
import com.wordbrain2.model.enums.MessageType;
import com.wordbrain2.service.core.GameEngine;
import com.wordbrain2.service.core.RoomService;
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
        
        // Clean up handler state
        roomMessageHandler.cleanupSession(sessionId);
        
        // Notify room if player was in one
        if (playerId != null && roomCode != null) {
            broadcastToRoom(roomCode, MessageType.PLAYER_LEFT, Map.of(
                "playerId", playerId,
                "message", "Player disconnected"
            ));
            broadcastRoomState(roomCode);
        }
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
        try {
            BaseMessage message = new BaseMessage(type, convertToMap(data));
            session.sendMessage(new TextMessage(gson.toJson(message)));
        } catch (Exception e) {
            log.error("Error sending message", e);
        }
    }
    
    private void sendError(WebSocketSession session, String error) {
        sendMessage(session, MessageType.ERROR, Map.of("error", error));
    }

    private void sendInvalidAction(WebSocketSession session, String reason) {
        sendMessage(session, MessageType.INVALID_ACTION, Map.of("reason", reason));
    }
    
    private void broadcastToRoom(String roomCode, MessageType type, Object data) {
        broadcastToRoom(roomCode, type, data, null);
    }
    
    private void broadcastToRoom(String roomCode, MessageType type, Object data, String excludeSessionId) {
        var room = roomService.getRoom(roomCode);
        if (room != null) {
            room.getPlayers().forEach(player -> {
                String sessionId = player.getSessionId();
                if (sessionId == null) return;
                if (excludeSessionId != null && excludeSessionId.equals(sessionId)) return;

                WebSocketSession session = connectionManager.getSession(sessionId);
                if (session != null && session.isOpen()) {
                    sendMessage(session, type, data);
                }
            });
        }
    }

    private void broadcastRoomState(String roomCode) {
        var room = roomService.getRoom(roomCode);
        if (room == null) return;

        List<Map<String, Object>> players = new ArrayList<>();
        room.getPlayers().forEach(p -> {
            boolean ready = Boolean.TRUE.equals(room.getPlayerReady().get(p.getId()));
            Map<String, Object> pInfo = new HashMap<>();
            pInfo.put("id", p.getId());
            pInfo.put("name", p.getName());
            pInfo.put("ready", ready);
            pInfo.put("isHost", p.getId().equals(room.getHostId()));
            players.add(pInfo);
        });

        Map<String, Object> state = new HashMap<>();
        state.put("roomCode", room.getRoomCode());
        state.put("hostId", room.getHostId());
        state.put("players", players);
        state.put("playersCount", room.getPlayerCount());
        state.put("maxPlayers", room.getMaxPlayers());
        
        broadcastToRoom(roomCode, MessageType.ROOM_STATE, state);
    }
    
    private Map<String, Object> convertToMap(Object data) {
        if (data instanceof Map) {
            Map<String, Object> result = new HashMap<>();
            Map<?, ?> rawMap = (Map<?, ?>) data;
            
            // Filter out null keys
            rawMap.entrySet().forEach(entry -> {
                if (entry.getKey() != null) {
                    result.put(entry.getKey().toString(), entry.getValue());
                }
            });
            return result;
        } else {
            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("data", data);
            return wrapper;
        }
    }
}