package com.wordbrain2.websocket.handler;

import com.wordbrain2.model.dto.request.StartGameRequest;
import com.wordbrain2.model.dto.request.SubmitWordRequest;
import com.wordbrain2.model.game.Cell;
import com.wordbrain2.service.core.GameEngine;
import com.wordbrain2.service.event.EventBusService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonArray;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.util.*;

@Component
public class GameMessageHandler {
    @Autowired
    private GameEngine gameEngine;
    
    @Autowired
    private EventBusService eventBusService;
    
    private final Gson gson = new Gson();
    
    public void handleMessage(WebSocketSession session, JsonObject message) {
        String type = message.get("type").getAsString();
        JsonObject data = message.getAsJsonObject("data");
        
        switch (type) {
            case "START_GAME":
                handleStartGame(session, data);
                break;
            case "SUBMIT_WORD":
                handleSubmitWord(session, data);
                break;
            case "REQUEST_HINT":
                handleRequestHint(session, data);
                break;
        }
    }
    
    private void handleStartGame(WebSocketSession session, JsonObject data) {
        StartGameRequest request = gson.fromJson(data, StartGameRequest.class);
        
        Map<String, Object> startResult = gameEngine.startGame(request.getRoomCode());
        
        if (startResult != null) {
            Map<String, Object> startData = new HashMap<>();
            startData.put("success", true);
            startData.put("roomCode", request.getRoomCode());
            sendResponse(session, "GAME_STARTED", startData);
        } else {
            sendError(session, "Failed to start game");
        }
    }
    
    private void handleSubmitWord(WebSocketSession session, JsonObject data) {
        String roomCode = data.get("roomCode").getAsString();
        String playerId = data.get("playerId").getAsString();
        String word = data.get("word").getAsString();
        
        // Parse path
        List<Cell> path = new ArrayList<>();
        JsonArray pathArray = data.getAsJsonArray("path");
        for (int i = 0; i < pathArray.size(); i++) {
            JsonObject cellObj = pathArray.get(i).getAsJsonObject();
            Cell cell = new Cell();
            cell.setRow(cellObj.get("row").getAsInt());
            cell.setCol(cellObj.get("col").getAsInt());
            if (cellObj.has("char")) {
                cell.setValue(cellObj.get("char").getAsString());
            }
            path.add(cell);
        }
        
        // Submit word
        Map<String, Object> response = gameEngine.submitWord(roomCode, playerId, path, word);
        
        // Send response to player
        if (response != null) {
            sendResponse(session, "WORD_RESULT", response);
        } else {
            sendError(session, "Failed to submit word");
        }
    }
    
    private void handleRequestHint(WebSocketSession session, JsonObject data) {
        String roomCode = data.get("roomCode").getAsString();
        String playerId = data.get("playerId").getAsString();
        
        Map<String, Object> hint = gameEngine.getHint(roomCode, playerId);
        
        if (hint != null) {
            sendResponse(session, "HINT_RECEIVED", hint);
        } else {
            sendError(session, "No hints available");
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