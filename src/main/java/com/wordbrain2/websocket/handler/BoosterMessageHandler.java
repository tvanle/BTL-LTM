package com.wordbrain2.websocket.handler;

import com.wordbrain2.model.dto.request.UseBoosterRequest;
import com.wordbrain2.model.enums.BoosterType;
import com.wordbrain2.service.core.GameEngine;
import com.wordbrain2.service.booster.BoosterService;
import com.wordbrain2.service.event.EventBusService;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import java.util.HashMap;
import java.util.Map;

@Component
public class BoosterMessageHandler {
    @Autowired
    private GameEngine gameEngine;
    
    @Autowired
    private BoosterService boosterService;
    
    @Autowired
    private EventBusService eventBusService;
    
    private final Gson gson = new Gson();
    
    public void handleMessage(WebSocketSession session, JsonObject message) {
        JsonObject data = message.getAsJsonObject("data");
        
        String roomCode = data.get("roomCode").getAsString();
        String playerId = data.get("playerId").getAsString();
        String boosterTypeStr = data.get("boosterType").getAsString();
        
        BoosterType boosterType = BoosterType.valueOf(boosterTypeStr);
        
        Map<String, Object> boosterData = new HashMap<>();
        boosterData.put("boosterType", boosterType);
        
        Map<String, Object> result = gameEngine.useBooster(roomCode, playerId, boosterData);
        boolean success = result != null && Boolean.TRUE.equals(result.get("success"));
        
        if (success) {
            Map<String, Object> successData = new HashMap<>();
            successData.put("success", true);
            successData.put("boosterType", boosterType.name());
            successData.put("playerId", playerId);
            sendResponse(session, "BOOSTER_USED", successData);
            
            // Notify all players about booster usage
            eventBusService.publishBoosterUsed(roomCode, playerId, boosterType);
        } else {
            sendError(session, "Failed to use booster");
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