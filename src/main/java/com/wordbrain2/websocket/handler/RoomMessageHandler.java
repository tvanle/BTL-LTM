package com.wordbrain2.websocket.handler;

import com.wordbrain2.model.dto.request.CreateRoomRequest;
import com.wordbrain2.model.dto.request.JoinRoomRequest;
import com.wordbrain2.model.dto.response.RoomResponse;
import com.wordbrain2.model.entity.Player;
import com.wordbrain2.model.entity.Room;
import com.wordbrain2.service.core.RoomService;
import com.wordbrain2.service.core.PlayerService;
import com.wordbrain2.service.event.EventBusService;
import com.wordbrain2.controller.websocket.ConnectionManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.util.HashMap;
import java.util.Map;

@Component
public class RoomMessageHandler {
    @Autowired
    private RoomService roomService;
    
    @Autowired
    private PlayerService playerService;
    
    @Autowired
    private EventBusService eventBusService;
    
    @Autowired
    private ConnectionManager connectionManager;
    
    private final Gson gson = new Gson();
    
    public void handleMessage(WebSocketSession session, JsonObject message) {
        String type = message.get("type").getAsString();
        JsonObject data = message.getAsJsonObject("data");
        
        switch (type) {
            case "CREATE_ROOM":
                handleCreateRoom(session, data);
                break;
            case "JOIN_ROOM":
                handleJoinRoom(session, data);
                break;
            case "LEAVE_ROOM":
                handleLeaveRoom(session, data);
                break;
            case "PLAYER_READY":
                handlePlayerReady(session, data);
                break;
        }
    }
    
    private void handleCreateRoom(WebSocketSession session, JsonObject data) {
        CreateRoomRequest request = gson.fromJson(data, CreateRoomRequest.class);
        
        // Create player
        Player host = playerService.createPlayer(request.getHostName(), null);
        host.setHost(true);
        
        // Create room
        RoomResponse response = roomService.createRoom(request, host.getId());
        
        // Register session
        connectionManager.registerPlayer(session, host.getId());
        
        // Add host to room
        roomService.addPlayerToRoom(response.getRoomCode(), host);
        playerService.assignPlayerToRoom(host.getId(), response.getRoomCode());
        
        // Send response
        sendResponse(session, "ROOM_CREATED", response);
        
        // Notify others
        eventBusService.publishPlayerJoined(response.getRoomCode(), host.getId(), host.getName());
    }
    
    private void handleJoinRoom(WebSocketSession session, JsonObject data) {
        JoinRoomRequest request = gson.fromJson(data, JoinRoomRequest.class);
        
        // Check if room exists
        Room room = roomService.getRoom(request.getRoomCode());
        if (room == null) {
            sendError(session, "Room not found");
            return;
        }
        
        // Create player
        Player player = playerService.createPlayer(request.getPlayerName(), request.getAvatarUrl());
        
        // Register session
        connectionManager.registerPlayer(session, player.getId());
        
        // Add player to room
        boolean added = roomService.addPlayerToRoom(request.getRoomCode(), player);
        if (!added) {
            sendError(session, "Failed to join room");
            return;
        }
        
        playerService.assignPlayerToRoom(player.getId(), request.getRoomCode());
        
        // Send response
        RoomResponse response = roomService.getRoomInfo(request.getRoomCode());
        sendResponse(session, "JOINED_ROOM", response);
        
        // Notify others
        eventBusService.publishPlayerJoined(request.getRoomCode(), player.getId(), player.getName());
    }
    
    private void handleLeaveRoom(WebSocketSession session, JsonObject data) {
        String playerId = data.get("playerId").getAsString();
        String roomCode = data.get("roomCode").getAsString();
        
        Player player = playerService.getPlayer(playerId);
        if (player == null) {
            return;
        }
        
        // Remove player from room
        roomService.removePlayerFromRoom(roomCode, playerId);
        playerService.removePlayerFromRoom(playerId);
        
        // Notify others
        eventBusService.publishPlayerLeft(roomCode, playerId, player.getName());
        
        // Send confirmation
        Map<String, Object> leftResponse = new HashMap<>();
        leftResponse.put("success", true);
        sendResponse(session, "LEFT_ROOM", leftResponse);
    }
    
    private void handlePlayerReady(WebSocketSession session, JsonObject data) {
        String playerId = data.get("playerId").getAsString();
        String roomCode = data.get("roomCode").getAsString();
        boolean ready = data.get("ready").getAsBoolean();
        
        // Update ready status
        roomService.setPlayerReady(roomCode, playerId, ready);
        playerService.setPlayerReady(playerId, ready);
        
        // Notify all players
        eventBusService.publishPlayerReady(roomCode, playerId, ready);
        
        // Check if all ready and auto-start
        if (roomService.areAllPlayersReady(roomCode)) {
            Map<String, Object> readyData = new HashMap<>();
            readyData.put("message", "All players ready! Game can start now.");
            eventBusService.publishEvent(roomCode, "ALL_PLAYERS_READY", readyData);
        }
    }
    
    private void sendResponse(WebSocketSession session, String type, Object data) {
        try {
            Map<String, Object> response = new HashMap<>();
            response.put("type", type);
            response.put("data", data);
            response.put("timestamp", System.currentTimeMillis());
            
            session.sendMessage(new TextMessage(gson.toJson(response)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    private void sendError(WebSocketSession session, String error) {
        Map<String, Object> errorData = new HashMap<>();
        errorData.put("error", error);
        sendResponse(session, "ERROR", errorData);
    }
}