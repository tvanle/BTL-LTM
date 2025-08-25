package com.wordbrain2.websocket.handler;

import com.google.gson.Gson;
import com.wordbrain2.model.enums.MessageType;
import com.wordbrain2.service.core.GameEngine;
import com.wordbrain2.service.core.RoomService;
import com.wordbrain2.websocket.message.BaseMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class GameWebSocketHandler extends TextWebSocketHandler {
    
    private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<>();
    private final Map<String, String> sessionToPlayer = new ConcurrentHashMap<>();
    private final Map<String, String> playerToRoom = new ConcurrentHashMap<>();
    
    private final GameEngine gameEngine;
    private final RoomService roomService;
    private final Gson gson;
    
    public GameWebSocketHandler(GameEngine gameEngine, RoomService roomService) {
        this.gameEngine = gameEngine;
        this.roomService = roomService;
        this.gson = new Gson();
    }
    
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {
        log.info("WebSocket connection established: {}", session.getId());
        sessions.put(session.getId(), session);
        
        // Send connection success message
        BaseMessage welcomeMessage = new BaseMessage();
        welcomeMessage.setType(MessageType.PLAYER_JOINED.name());
        Map<String, Object> welcomeData = new HashMap<>();
        welcomeData.put("sessionId", session.getId());
        welcomeData.put("message", "Connected to game server");
        welcomeMessage.setData(welcomeData);
        
        session.sendMessage(new TextMessage(gson.toJson(welcomeMessage)));
    }
    
    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {
        try {
            String payload = message.getPayload();
            log.debug("Received message: {}", payload);
            
            BaseMessage gameMessage = gson.fromJson(payload, BaseMessage.class);
            
            switch (gameMessage.getType()) {
                case "CREATE_ROOM":
                    handleCreateRoom(session, gameMessage);
                    break;
                    
                case "JOIN_ROOM":
                    handleJoinRoom(session, gameMessage);
                    break;
                    
                case "LEAVE_ROOM":
                    handleLeaveRoom(session, gameMessage);
                    break;
                    
                case "PLAYER_READY":
                    handlePlayerReady(session, gameMessage);
                    break;
                    
                case "START_GAME":
                    handleStartGame(session, gameMessage);
                    break;
                    
                case "SUBMIT_WORD":
                    handleSubmitWord(session, gameMessage);
                    break;
                    
                case "USE_BOOSTER":
                    handleUseBooster(session, gameMessage);
                    break;
                    
                default:
                    log.warn("Unknown message type: {}", gameMessage.getType());
            }
            
        } catch (Exception e) {
            log.error("Error handling message", e);
            sendError(session, "Error processing message: " + e.getMessage());
        }
    }
    
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {
        log.info("WebSocket connection closed: {} - {}", session.getId(), status);
        
        String playerId = sessionToPlayer.remove(session.getId());
        if (playerId != null) {
            String roomCode = playerToRoom.remove(playerId);
            if (roomCode != null) {
                roomService.removePlayer(roomCode, playerId);
                broadcastToRoom(roomCode, MessageType.PLAYER_LEFT, Map.of(
                    "playerId", playerId,
                    "message", "Player disconnected"
                ));
                // Broadcast full room state after a player leaves
                broadcastRoomState(roomCode);
            }
        }
        
        sessions.remove(session.getId());
    }
    
    private void handleCreateRoom(WebSocketSession session, BaseMessage message) {
        Map<?, ?> data = (Map<?, ?>) message.getData();
        String playerName = getString(data, "playerName");
        String topic = getString(data, "topic");
        
        var result = roomService.createRoom(playerName, topic, session.getId());
        
        if (result != null) {
            String roomCode = (String) result.get("roomCode");
            String playerId = (String) result.get("playerId");
            
            sessionToPlayer.put(session.getId(), playerId);
            playerToRoom.put(playerId, roomCode);
            
            sendMessage(session, MessageType.ROOM_CREATED, result);
            // Send initial room state to the creator
            broadcastRoomState(roomCode);
        } else {
            sendError(session, "Failed to create room");
        }
    }
    
    private void handleJoinRoom(WebSocketSession session, BaseMessage message) {
        Map<?, ?> data = (Map<?, ?>) message.getData();
        String roomCode = getString(data, "roomCode");
        String playerName = getString(data, "playerName");
        
        var result = roomService.joinRoom(roomCode, playerName, session.getId());
        
        if (result != null) {
            String playerId = (String) result.get("playerId");
            
            sessionToPlayer.put(session.getId(), playerId);
            playerToRoom.put(playerId, roomCode);
            
            sendMessage(session, MessageType.ROOM_JOINED, result);
            
            // Notify other players
            broadcastToRoom(roomCode, MessageType.PLAYER_JOINED, Map.of(
                "playerId", playerId,
                "playerName", playerName
            ), session.getId());
            // Broadcast full room state to everyone
            broadcastRoomState(roomCode);
        } else {
            sendError(session, "Failed to join room");
        }
    }
    
    private void handleLeaveRoom(WebSocketSession session, BaseMessage message) {
        String playerId = resolvePlayerId(session, message);
        if (playerId == null) {
            sendInvalidAction(session, "Bạn chưa tham gia phòng.");
            return;
        }
        String roomCode = resolveRoomCode(session, message, playerId);
        if (roomCode == null) {
            sendInvalidAction(session, "Không xác định phòng.");
            return;
        }
        ensureSessionRegistered(session, playerId, roomCode);
        
        roomService.removePlayer(roomCode, playerId);
        playerToRoom.remove(playerId);
        
        broadcastToRoom(roomCode, MessageType.PLAYER_LEFT, Map.of(
            "playerId", playerId
        ));
        // Broadcast full room state after leave
        broadcastRoomState(roomCode);
    }
    
    private void handlePlayerReady(WebSocketSession session, BaseMessage message) {
        String playerId = resolvePlayerId(session, message);
        if (playerId == null) {
            sendInvalidAction(session, "Bạn chưa tham gia phòng.");
            return;
        }
        String roomCode = resolveRoomCode(session, message, playerId);
        if (roomCode == null) {
            sendInvalidAction(session, "Không xác định phòng.");
            return;
        }
        ensureSessionRegistered(session, playerId, roomCode);
        
        Map<?, ?> data = (Map<?, ?>) message.getData();
        boolean ready = getBoolean(data, "ready");
        
        roomService.setPlayerReady(roomCode, playerId, ready);
        
        broadcastToRoom(roomCode, MessageType.PLAYER_READY, Map.of(
            "playerId", playerId,
            "ready", ready
        ));
        // Broadcast full room state after ready toggle
        broadcastRoomState(roomCode);
    }
    
    private void handleStartGame(WebSocketSession session, BaseMessage message) {
        String playerId = resolvePlayerId(session, message);
        if (playerId == null) {
            sendInvalidAction(session, "Bạn chưa tham gia phòng.");
            return;
        }
        String roomCode = resolveRoomCode(session, message, playerId);
        if (roomCode == null) {
            sendInvalidAction(session, "Không xác định phòng.");
            return;
        }
        ensureSessionRegistered(session, playerId, roomCode);
        
        // Enforce host-only start
        var room = roomService.getRoom(roomCode);
        if (room == null) {
            sendInvalidAction(session, "Phòng không tồn tại.");
            return;
        }
        if (!playerId.equals(room.getHostId())) {
            sendInvalidAction(session, "Chỉ chủ phòng mới có thể bắt đầu trò chơi.");
            return;
        }
        // Enforce all players ready before starting
        boolean allReady = room.getPlayerReady().keySet().stream()
            .allMatch(pid -> Boolean.TRUE.equals(room.getPlayerReady().get(pid)));
        if (!allReady) {
            sendInvalidAction(session, "Tất cả người chơi phải sẵn sàng để bắt đầu.");
            return;
        }
        
        var gameState = gameEngine.startGame(roomCode);
        
        if (gameState != null) {
            broadcastToRoom(roomCode, MessageType.GAME_STARTING, Map.of(
                "countdown", 5,
                "message", "Game starting in 5 seconds..."
            ));
            
            // Schedule actual game start
            // In production, use a scheduled executor
            new Thread(() -> {
                try {
                    Thread.sleep(5000);
                    var levelData = gameEngine.startLevel(roomCode, 1);
                    broadcastToRoom(roomCode, MessageType.LEVEL_START, levelData);
                } catch (InterruptedException e) {
                    log.error("Game start interrupted", e);
                } catch (Exception ex) {
                    log.error("Failed to start level", ex);
                    broadcastToRoom(roomCode, MessageType.ERROR, Map.of("error", "Không thể bắt đầu level: " + ex.getMessage()));
                }
            }).start();
        }
    }
    
    private void handleSubmitWord(WebSocketSession session, BaseMessage message) {
        String playerId = resolvePlayerId(session, message);
        if (playerId == null) {
            sendInvalidAction(session, "Bạn chưa tham gia phòng.");
            return;
        }
        String roomCode = resolveRoomCode(session, message, playerId);
        if (roomCode == null) {
            sendInvalidAction(session, "Không xác định phòng.");
            return;
        }
        ensureSessionRegistered(session, playerId, roomCode);
        
        var result = gameEngine.submitWord(roomCode, playerId, message.getData());
        
        if (result != null) {
            boolean correct = Boolean.TRUE.equals(result.get("correct"));
            
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
    }
    
    private void handleUseBooster(WebSocketSession session, BaseMessage message) {
        String playerId = resolvePlayerId(session, message);
        if (playerId == null) {
            sendInvalidAction(session, "Bạn chưa tham gia phòng.");
            return;
        }
        String roomCode = resolveRoomCode(session, message, playerId);
        if (roomCode == null) {
            sendInvalidAction(session, "Không xác định phòng.");
            return;
        }
        ensureSessionRegistered(session, playerId, roomCode);
        
        var result = gameEngine.useBooster(roomCode, playerId, message.getData());
        
        if (result != null) {
            sendMessage(session, MessageType.BOOSTER_APPLIED, result);
            
            // If booster affects others (like FREEZE), notify them
            Map<?, ?> data = (Map<?, ?>) message.getData();
            String boosterType = getString(data, "boosterType");
            if ("FREEZE".equals(boosterType)) {
                broadcastToRoom(roomCode, MessageType.EFFECT_RECEIVED, Map.of(
                    "effect", "FREEZE",
                    "duration", 3000,
                    "fromPlayer", playerId
                ), session.getId());
            }
        }
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

                WebSocketSession session = sessions.get(sessionId);
                if (session != null && session.isOpen()) {
                    sendMessage(session, type, data);
                }
            });
        }
    }

    // Build and broadcast the full ROOM_STATE to all clients in the room
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

    private String getString(Map<?, ?> map, String key) {
        if (map == null) return null;
        Object val = map.get(key);
        return val != null ? String.valueOf(val) : null;
    }

    private boolean getBoolean(Map<?, ?> map, String key) {
        if (map == null) return false;
        Object val = map.get(key);
        if (val instanceof Boolean) return (Boolean) val;
        if (val instanceof Number) return ((Number) val).intValue() != 0;
        if (val != null) return Boolean.parseBoolean(String.valueOf(val));
        return false;
    }

    private String resolvePlayerId(WebSocketSession session, BaseMessage message) {
        String pid = sessionToPlayer.get(session.getId());
        if (pid == null && message != null && message.getData() != null) {
            pid = (String) message.getData().get("playerId");
        }
        return pid;
    }

    private String resolveRoomCode(WebSocketSession session, BaseMessage message, String playerId) {
        String rc = null;
        if (playerId != null) rc = playerToRoom.get(playerId);
        if (rc == null && message != null && message.getData() != null) {
            rc = (String) message.getData().get("roomCode");
        }
        return rc;
    }

    private void ensureSessionRegistered(WebSocketSession session, String playerId, String roomCode) {
        if (playerId == null || roomCode == null) return;
        sessionToPlayer.putIfAbsent(session.getId(), playerId);
        playerToRoom.putIfAbsent(playerId, roomCode);
    }
    
    private Map<String, Object> convertToMap(Object data) {
        if (data instanceof Map) {
            return (Map<String, Object>) data;
        } else {
            Map<String, Object> wrapper = new HashMap<>();
            wrapper.put("data", data);
            return wrapper;
        }
    }
}