package com.wordbrain2.websocket.handler;

import com.wordbrain2.controller.websocket.ConnectionManager;
import com.wordbrain2.model.entity.Room;
import com.wordbrain2.service.core.RoomService;
import com.wordbrain2.websocket.message.BaseMessage;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;
import java.util.Map;
import java.util.Set;

@Component
public class ChatMessageHandler {
    
    private final RoomService roomService;
    private final ConnectionManager connectionManager;
    
    public ChatMessageHandler(RoomService roomService, ConnectionManager connectionManager) {
        this.roomService = roomService;
        this.connectionManager = connectionManager;
    }
    
    public void handleChatMessage(WebSocketSession session, BaseMessage message) {
        String playerId = connectionManager.getPlayerId(session.getId());
        String roomCode = message.getRoomCode();
        
        if (playerId == null || roomCode == null) {
            sendErrorMessage(session, "Invalid session or room code");
            return;
        }
        
        Room room = roomService.findByCode(roomCode).orElse(null);
        if (room == null) {
            sendErrorMessage(session, "Room not found");
            return;
        }
        
        if (!connectionManager.isPlayerInRoom(playerId, roomCode)) {
            sendErrorMessage(session, "Player not in room");
            return;
        }
        
        processChatMessage(room, playerId, message);
    }
    
    private void processChatMessage(Room room, String playerId, BaseMessage message) {
        Map<String, Object> data = message.getData();
        String chatMessage = (String) data.get("message");
        String messageType = (String) data.get("type");
        
        if (chatMessage == null || chatMessage.trim().isEmpty()) {
            return;
        }
        
        // Filter inappropriate content
        String filteredMessage = filterMessage(chatMessage);
        
        // Create chat response
        BaseMessage chatResponse = createChatResponse(playerId, filteredMessage, messageType);
        chatResponse.setRoomCode(room.getCode());
        
        // Broadcast to all players in room
        broadcastToRoom(room, chatResponse);
    }
    
    private String filterMessage(String message) {
        // Basic content filtering
        return message.replaceAll("(?i)(damn|hell)", "***")
                     .trim()
                     .substring(0, Math.min(message.length(), 200)); // Max 200 chars
    }
    
    private BaseMessage createChatResponse(String playerId, String message, String type) {
        BaseMessage response = new BaseMessage();
        response.setType("CHAT_MESSAGE");
        response.setPlayerId(playerId);
        
        Map<String, Object> data = Map.of(
            "message", message,
            "type", type != null ? type : "general",
            "timestamp", System.currentTimeMillis()
        );
        response.setData(data);
        
        return response;
    }
    
    private void broadcastToRoom(Room room, BaseMessage message) {
        connectionManager.broadcastToRoom(room.getCode(), message);
    }
    
    private void sendErrorMessage(WebSocketSession session, String error) {
        BaseMessage errorMessage = new BaseMessage();
        errorMessage.setType("ERROR");
        errorMessage.setData(Map.of("message", error));
        connectionManager.sendMessage(session, errorMessage);
    }
    
    public void handlePlayerJoinedRoom(Room room, String playerId) {
        BaseMessage joinMessage = new BaseMessage();
        joinMessage.setType("PLAYER_JOINED_CHAT");
        joinMessage.setRoomCode(room.getCode());
        joinMessage.setPlayerId(playerId);
        joinMessage.setData(Map.of(
            "message", "Player joined the game",
            "timestamp", System.currentTimeMillis()
        ));
        
        broadcastToRoom(room, joinMessage);
    }
    
    public void handlePlayerLeftRoom(Room room, String playerId) {
        BaseMessage leaveMessage = new BaseMessage();
        leaveMessage.setType("PLAYER_LEFT_CHAT");
        leaveMessage.setRoomCode(room.getCode());
        leaveMessage.setPlayerId(playerId);
        leaveMessage.setData(Map.of(
            "message", "Player left the game",
            "timestamp", System.currentTimeMillis()
        ));
        
        broadcastToRoom(room, leaveMessage);
    }
}