package com.wordbrain2.controller.websocket;

import com.wordbrain2.model.enums.MessageType;
import com.wordbrain2.websocket.handler.RoomMessageHandler;
import com.wordbrain2.websocket.handler.GameMessageHandler;
import com.wordbrain2.websocket.handler.BoosterMessageHandler;
import com.wordbrain2.websocket.message.BaseMessage;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

@Component
public class MessageRouter {
    @Autowired
    private RoomMessageHandler roomMessageHandler;
    
    @Autowired
    private GameMessageHandler gameMessageHandler;
    
    @Autowired
    private BoosterMessageHandler boosterMessageHandler;
    
    private final Gson gson = new Gson();
    
    public void routeMessage(WebSocketSession session, TextMessage message) {
        try {
            JsonObject jsonMessage = gson.fromJson(message.getPayload(), JsonObject.class);
            String typeStr = jsonMessage.get("type").getAsString();
            MessageType type = MessageType.valueOf(typeStr);
            
            switch (type) {
                case CREATE_ROOM:
                case JOIN_ROOM:
                case LEAVE_ROOM:
                case PLAYER_READY:
                    roomMessageHandler.handleMessage(session, jsonMessage);
                    break;
                    
                case START_GAME:
                case SUBMIT_WORD:
                case REQUEST_HINT:
                    gameMessageHandler.handleMessage(session, jsonMessage);
                    break;
                    
                case USE_BOOSTER:
                    boosterMessageHandler.handleMessage(session, jsonMessage);
                    break;
                    
                default:
                    handleUnknownMessage(session, type);
            }
        } catch (Exception e) {
            sendError(session, "Failed to process message: " + e.getMessage());
        }
    }
    
    private void handleUnknownMessage(WebSocketSession session, MessageType type) {
        sendError(session, "Unknown message type: " + type);
    }
    
    private void sendError(WebSocketSession session, String error) {
        try {
            BaseMessage errorMessage = new BaseMessage();
            errorMessage.setType(MessageType.ERROR.name());
            java.util.Map<String, Object> errorData = new java.util.HashMap<>();
            errorData.put("error", error);
            errorMessage.setData(errorData);
            errorMessage.setTimestamp(System.currentTimeMillis());
            
            session.sendMessage(new TextMessage(gson.toJson(errorMessage)));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}